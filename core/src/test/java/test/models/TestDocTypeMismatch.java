package test.models;

import pourmaster.annotations.Field;
import pourmaster.fields.StringFieldType;

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
