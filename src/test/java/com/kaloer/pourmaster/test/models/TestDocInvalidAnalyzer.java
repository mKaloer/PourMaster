package com.kaloer.pourmaster.test.models;

import com.kaloer.pourmaster.annotations.Field;
import com.kaloer.pourmaster.fields.IntegerFieldType;

public class TestDocInvalidAnalyzer {

    // Author type: int, analyzer type: String
    @Field(
            type = IntegerFieldType.class,
            indexed = true,
            stored = true,
            indexAnalyzer = TestDoc.SimpleStringAnalyzer.class
    )
    public int author;

}
