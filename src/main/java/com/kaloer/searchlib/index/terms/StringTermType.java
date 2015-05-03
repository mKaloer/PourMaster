package com.kaloer.searchlib.index.terms;

/**
 * Created by mkaloer on 03/05/15.
 */
public class StringTermType implements TermType<String> {

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

    public byte[] getBytes(String value) {
        return value.getBytes();
    }

    public String readFromBytes(byte[] input) {
        return new String(input);
    }

    public int compare(String a, String b) {
        return a.compareTo(b);
    }

    public static class StringTermSerializer extends TermSerializer<StringTerm> {

        @Override
        public TermType getTermType() {
            return StringTermType.getInstance();
        }
    }
}
