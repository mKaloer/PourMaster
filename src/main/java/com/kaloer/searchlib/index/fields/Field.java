package com.kaloer.searchlib.index.fields;

import com.kaloer.searchlib.index.Token;
import com.kaloer.searchlib.index.pipeline.Pipeline;

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
    private Pipeline<T, Token> queryAnalysisPipeline;
    private Pipeline<T, Token> indexAnalysisPipeline;

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
