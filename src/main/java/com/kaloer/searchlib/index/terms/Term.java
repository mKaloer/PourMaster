package com.kaloer.searchlib.index.terms;

import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.fields.FieldType;

import java.lang.reflect.Type;

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

    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term term = (Term) o;

        return !(value != null ? !value.equals(term.value) : term.value != null);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    public int compareTo(Term<T, V> o) {
        return termType.compare(getValue(), o.getValue());
    }

    public byte[] serialize() {
        return termType.getBytes(getValue());
    }

    public static <T, V extends TermType<T>> Term<T,V> deserialize(byte[] in, V termType) throws IllegalAccessException, InstantiationException {
        T value = termType.readFromBytes(in);
        return new Term<T, V>(value, termType);
    }
}
