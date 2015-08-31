package pourmaster;

import pourmaster.exceptions.FieldNotFoundException;
import pourmaster.fields.FieldList;
import pourmaster.fields.Field;

import java.io.IOException;

/**
 * Data store for storing and retrieving field data given an field data index.
 */
public abstract class FieldDataStore {

    public abstract FieldList getFields(long index) throws IOException;

    public abstract long appendFields(FieldList fields) throws IOException, ReflectiveOperationException;

    public abstract Field getField(String name) throws FieldNotFoundException;

    public abstract int getFieldCount() throws IOException;

    public abstract Field getField(int id) throws IOException;

    public abstract void deleteAll() throws IOException;
}
