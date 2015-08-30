package pourmaster;

import java.util.Iterator;

/**
 * An Analyzer converts an object (e.g. a string) into a number of tokens. In the process, it may
 * transform the data by tokenizing it, making it lowercase, stemming it etc.
 * @param <T> The type of input the analyzer supports.
 */
public abstract class Analyzer<T> {

    public abstract Iterator<Token> analyze(T value);

}
