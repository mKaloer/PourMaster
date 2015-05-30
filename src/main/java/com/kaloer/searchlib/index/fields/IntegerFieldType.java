package com.kaloer.searchlib.index.fields;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by mkaloer on 03/05/15.
 */
public class IntegerFieldType implements FieldType<Integer> {

    public void writeToOutput(DataOutput output, Integer value) throws IOException {
        output.writeInt(value);
    }

    public Integer readFromInput(DataInput input) throws IOException {
        return input.readInt();
    }

    public long getLength(Integer fieldValue) {
        return 1;
    }
}
