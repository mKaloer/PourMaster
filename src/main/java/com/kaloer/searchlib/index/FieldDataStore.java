package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.fields.FieldList;

import java.io.IOException;

/**
 * Created by mkaloer on 13/04/15.
 */
public abstract class FieldDataStore {

    public abstract FieldList getFields(long index) throws IOException;

    public abstract long appendFields(FieldList fields) throws IOException, ReflectiveOperationException;

    public abstract Field getField(String name) throws IOException;

    public abstract Field getField(int id) throws IOException;

}
