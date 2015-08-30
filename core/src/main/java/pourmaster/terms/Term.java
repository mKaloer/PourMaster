package pourmaster.terms;

import pourmaster.AtomicTerm;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;

/**
 * Represents a term in a field.
 */
public class Term implements Comparable<Term> {

    private final Object value;
    private final TermType termType;

    public Term(Object value, TermType termType) {
        this.value = value;
        this.termType = termType;
    }

    public Term(byte[] data) {
        throw new NotImplementedException();
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term term = (Term) o;

        if (value != null ? !value.equals(term.value) : term.value != null) return false;
        return !(termType != null ? !termType.equals(term.termType) : term.termType != null);

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (termType != null ? termType.hashCode() : 0);
        return result;
    }

    public int compareTo(Term o) {
        return termType.compare(getValue(), o.getValue());
    }

    public byte[] serialize() {
        byte[] value = termType.getBytes(getValue());
        ByteBuffer data = ByteBuffer.allocate(value.length + 4);
        data.put(value);
        return data.array();
    }

    public static Term deserialize(byte[] in, TermType termType) throws IllegalAccessException, InstantiationException {
        Object value = termType.readFromBytes(in);
        return new Term(value, termType);
    }

    public AtomicTerm toAtomic() {
        return getTermType().toAtomic(this);
    }

    public TermType getTermType() {
        return termType;
    }

    public boolean isPrefix(Object value) {
        if (getValue().getClass().equals(value.getClass())) {
            return getTermType().isPrefix(getValue(), value);
        } else {
            return false;
        }
    }
}
