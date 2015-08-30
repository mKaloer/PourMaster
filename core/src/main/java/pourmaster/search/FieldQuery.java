package pourmaster.search;

/**
 * Query for searching in a single field.
 */
public abstract class FieldQuery extends Query {

    private final String field;

    public FieldQuery(String field) {
        super();
        this.field = field;
    }

    public String getField() {
        return field;
    }

}
