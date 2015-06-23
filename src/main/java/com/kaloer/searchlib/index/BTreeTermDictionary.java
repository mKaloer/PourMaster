package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.terms.Term;
import org.apache.directory.mavibot.btree.BTree;
import org.apache.directory.mavibot.btree.RecordManager;
import org.apache.directory.mavibot.btree.Tuple;
import org.apache.directory.mavibot.btree.TupleCursor;
import org.apache.directory.mavibot.btree.exception.BTreeAlreadyManagedException;
import org.apache.directory.mavibot.btree.exception.KeyNotFoundException;
import org.apache.directory.mavibot.btree.serializer.AbstractElementSerializer;
import org.apache.directory.mavibot.btree.serializer.BufferHandler;
import org.apache.directory.mavibot.btree.serializer.IntSerializer;
import org.apache.directory.mavibot.btree.serializer.LongSerializer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Term dictionary based on a B-tree. This allows for efficient lookup even if the
 * dictionary does not fit into main memory.
 */
public class BTreeTermDictionary extends TermDictionary {

    private final static String B_TREE_NAME = "termDictionary";
    private final static String SUFFIX_B_TREE_NAME = "termDictionary_suffix";

    private BTree<AtomicTerm, TermData> dictionary;
    private BTree<AtomicTerm, TermData> suffixDictionary = null;
    private RecordManager recordManager;
    private boolean supportWildcardQuery;

    public BTreeTermDictionary(String dictionaryFile) throws IOException, BTreeAlreadyManagedException {
        this(dictionaryFile, -1, false);
    }

    public BTreeTermDictionary(String dictionaryFile, boolean supportWildcardQuery) throws IOException, BTreeAlreadyManagedException {
        this(dictionaryFile, -1, supportWildcardQuery);
    }

    public BTreeTermDictionary(String dictionaryFile, int pageSize, boolean supportWildcardQuery) throws IOException, BTreeAlreadyManagedException {
        super();
        this.supportWildcardQuery = supportWildcardQuery;
        recordManager = new RecordManager(dictionaryFile);
        dictionary = recordManager.getManagedTree(B_TREE_NAME);
        // Create if it does not exist.
        if (dictionary == null) {
            dictionary = recordManager.addBTree(B_TREE_NAME, new AtomicTermSerializer(), new TermDataSerializer(), false);
            if (pageSize > 0) {
                dictionary.setPageSize(pageSize);
            }
        }
        // Create suffix dictionary
        if (supportWildcardQuery) {
            suffixDictionary = recordManager.getManagedTree(SUFFIX_B_TREE_NAME);
            if (suffixDictionary == null) {
                suffixDictionary = recordManager.addBTree(SUFFIX_B_TREE_NAME, new AtomicTermSerializer(), new TermDataSerializer(), false);
                if (pageSize > 0) {
                    suffixDictionary.setPageSize(pageSize);
                }
            }
        }
    }

    @Override
    public TermData findTerm(Term term) throws IOException {
        try {
            return dictionary.get(term.toAtomic());
        } catch (KeyNotFoundException e) {
            return null;
        }
    }

