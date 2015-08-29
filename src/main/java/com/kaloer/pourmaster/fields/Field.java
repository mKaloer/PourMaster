package com.kaloer.pourmaster.fields;

/**
 * A field in a document, such as 'content', 'author', etc. Each field has an associated data type.
 * @param <T> The data type of the field data.
 * @param <V> The FieldType used to represent the data type.
 */
public class Field<T, V extends FieldType<T>> {

    private int fieldId;
    private V fieldType;
    private boolean isStored;
    private boolean isIndexed;
    private String fieldName;

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isStored() {
        return isStored;
    }

    public boolean isIndexed() {
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

    public void setFieldType(V fieldType) {
        this.fieldType = fieldType;
    }

    public void setIsStored(boolean isStored) {
        this.isStored = isStored;
    }

    public void setIsIndexed(boolean isIndexed) {
        this.isIndexed = isIndexed;
    }

}
