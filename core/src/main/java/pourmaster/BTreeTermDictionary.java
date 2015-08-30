package pourmaster;

import com.google.common.collect.ImmutableList;
import pourmaster.terms.Term;
import pourmaster.util.Tuple;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * Term dictionary based on a B-tree. This allows for efficient lookup even if the
 * dictionary does not fit into main memory.
 */
public class BTreeTermDictionary extends TermDictionary {

    public static final String CONFIG_DICTIONARY_FILE_ID = "termDictionary.file";
    public static final String CONFIG_SUPPORT_WILDCARD_ID = "termDictionary.wildcard";
    public static final String CONFIG_PAGE_SIZE = "termDictionary.pageSize";

    private final static String DEFAULT_FILE_NAME = "termDict.idx";
    private final static String B_TREE_NAME = "termDictionary";
    private final static String SUFFIX_B_TREE_NAME = "termDictionary_suffix";

    public BTreeMap<AtomicTerm, TermData> dictionary;
    private BTreeMap<AtomicTerm, TermData> suffixDictionary = null;
    private DB btreeDb;
    private boolean supportWildcardQuery;
    private String dictionaryFile;

    public void init(IndexConfig config) throws IOException {
        dictionaryFile = config.getFilePath(CONFIG_DICTIONARY_FILE_ID, DEFAULT_FILE_NAME);
        new File(dictionaryFile).createNewFile();
        int pageSize = Integer.parseInt(config.get(CONFIG_PAGE_SIZE, "-1"));
        this.supportWildcardQuery = Boolean.valueOf(config.get(CONFIG_SUPPORT_WILDCARD_ID, "true"));
        setupBTree(pageSize);
    }

    private void setupBTree(int pageSize) throws IOException {
        btreeDb = DBMaker.fileDB(new File(dictionaryFile)).fileLockDisable().lockDisable().make();
        DB.BTreeMapMaker bTreeMaker = btreeDb.treeMapCreate(B_TREE_NAME)
                .keySerializer(new AtomicTermSerializer())
                .valueSerializer(new TermDataSerializer())
                .comparator(new AtomicTermComparator());
        if (pageSize > 0) {
            bTreeMaker.nodeSize(pageSize);
        }
        dictionary = bTreeMaker.makeOrGet();
        // Create suffix dictionary
        if (supportWildcardQuery) {
            bTreeMaker = btreeDb.treeMapCreate(SUFFIX_B_TREE_NAME)
                    .keySerializer(new AtomicTermSerializer())
                    .valueSerializer(new TermDataSerializer())
                    .comparator(new AtomicTermComparator());
            if (pageSize > 0) {
                bTreeMaker.nodeSize(pageSize);
            }
            suffixDictionary = bTreeMaker.makeOrGet();
        }
    }

    @Override
    public TermData findTerm(Term term) throws IOException {
        return dictionary.get(term.toAtomic());
    }

    @Override
    public List<TermData> findTerm(Term prefix, Term suffix) throws IOException {

        if (suffixDictionary == null) {
            throw new UnsupportedOperationException("Prefix/suffix queries not enabled.");
        }

        Term reverseSuffix = null;
        if (suffix != null) {
            reverseSuffix = new Term(suffix.getTermType().reverse(suffix.getValue()), suffix.getTermType());
        }

        // Hash table mapping postings pointers to term and data
        HashMap<Long, Tuple<AtomicTerm, TermData>> prefixMatches = null;
        if (prefix != null) {
            // Add all prefix matches to set
            prefixMatches = new HashMap<Long, Tuple<AtomicTerm, TermData>>();
            AtomicTerm atomicPrefix = prefix.toAtomic();
            ConcurrentNavigableMap<AtomicTerm, TermData> cursor = dictionary.tailMap(atomicPrefix, true);
            AtomicTerm currentTerm = cursor.ceilingKey(atomicPrefix);
            do {
                TermData termData = dictionary.get(currentTerm);
                // Check if actually a prefix
                if (!prefix.isPrefix(currentTerm.getValue())) {
                    break;
                }
                prefixMatches.put(termData.getPostingsIndex(), new Tuple(currentTerm, termData));
            } while ((currentTerm = cursor.higherKey(currentTerm)) != null);
        }

        ArrayList<TermData> resultMatches = new ArrayList<TermData>();

        // If prefixMatches != null and its size == 0, there is no need to find suffixes (none will match)
        if (suffix != null && (prefixMatches == null || prefixMatches.size() > 0)) {
            AtomicTerm reverseAtomicSuffix = reverseSuffix.toAtomic();
            ConcurrentNavigableMap<AtomicTerm, TermData> cursor = suffixDictionary.tailMap(reverseAtomicSuffix, true);
            AtomicTerm currentTerm = cursor.ceilingKey(reverseAtomicSuffix);
            do {
                TermData termData = suffixDictionary.get(currentTerm);
                // Check if actually a prefix
                if (!reverseSuffix.isPrefix(currentTerm.getValue())) {
                    break;
                }
                // Add to results
                if (prefix == null) {
                    resultMatches.add(termData);
                } else if (prefixMatches.containsKey(termData.getPostingsIndex())) {
                    // If prefix not null and suffix not null, we must check that the term
                    // does not fully match either the prefix or suffix, e.g. "abba" should not
                    // match query with prefix = "abba", suffix = "ba".
                    if (!currentTerm.getValue().equals(prefix.getValue()) &&
                            !currentTerm.getValue().equals(suffix.getValue())) {
                        resultMatches.add(termData);
                        // Remove from prefixMatches for faster containsKey() in next iterations.
                        prefixMatches.remove(termData.getPostingsIndex());
                    }
                }
            } while ((currentTerm = cursor.higherKey(currentTerm)) != null);
        } else {
            // Handle prefix-query (suffix is null, so add all with matching prefix)
            for (Tuple<AtomicTerm, TermData> match : prefixMatches.values()) {
                resultMatches.add(match.getSecond());
            }
        }

        return resultMatches;
    }

