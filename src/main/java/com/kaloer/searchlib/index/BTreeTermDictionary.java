package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.terms.IntegerTerm;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.terms.TermType;
import org.apache.directory.mavibot.btree.BTree;
import org.apache.directory.mavibot.btree.RecordManager;
import org.apache.directory.mavibot.btree.exception.BTreeAlreadyManagedException;
import org.apache.directory.mavibot.btree.exception.KeyNotFoundException;
import org.apache.directory.mavibot.btree.serializer.*;
import sun.plugin.dom.exception.InvalidStateException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mkaloer on 12/04/15.
 */
public class BTreeTermDictionary extends TermDictionary {

    private final static String B_TREE_NAME = "termDictionary";

    private BTree<Term, TermData> dictionary;
    private RecordManager recordManager;

    public BTreeTermDictionary(String dictionaryFile, TermType termType) throws IOException, BTreeAlreadyManagedException {
        this(dictionaryFile, termType, -1);
    }

    public BTreeTermDictionary(String dictionaryFile, TermType termType, int pageSize) throws IOException, BTreeAlreadyManagedException {
        super();
        setTermType(termType);
        recordManager = new RecordManager(dictionaryFile);
        dictionary = recordManager.getManagedTree(B_TREE_NAME);
        // Create if it does not exist.
        if(dictionary == null) {
            dictionary = recordManager.addBTree(B_TREE_NAME, termType.getSerializer(), new TermDataSerializer(), false);
            if(pageSize > 0) {
                dictionary.setPageSize(pageSize);
            }
        }
    }

    @Override
    public TermData findTerm(Term term) throws IOException {
        if(getTermType() == null) {
            throw new InvalidStateException("TermType must be set!");
        }
        try {
            return dictionary.get(term);
        } catch (KeyNotFoundException e) {
            return null;
        }
    }

    @Override
    public void addTerm(Term term, TermData data) throws IOException {
        if(getTermType() == null) {
            throw new InvalidStateException("TermType must be set!");
        }
        dictionary.insert(term, data);
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
            for(Map.Entry<Integer, Integer> field : termData.getFieldDocFrequency().entrySet()) {
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
            for(int i = 0; i < numFields; i++) {
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
            HashMap<Integer, Integer> fieldDocFrequency  = new HashMap<Integer, Integer>();
            for(int i = 0; i < numFields; i++) {
                int fieldId = byteBuffer.get();
                int freq = byteBuffer.getInt();
                fieldDocFrequency .put(fieldId, freq);
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
}
