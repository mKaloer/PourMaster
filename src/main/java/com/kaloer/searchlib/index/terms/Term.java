package com.kaloer.searchlib.index.terms;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;

/**
 * Created by mkaloer on 12/04/15.
 */
public class Term<T, V extends TermType<T>> implements Comparable<Term<T, V>> {

    private T value;
    private V termType;

    public Term(T value, V termType) {
        this.value = value;
        this.termType = termType;
    }

    public Term(byte[] data) {
        throw new NotImplementedException();
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term<?, ?> term = (Term<?, ?>) o;

        if (value != null ? !value.equals(term.value) : term.value != null) return false;
        return !(termType != null ? !termType.equals(term.termType) : term.termType != null);

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (termType != null ? termType.hashCode() : 0);
        return result;
    }

    public int compareTo(Term<T, V> o) {
        return termType.compare(getValue(), o.getValue());
    }

    public byte[] serialize() {
        byte[] value = termType.getBytes(getValue());
        ByteBuffer data = ByteBuffer.allocate(value.length + 4);
        data.put(value);
        return data.array();
    }

    public static <T, V extends TermType<T>> Term<T,V> deserialize(byte[] in, V termType) throws IllegalAccessException, InstantiationException {
        T value = termType.readFromBytes(in);
        return new Term<T, V>(value, termType);
    }


}