    @Override
    public void addTerm(Term term, TermData data) throws IOException {
        AtomicTerm atomicTerm = term.toAtomic();
        dictionary.put(atomicTerm, data);
        if (supportWildcardQuery) {
            AtomicTerm reversedTerm = new AtomicTerm(term.getTermType().reverse(term.getValue()), atomicTerm.getDataType());
            suffixDictionary.put(reversedTerm, data);
        }
        btreeDb.commit();
    }

    @Override
    public void bulkInsertData(final ImmutableList<Tuple<Term, TermData>> data) throws IOException {
        HashMap<AtomicTerm, TermData> mapData = new HashMap<AtomicTerm, TermData>(data.size());
        for (Tuple<Term, TermData> entry : data) {
            mapData.put(entry.getFirst().toAtomic(), entry.getSecond());
        }
        dictionary.putAll(mapData);
        if (supportWildcardQuery) {
            mapData = new HashMap<AtomicTerm, TermData>(data.size());
            for (Tuple<Term, TermData> entry : data) {
                AtomicTerm atomicTerm = entry.getFirst().toAtomic();
                AtomicTerm reversedTerm = new AtomicTerm(entry.getFirst().getTermType().reverse(
                        entry.getFirst().getValue()), atomicTerm.getDataType());
                mapData.put(reversedTerm, entry.getSecond());
            }
            suffixDictionary.putAll(mapData);
        }
        btreeDb.commit();
    }

    @Override
    void deleteAll() throws IOException {
        // Setup new btree
        dictionary.clear();
        if (suffixDictionary != null) {
            suffixDictionary.clear();
        }
    }

    @Override
    void close() {

    }

    @Override
    public long getTotalDocCount() {
        throw new NotImplementedException();
    }

    public static class TermDataSerializer extends Serializer<TermData> {

        @Override
        public void serialize(DataOutput out, TermData termData) throws IOException {
            // Store fieldId:docFreq per field the term occurs in
            int numFields = termData.getFieldDocFrequency().size();
            out.writeInt(termData.getDocFrequency());

            out.writeByte(numFields);
            for (Map.Entry<Integer, Integer> field : termData.getFieldDocFrequency().entrySet()) {
                out.writeByte(field.getKey().byteValue());
                out.writeInt(field.getValue());
            }
            out.writeLong(termData.getPostingsIndex());
        }

        @Override
        public TermData deserialize(DataInput in, int available) throws IOException {
            int docFrequency = in.readInt();
            int numFields = in.readByte();
            HashMap<Integer, Integer> fieldDocFrequency = new HashMap<Integer, Integer>();
            for (int i = 0; i < numFields; i++) {
                int fieldId = in.readByte();
                int freq = in.readInt();
                fieldDocFrequency.put(fieldId, freq);
            }
            long postingsIndex = in.readLong();

            return new TermData(docFrequency, fieldDocFrequency, postingsIndex);
        }
    }

    public static class TermDataComparator implements Comparator<TermData> {
        public int compare(TermData o1, TermData o2) {
            return o1.getDocFrequency() - o2.getDocFrequency();
        }
    }

    public static class AtomicTermComparator implements Comparator<AtomicTerm> {
        public int compare(AtomicTerm o1, AtomicTerm o2) {
            return o1.compareTo(o2);
        }
    }

    public static class AtomicTermSerializer extends Serializer<AtomicTerm> {

        @Override
        public void serialize(DataOutput dataOutput, AtomicTerm atomicTerm) throws IOException {
            byte[] data = atomicTerm.serialize();
            dataOutput.writeInt(data.length);
            dataOutput.write(data);
        }

        @Override
        public AtomicTerm deserialize(DataInput dataInput, int i) throws IOException {
            int length = dataInput.readInt();
            byte[] data = new byte[length];
            dataInput.readFully(data);
            return new AtomicTerm(ByteBuffer.wrap(data));
        }
    }

}