    @Override
    public List<TermData> findTerm(Term prefix, Term suffix) throws IOException {
        Term reverseSuffix = null;
        if (suffix != null) {
            reverseSuffix = new Term(suffix.getTermType().reverse(suffix.getValue()), suffix.getTermType());
        }
        TupleCursor<AtomicTerm, TermData> cursor = null;
        try {
            // Hash table mapping postings pointers to term and data
            HashMap<Long, Tuple<AtomicTerm, TermData>> prefixMatches = null;
            if (prefix != null) {
                // Add all prefix matches to set
                prefixMatches = new HashMap<Long, Tuple<AtomicTerm, TermData>>();
                cursor = dictionary.browseFrom(prefix.toAtomic());
                // Cursor starts at matching node, but we have to look at this node as well.
                // So step back once if we can.
                if (cursor.hasPrevKey()) {
                    cursor.prevKey();
                }
                while (cursor.hasNextKey()) {
                    Tuple<AtomicTerm, TermData> termItem = cursor.nextKey();
                    // Check if actually a prefix
                    if (!prefix.isPrefix(termItem.getKey().getValue())) {
                        break;
                    }
                    prefixMatches.put(termItem.getValue().getPostingsIndex(), termItem);
                }
                cursor.close();
            }

            ArrayList<TermData> resultMatches = new ArrayList<TermData>();

            // If prefixMatches != null and its size == 0, there is no need to find suffixes (none will match)
            if (suffix != null && (prefixMatches == null || prefixMatches.size() > 0)) {
                cursor = suffixDictionary.browseFrom(reverseSuffix.toAtomic());
                // Cursor starts at matching node, but we have to look at this node as well.
                // So step back once if we can.
                if (cursor.hasPrevKey()) {
                    cursor.prevKey();
                }
                while (cursor.hasNextKey() && (prefixMatches == null || prefixMatches.size() > 0)) {
                    Tuple<AtomicTerm, TermData> termItem = cursor.nextKey();

                    // Check if actually a suffix
                    if (!reverseSuffix.isPrefix(termItem.getKey().getValue())) {
                        break;
                    }

                    // Add to results
                    if (prefix == null) {
                        resultMatches.add(termItem.getValue());
                    } else if (prefixMatches.containsKey(termItem.getValue().getPostingsIndex())) {
                        // If prefix not null and suffix not null, we must check that the term
                        // does not fully match either the prefix or suffix, e.g. "abba" should not
                        // match query with prefix = "abba", suffix = "ba".
                        if (!termItem.getKey().getValue().equals(prefix.getValue()) &&
                                !termItem.getKey().getValue().equals(suffix.getValue())) {
                            resultMatches.add(termItem.getValue());
                            // Remove from prefixMatches for faster containsKey() in next iterations.
                            prefixMatches.remove(termItem.getValue().getPostingsIndex());
                        }
                    }
                }
            } else {
                // Handle prefix-query (suffix is null, so add all with matching prefix)
                for (Tuple<AtomicTerm, TermData> match : prefixMatches.values()) {
                    resultMatches.add(match.getValue());
                }
            }

            return resultMatches;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void addTerm(Term term, TermData data) throws IOException {
        AtomicTerm atomicTerm = term.toAtomic();
        dictionary.insert(atomicTerm, data);
        if (supportWildcardQuery) {
            AtomicTerm reversedTerm = new AtomicTerm(term.getTermType().reverse(term.getValue()), atomicTerm.getDataType());
            suffixDictionary.insert(reversedTerm, data);
        }
    }

    @Override
    public long getTotalDocCount() {
        throw new NotImplementedException();
    }

    public static class TermDataSerializer extends AbstractElementSerializer<TermData> {

        public TermDataSerializer() {
            super(new TermDataComparator());
        }

        public byte[] serialize(TermData termData) {
            // Store fieldId:docFreq per field the term occurs in
            int numFields = termData.getFieldDocFrequency().size();
            ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + numFields * (1 + 4) + 8);
            buffer.putInt(termData.getDocFrequency());

            buffer.put((byte) numFields);
            for (Map.Entry<Integer, Integer> field : termData.getFieldDocFrequency().entrySet()) {
                buffer.put(field.getKey().byteValue());
                buffer.putInt(field.getValue());
            }
            buffer.putLong(termData.getPostingsIndex());
            return buffer.array();
        }

        public TermData deserialize(BufferHandler bufferHandler) throws IOException {
            int docFrequency = IntSerializer.deserialize(bufferHandler.read(4));
            int numFields = bufferHandler.read(1)[0];
            HashMap<Integer, Integer> fieldDocFrequency = new HashMap<Integer, Integer>();
            for (int i = 0; i < numFields; i++) {
                int fieldId = bufferHandler.read(1)[0];
                int freq = IntSerializer.deserialize(bufferHandler.read(4));
                fieldDocFrequency.put(fieldId, freq);
            }
            long postingsIndex = LongSerializer.deserialize(bufferHandler.read(8));

            return new TermData(docFrequency, fieldDocFrequency, postingsIndex);
        }

        public TermData deserialize(ByteBuffer byteBuffer) throws IOException {
            int docFrequency = byteBuffer.getInt();
            int numFields = byteBuffer.get();
            HashMap<Integer, Integer> fieldDocFrequency = new HashMap<Integer, Integer>();
            for (int i = 0; i < numFields; i++) {
                int fieldId = byteBuffer.get();
                int freq = byteBuffer.getInt();
                fieldDocFrequency.put(fieldId, freq);
            }
            long postingsIndex = byteBuffer.getLong();

            return new TermData(docFrequency, fieldDocFrequency, postingsIndex);
        }

        public TermData fromBytes(byte[] bytes) throws IOException {
            return deserialize(ByteBuffer.wrap(bytes));
        }

        public TermData fromBytes(byte[] bytes, int i) throws IOException {
            return deserialize(ByteBuffer.wrap(bytes, i, bytes.length - i));
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

    public static class AtomicTermSerializer extends AbstractElementSerializer<AtomicTerm> {

        public AtomicTermSerializer() {
            super(new AtomicTermComparator());
        }

        private InvertedIndex index;

        public byte[] serialize(AtomicTerm term) {
            return term.serialize();
        }

        public AtomicTerm deserialize(BufferHandler bufferHandler) throws IOException {
            return new AtomicTerm(ByteBuffer.wrap(bufferHandler.getBuffer()));
        }

        public AtomicTerm deserialize(ByteBuffer byteBuffer) throws IOException {
            return new AtomicTerm(byteBuffer);
        }

        public AtomicTerm fromBytes(byte[] bytes) throws IOException {
            return deserialize(ByteBuffer.wrap(bytes));
        }

        public AtomicTerm fromBytes(byte[] bytes, int i) throws IOException {
            return deserialize(ByteBuffer.wrap(bytes, i, bytes.length - i));
        }
    }

}
