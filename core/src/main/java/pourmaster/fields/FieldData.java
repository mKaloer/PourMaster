package pourmaster.fields;

import pourmaster.FieldInfoStore;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents the length and value of a field.
 */
public class FieldData {

    private Field field;
    private Object value;
    private long length;

    public static FieldData createFromData(DataInput input, FieldInfoStore fieldInfoStore) throws IOException {
        // Read field info.
        int fieldId = input.readUnsignedShort();
        Field f = fieldInfoStore.getFieldById(fieldId);
        FieldData fieldData = new FieldData();
        fieldData.setField(f);
        // Read value if stored, otherwise read length
        if (f.isStored()) {
            fieldData.setValue(f.getFieldType().readFromInput(input));
        }
        return fieldData;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public void setValue(Object value) {
        this.value = value;
        setLength(getField().getFieldType().getLength(value));
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void writeToOutput(DataOutput output) throws IOException {
        output.writeShort(field.getFieldId());
        // Store value if stored
        if (field.isStored()) {
            field.getFieldType().writeToOutput(output, value);
        }
    }

    public Field getField() {
        return field;
    }

    public long getLength() {
        if (field.isStored()) {
            return field.getFieldType().getLength(value);
        } else {
            return length;
        }
    }

    public Object getValue() {
        return value;
    }
}
