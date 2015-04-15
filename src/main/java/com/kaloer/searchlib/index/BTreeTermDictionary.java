package com.kaloer.searchlib.index;

import org.apache.directory.mavibot.btree.BTree;
import org.apache.directory.mavibot.btree.RecordManager;
import org.apache.directory.mavibot.btree.exception.BTreeAlreadyManagedException;
import org.apache.directory.mavibot.btree.exception.KeyNotFoundException;
import org.apache.directory.mavibot.btree.serializer.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * Created by mkaloer on 12/04/15.
 */
public class BTreeTermDictionary extends TermDictionary {

    private final static String B_TREE_NAME = "termDictionary";

    private BTree<String, TermData> dictionary;
    private RecordManager recordManager;

    public BTreeTermDictionary(String dictionaryFile) throws IOException, BTreeAlreadyManagedException {
        this(dictionaryFile, -1);
    }

    public BTreeTermDictionary(String dictionaryFile, int pageSize) throws IOException, BTreeAlreadyManagedException {
        recordManager = new RecordManager(dictionaryFile);
        dictionary = recordManager.getManagedTree(B_TREE_NAME);
        // Create if it does not exist.
        if(dictionary == null) {
            dictionary = recordManager.addBTree(B_TREE_NAME, StringSerializer.INSTANCE, new TermDataSerializer(), false);
            if(pageSize > 0) {
                dictionary.setPageSize(pageSize);
            }
        }
    }

    @Override
    public TermData findTerm(String term) throws IOException {
        try {
            return dictionary.get(term);
        } catch (KeyNotFoundException e) {
            return null;
        }
    }

    public void addTerm(String term) throws IOException {
        TermData data = new TermData(10, 11, 12);
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
            byte[] data = ByteBuffer.allocate(8 * 4)
                    .putInt(termData.getDocFrequency())
                    .putLong(termData.getTermFrequency())
                    .putLong(termData.getPostingsIndex()).array();
            return data;
        }

        public TermData deserialize(BufferHandler bufferHandler) throws IOException {
            int docFrequency = IntSerializer.deserialize(bufferHandler.read(4));
            long termFrequency = LongSerializer.deserialize(bufferHandler.read(8));
            long postingsIndex = LongSerializer.deserialize(bufferHandler.read(8));

            return new TermData(docFrequency, termFrequency, postingsIndex);
        }

        public TermData deserialize(ByteBuffer byteBuffer) throws IOException {
            int docFrequency = byteBuffer.getInt();
            long termFrequency = byteBuffer.getLong();
            long postingsIndex = byteBuffer.getLong();

            return new TermData(docFrequency, termFrequency, postingsIndex);
        }

        public TermData fromBytes(byte[] bytes) throws IOException {
            int docFrequency = IntSerializer.deserialize(bytes, 0);
            long termFrequency = LongSerializer.deserialize(bytes, 8);
            long postingsIndex = LongSerializer.deserialize(bytes, 16);

            return new TermData(docFrequency, termFrequency, postingsIndex);
        }

        public TermData fromBytes(byte[] bytes, int i) throws IOException {
            int docFrequency = IntSerializer.deserialize(bytes, i);
            long termFrequency = LongSerializer.deserialize(bytes, i+8);
            long postingsIndex = LongSerializer.deserialize(bytes, i+16);

            return new TermData(docFrequency, termFrequency, postingsIndex);
        }
    }

    public static class TermDataComparator implements Comparator<TermData> {
        public int compare(TermData o1, TermData o2) {
            return (int) (o1.getDocFrequency() - o2.getDocFrequency());
        }
    }
}
