package com.kaloer.searchlib.index.terms;

import com.kaloer.searchlib.index.AtomicTerm;

/**
 * Created by mkaloer on 03/05/15.
 */
public class StringTermType implements TermType {

    private static StringTermType instance;
    private StringTermSerializer stringTermSerializer = new StringTermSerializer();

    public static StringTermType getInstance() {
        if(instance == null) {
            instance = new StringTermType();
        }
        return instance;
    }

    private StringTermType() { }

    public TermSerializer getSerializer() {
        return stringTermSerializer;
    }

    public byte[] getBytes(Object value) {
        return ((String)value).getBytes();
    }

    public Object readFromBytes(byte[] input) {
        return new String(input);
    }

    public AtomicTerm toAtomic(Term term) {
        return new AtomicTerm(term.getValue(), AtomicTerm.DataType.DATA_TYPE_STRING);
    }

    public int compare(Object a, Object b) {
        if(b instanceof String) {
            return ((String) a).compareTo((String) b);
        } else if(b instanceof Integer) {
            return ((String) a).compareTo(Integer.toString((Integer) b));
        } else {
            return -1;
        }
    }

    public static class StringTermSerializer extends TermSerializer<StringTerm> {

        @Override
        public TermType getTermType() {
            return StringTermType.getInstance();
        }
    }
}
