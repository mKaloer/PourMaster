package com.kaloer.searchlib.index.terms;

import java.nio.ByteBuffer;

/**
 * Created by mkaloer on 03/05/15.
 */
public class IntegerTermType implements TermType<Integer> {

    private static IntegerTermType instance;
    private IntegerTermSerializer integerTermSerializer = new IntegerTermSerializer();

    public static IntegerTermType getInstance() {
        if(instance == null) {
            instance = new IntegerTermType();
        }
        return instance;
    }

    private IntegerTermType() { }

    public TermSerializer getSerializer() {
        return integerTermSerializer;
    }

    public byte[] getBytes(Integer value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public Integer readFromBytes(byte[] input) {
        return ByteBuffer.wrap(input).getInt();
    }

    public int compare(Integer a, Integer b) {
        return a.compareTo(b);
    }

    public static class IntegerTermSerializer extends TermSerializer<IntegerTerm> {

        @Override
        public TermType getTermType() {
            return IntegerTermType.getInstance();
        }
    }
}
