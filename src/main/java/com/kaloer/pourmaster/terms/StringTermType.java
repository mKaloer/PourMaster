package com.kaloer.pourmaster.terms;

import com.kaloer.pourmaster.AtomicTerm;

/**
 * Term type for terms with string values.
 */
public class StringTermType implements TermType {

    private static StringTermType instance;

    public static StringTermType getInstance() {
        if (instance == null) {
            instance = new StringTermType();
        }
        return instance;
    }

    private StringTermType() { }

    public byte[] getBytes(Object value) {
        return ((String) value).getBytes();
    }

    public Object readFromBytes(byte[] input) {
        return new String(input);
    }

    public AtomicTerm toAtomic(Term term) {
        return new AtomicTerm(term.getValue(), AtomicTerm.DataType.DATA_TYPE_STRING);
    }

    public int compare(Object a, Object b) {
        if (b instanceof String) {
            return ((String) a).compareTo((String) b);
        } else if (b instanceof Integer) {
            return ((String) a).compareTo(Integer.toString((Integer) b));
        } else {
            return -1;
        }
    }

    public boolean isPrefix(Object prefix, Object value) {
        if (prefix instanceof String && value instanceof String) {
            return ((String) value).startsWith((String) prefix);
        } else {
            return false;
        }
    }

    public boolean isSuffix(Object suffix, Object value) {
        if (suffix instanceof String && value instanceof String) {
            return ((String) value).endsWith((String) suffix);
        } else {
            return false;
        }
    }

    public Object reverse(Object value) {
        return new StringBuilder((String) value).reverse().toString();
    }

}
