package com.kaloer.pourmaster.util;

import java.io.IOException;

/**
 * Iterator interface which allows IOExceptions to be thrown in next().
 */
public interface IOIterator<T> {

    boolean hasNext();

    T next() throws IOException;

}
