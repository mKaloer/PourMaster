package com.kaloer.pourmaster.terms;

import com.kaloer.pourmaster.BTreeTermDictionary;
import org.apache.directory.mavibot.btree.serializer.AbstractElementSerializer;
import org.apache.directory.mavibot.btree.serializer.BufferHandler;
import org.apache.directory.mavibot.btree.serializer.IntSerializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * Term serializer used for the {@link BTreeTermDictionary}.
 */
public abstract class TermSerializer<T extends Term> extends AbstractElementSerializer<Term> {

    public TermSerializer() {
        super(new TermComparator<Term>());
    }

    public abstract TermType getTermType();

    public final byte[] serialize(Term term) {
        byte[] termData = term.serialize();
        return ByteBuffer.allocate(termData.length + 4)
                .putInt(termData.length)
                .put(termData).array();
    }

    public final Term deserialize(BufferHandler bufferHandler) throws IOException {
        int length = IntSerializer.deserialize(bufferHandler.read(4));
        return this.deserialize(ByteBuffer.wrap(bufferHandler.read(length)));
    }

    public Term deserialize(ByteBuffer byteBuffer) throws IOException {
        int length = byteBuffer.getInt();
        byte[] data = new byte[length];
        byteBuffer.get(data);
        try {
            return Term.deserialize(data, getTermType());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Could not deserialize type", e.getCause());
        }
    }

    public Term fromBytes(byte[] bytes) throws IOException {
        return this.deserialize(ByteBuffer.wrap(bytes));
    }

    public Term fromBytes(byte[] bytes, int i) throws IOException {
        return this.deserialize(ByteBuffer.wrap(bytes, i, bytes.length - i));
    }

    public static class TermComparator<T extends Term> implements Comparator<T> {
        public int compare(T o1, T o2) {
            return o1.compareTo(o2);
        }
    }

}
