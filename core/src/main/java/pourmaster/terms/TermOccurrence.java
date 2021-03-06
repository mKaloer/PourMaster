package pourmaster.terms;

/**
 * Represents a single occurrence of a term in a field of a document.
 */
public class TermOccurrence {

    private final long position;
    private final int fieldId;

    public TermOccurrence(long position, int fieldId) {
        this.position = position;
        this.fieldId = fieldId;
    }

    public int getFieldId() {
        return fieldId;
    }

    public long getPosition() {
        return position;
    }
}
