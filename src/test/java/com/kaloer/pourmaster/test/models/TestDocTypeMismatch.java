package com.kaloer.pourmaster.test.models;

import com.kaloer.pourmaster.annotations.Field;
import com.kaloer.pourmaster.fields.StringFieldType;

public class TestDocTypeMismatch {

    // Author type: int, field type: StringFieldType
    @Field(
            type = StringFieldType.class,
            indexed = true,
            stored = true,
            indexAnalyzer = TestDoc.SimpleStringAnalyzer.class
    )
    public int author;

}
