package com.kaloer.pourmaster.test.models;

import com.kaloer.pourmaster.annotations.Field;
import com.kaloer.pourmaster.fields.IntegerFieldType;

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
