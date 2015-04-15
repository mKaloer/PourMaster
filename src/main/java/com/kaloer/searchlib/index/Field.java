package com.kaloer.searchlib.index;

import java.io.DataInput;
import java.io.IOException;

/**
 * Created by mkaloer on 13/04/15.
 */
public class Field {

    private int fieldId;
    private FieldType fieldType;
    private boolean isStored;
    private boolean isIndexed;
    private String fieldName;

    protected static Field createFromData(DataInput input) throws IOException {
        Field f = new Field();
        // Read field info.
        int fieldId = input.readUnsignedShort();
        int fieldType = input.readUnsignedByte();
        f.setFieldId(fieldId);
        f.setFieldType(fieldType);

        // Read data
        // TODO: read data
        return f;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean getIsStored() {
        return isStored;
    }

    public boolean getIsIndexed() {
        return isIndexed;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }


    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public void setFieldType(int fieldTypeIdentifier) {
        if(fieldTypeIdentifier == FieldType.LONG_TEXT.identifier) {
            setFieldType(FieldType.LONG_TEXT);
        } else if(fieldTypeIdentifier == FieldType.SHORT_TEXT.identifier) {
            setFieldType(FieldType.SHORT_TEXT);
        } else if(fieldTypeIdentifier == FieldType.INTEGER.identifier) {
            setFieldType(FieldType.INTEGER);
        } else if(fieldTypeIdentifier == FieldType.LONG.identifier) {
                setFieldType(FieldType.LONG);
        } else if(fieldTypeIdentifier == FieldType.DOUBLE.identifier) {
            setFieldType(FieldType.DOUBLE);
        } else if(fieldTypeIdentifier == FieldType.FLOAT.identifier) {
            setFieldType(FieldType.FLOAT);
        } else if(fieldTypeIdentifier == FieldType.BINARY.identifier) {
            setFieldType(FieldType.BINARY);
        } else {
            throw new IllegalArgumentException(String.format("Invalid field type identifier (no known type): %d", fieldTypeIdentifier));
        }
    }

    public enum FieldType {
        LONG_TEXT (1),
        SHORT_TEXT (2),
        INTEGER (3),
        LONG (4),
        DOUBLE (5),
        FLOAT (6),
        BINARY (7);

        private int identifier;

        FieldType(int identifier) {
            this.identifier = identifier;
        }

        protected int getIdentifier() {
            return identifier;
        }
    }
}
