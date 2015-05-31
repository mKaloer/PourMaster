package com.kaloer.searchlib.index.terms;

import com.kaloer.searchlib.index.AtomicTerm;

/**
 * Created by mkaloer on 03/05/15.
 */
public interface TermType {

    TermSerializer getSerializer();

    byte[] getBytes(Object value);

    Object readFromBytes(byte[] input);

    int compare(Object a, Object b);

    AtomicTerm toAtomic(Term term);
}
