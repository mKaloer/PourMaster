package com.kaloer.pourmaster.fields;

import java.util.List;

/**
 * List of fields corresponding to a specific document.
 */
public class FieldList {

    private List<FieldData> fieldData;
    private int docTypeId;

    public FieldList(List<FieldData> fieldData, int docTypeId) {
        this.fieldData = fieldData;
        this.docTypeId = docTypeId;
    }

    public List<FieldData> getFieldData() {
        return fieldData;
    }

    public int getDocTypeId() {
        return docTypeId;
    }
}
