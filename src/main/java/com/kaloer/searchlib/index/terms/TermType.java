package com.kaloer.searchlib.index.terms;

import com.kaloer.searchlib.index.AtomicTerm;

/**
 * Interface used by every type of term.
 */
public interface TermType {

    TermSerializer getSerializer();

    byte[] getBytes(Object value);

    Object readFromBytes(byte[] input);

    int compare(Object a, Object b);

    AtomicTerm toAtomic(Term term);

    boolean isPrefix(Object prefix, Object value);

    boolean isSuffix(Object suffix, Object value);

    Object reverse(Object value);
}
