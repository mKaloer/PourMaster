package com.kaloer.searchlib.index.fields;

import com.kaloer.searchlib.index.FieldInfoStore;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.Token;
import com.kaloer.searchlib.index.pipeline.Pipeline;
import sun.reflect.FieldInfo;

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
