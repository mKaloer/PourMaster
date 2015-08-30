package pourmaster.terms;

/**
 * A String term.
 */
public class StringTerm extends Term {

    public StringTerm(String value) {
        super(value, StringTermType.getInstance());
    }

    public StringTerm(byte[] data) {
        super(data);
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
