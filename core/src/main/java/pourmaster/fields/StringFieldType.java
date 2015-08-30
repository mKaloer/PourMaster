package pourmaster.fields;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A FieldType containing a String.
 */
public class StringFieldType implements FieldType<String> {

    public void writeToOutput(DataOutput output, String value) throws IOException {
        output.writeUTF(value);
    }

    public String readFromInput(DataInput input) throws IOException {
        return input.readUTF();
    }

    public long getLength(String fieldValue) {
        return fieldValue.length();
    }
}
