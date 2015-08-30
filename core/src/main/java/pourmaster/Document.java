package pourmaster;

import com.google.common.collect.ImmutableList;
import pourmaster.fields.FieldData;

import java.util.List;

/**
 * Represents data about an indexed document.
 */
public class Document {

    private static final int MAX_NUM_FIELDS = 255;

    private final ImmutableList<FieldData> fields;
    private final long documentId;
    private final int documentType;

    public Document(long documentId, int documentType, List<FieldData> fields) {
        this.documentId = documentId;
        this.documentType = documentType;
        if (fields.size() > 255) {
            throw new IllegalArgumentException(String.format("Too many fields in document. Maximum number of fields: %d", MAX_NUM_FIELDS));
        }
        this.fields = ImmutableList.copyOf(fields);
    }

    public List<FieldData> getFields() {
        return fields;
    }

    public long getDocumentId() {
        return documentId;
    }

    public int getDocumentType() {
        return documentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return this.documentId == ((Document) o).documentId;
    }

    @Override
    public int hashCode() {
        int result = (int) (documentId ^ (documentId >>> 32));
        result = 31 * result + documentType;
        return result;
    }
}
