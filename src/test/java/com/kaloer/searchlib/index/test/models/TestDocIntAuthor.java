package com.kaloer.searchlib.index.test.models;

import com.kaloer.searchlib.index.annotations.Field;
import com.kaloer.searchlib.index.fields.IntegerFieldType;

/**
 * Created by mkaloer on 10/06/15.
 */
public class TestDocIntAuthor {

    // This has author as an int (in contrast to TestDoc and TestDoc2 which have strings)
    @Field(
            type = IntegerFieldType.class,
            indexed = true,
            stored = true,
            indexAnalyzer = TestDoc.SimpleIntAnalyzer.class
    )
    public int author;
}
