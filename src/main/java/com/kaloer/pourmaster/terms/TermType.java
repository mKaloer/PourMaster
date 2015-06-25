package com.kaloer.pourmaster.terms;

import com.kaloer.pourmaster.AtomicTerm;

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
