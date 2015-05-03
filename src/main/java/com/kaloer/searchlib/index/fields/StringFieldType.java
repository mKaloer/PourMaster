package com.kaloer.searchlib.index.fields;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by mkaloer on 03/05/15.
 */
public class StringFieldType implements FieldType<String> {

    public void writeToOutput(DataOutput output, String value) throws IOException {
        output.writeUTF(value);
    }

    public String readFromInput(DataInput input) throws IOException {
        return input.readUTF();
    }
}
