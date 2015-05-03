package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.Field;

import java.io.IOException;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public abstract class FieldDataStore {

    public abstract List<Field> getFields(long index) throws IOException;

    public abstract long appendFields(List<Field> fields) throws IOException, ReflectiveOperationException;

}
