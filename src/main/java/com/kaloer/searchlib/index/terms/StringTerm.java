package com.kaloer.searchlib.index.terms;

/**
 * Created by mkaloer on 03/05/15.
 */
public class StringTerm extends Term {

    public StringTerm(String value) {
        super(value, StringTermType.getInstance());
    }

    public StringTerm(byte[] data) {
        super(data);
    }
}
