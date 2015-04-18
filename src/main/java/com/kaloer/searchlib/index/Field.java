package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.pipeline.Pipeline;

import java.io.DataInput;
import java.io.IOException;

/**
 * Created by mkaloer on 13/04/15.
 */
public class Field<T> {

    private int fieldId;
    private FieldType fieldType;
    private boolean isStored;
    private boolean isIndexed;
    private String fieldName;
    private Object fieldValue;
    private Pipeline<T, Token> queryAnalysisPipeline;
    private Pipeline<T, Token> indexAnalysisPipeline;

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

    public void setIndexAnalysisPipeline(Pipeline<T, Token> indexAnalysisPipeline) {
        this.indexAnalysisPipeline = indexAnalysisPipeline;
    }

    public Pipeline<T, Token> getIndexAnalysisPipeline() {
        return indexAnalysisPipeline;
    }

    public void setQueryAnalysisPipeline(Pipeline<T, Token> queryAnalysisPipeline) {
        this.queryAnalysisPipeline = queryAnalysisPipeline;
    }

    public Pipeline<T, Token> getQueryAnalysisPipeline() {
        return queryAnalysisPipeline;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
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
