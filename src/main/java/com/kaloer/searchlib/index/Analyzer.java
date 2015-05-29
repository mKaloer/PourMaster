package com.kaloer.searchlib.index;

import java.util.Iterator;

/**
 * Created by mkaloer on 10/05/15.
 */
public abstract class Analyzer<T> {

    public abstract Iterator<Token> analyze(T value);

}
