package com.kaloer.pourmaster.terms;

/**
 * Term for storing integers.
 */
public class IntegerTerm extends Term {

    public IntegerTerm(Integer value) {
        super(value, IntegerTermType.getInstance());
    }

    public IntegerTerm(byte[] data) {
        super(data);
    }
}
