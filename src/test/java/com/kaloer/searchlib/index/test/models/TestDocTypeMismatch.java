package com.kaloer.searchlib.index.test.models;

import com.kaloer.searchlib.index.annotations.Field;
import com.kaloer.searchlib.index.fields.IntegerFieldType;
import com.kaloer.searchlib.index.fields.StringFieldType;

/**
 * Created by mkaloer on 10/06/15.
 */
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
