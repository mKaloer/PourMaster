package com.kaloer.pourmaster.util;

/**
 * Simpe 2-tuple class.
 *
 * @param <T1> Type of first element.
 * @param <T2> Type of second element.
 */
public class Tuple<T1, T2> {

    private final T1 first;
    private final T2 second;

    public static <T1, T2> Tuple<T1, T2> of(T1 first, T2 second) {
        return new Tuple<T1, T2>(first, second);
    }

    public Tuple(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?> tuple = (Tuple<?, ?>) o;

        if (first != null ? !first.equals(tuple.first) : tuple.first != null) return false;
        return !(second != null ? !second.equals(tuple.second) : tuple.second != null);

    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}
