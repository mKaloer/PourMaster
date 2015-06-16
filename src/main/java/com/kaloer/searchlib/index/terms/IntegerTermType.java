package com.kaloer.searchlib.index.terms;

import com.kaloer.searchlib.index.AtomicTerm;

import java.nio.ByteBuffer;

/**
 * Created by mkaloer on 03/05/15.
 */
public class IntegerTermType implements TermType {

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

    public byte[] getBytes(Object value) {
        return ByteBuffer.allocate(4).putInt((Integer) value).array();
    }

    public Object readFromBytes(byte[] input) {
        return ByteBuffer.wrap(input).getInt();
    }

    public AtomicTerm toAtomic(Term term) {
        return new AtomicTerm(term.getValue(), AtomicTerm.DataType.DATA_TYPE_INT);
    }

    public int compare(Object a, Object b) {
        if(b instanceof Integer) {
            return ((Integer) a).compareTo((Integer) b);
        } else if(b instanceof String) {
            return Integer.toString((Integer) a).compareTo((String) b);
        } else {
            return -1;
        }
    }

    public boolean isPrefix(Object prefix, Object value) {
        if(prefix instanceof Integer && value instanceof Integer) {
            String prefixString = String.valueOf(prefix.toString());
            String valueString = String.valueOf(value.toString());
            return valueString.startsWith(prefixString);
        } else {
            return false;
        }
    }

    public boolean isSuffix(Object suffix, Object value) {
        if(suffix instanceof Integer && value instanceof Integer) {
            String suffixString = String.valueOf(suffix.toString());
            String valueString = String.valueOf(value.toString());
            return valueString.startsWith(suffixString);
        } else {
            return false;
        }
    }

    public Object reverse(Object value) {
        int val = (Integer) value;
        int reversed = 0;
        while (val > 0) {
            reversed = reversed * 10 + (val % 10);
            val = val / 10;
        }
        return reversed;
    }

    public static class IntegerTermSerializer extends TermSerializer<IntegerTerm> {

        @Override
        public TermType getTermType() {
            return IntegerTermType.getInstance();
        }
    }
}
