package test.models;

import pourmaster.annotations.Field;
import pourmaster.fields.IntegerFieldType;

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
