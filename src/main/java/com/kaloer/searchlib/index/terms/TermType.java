package com.kaloer.searchlib.index.terms;

/**
 * Created by mkaloer on 03/05/15.
 */
public interface TermType<T> {

    TermSerializer getSerializer();

    byte[] getBytes(T value);

    T readFromBytes(byte[] input);

    int compare(T a, T b);

}
