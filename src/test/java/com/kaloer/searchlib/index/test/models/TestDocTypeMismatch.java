package com.kaloer.searchlib.index.test.models;

import com.kaloer.searchlib.index.annotations.Field;
import com.kaloer.searchlib.index.fields.StringFieldType;

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
