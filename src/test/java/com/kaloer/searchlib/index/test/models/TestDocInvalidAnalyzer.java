package com.kaloer.searchlib.index.test.models;

import com.kaloer.searchlib.index.annotations.Field;
import com.kaloer.searchlib.index.fields.IntegerFieldType;

/**
 * Created by mkaloer on 10/06/15.
 */
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
