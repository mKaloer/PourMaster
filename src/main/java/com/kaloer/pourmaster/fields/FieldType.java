package com.kaloer.pourmaster.fields;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents a field data type. Such a type is responsible for serializing and deserializing the data value.
 */
public interface FieldType<T> {

    void writeToOutput(DataOutput output, T value) throws IOException;

    T readFromInput(DataInput input) throws IOException;

    long getLength(T fieldValue);
}
