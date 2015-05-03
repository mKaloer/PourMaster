package com.kaloer.searchlib.index.fields;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by mkaloer on 03/05/15.
 */
public interface FieldType<T> {

    void writeToOutput(DataOutput output, T value) throws IOException;

    T readFromInput(DataInput input) throws IOException;

}
