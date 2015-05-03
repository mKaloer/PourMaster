package com.kaloer.searchlib.index.fields;

import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.Token;
import com.kaloer.searchlib.index.pipeline.Pipeline;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by mkaloer on 13/04/15.
 */
public class Field<T, V extends FieldType<T>> {

    private int fieldId;
    private V fieldType;
    private boolean isStored;
    private boolean isIndexed;
    private String fieldName;
    private T fieldValue;
    private Pipeline<T, Token> queryAnalysisPipeline;
    private Pipeline<T, Token> indexAnalysisPipeline;

    public static Field createFromData(DataInput input, FieldTypeStore typeStore) throws IOException {
        Field f = new Field();
        // Read field info.
        int fieldId = input.readUnsignedShort();
        int fieldType = input.readUnsignedByte();
        f.setFieldId(fieldId);
        f.setFieldType(typeStore.findTypeById(fieldType));
        f.setFieldValue(f.getFieldType().readFromInput(input));
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

    public T getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(T fieldValue) {
        this.fieldValue = fieldValue;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(V fieldType) {
        this.fieldType = fieldType;
    }

    public void writeToOutput(DataOutput output, FieldTypeStore typeStore) throws IOException, ReflectiveOperationException {
        output.writeShort(getFieldId());
        int typeId = typeStore.getOrCreateTypeId(getFieldType());
        output.writeByte(typeId);
        fieldType.writeToOutput(output, getFieldValue());
    }
}
