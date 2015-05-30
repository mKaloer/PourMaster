package com.kaloer.searchlib.index.terms;

/**
 * Created by mkaloer on 03/05/15.
 */
public class IntegerTerm extends Term<Integer, IntegerTermType> {

    public IntegerTerm(Integer value) {
        super(value, IntegerTermType.getInstance());
    }

    public IntegerTerm(byte[] data) {
        super(data);
    }
}
