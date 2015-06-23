package com.kaloer.searchlib.index.terms;

/**
 * A String term.
 */
public class StringTerm extends Term {

    public StringTerm(String value) {
        super(value, StringTermType.getInstance());
    }

    public StringTerm(byte[] data) {
        super(data);
    }
}
