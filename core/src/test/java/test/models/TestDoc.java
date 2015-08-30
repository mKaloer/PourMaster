package test.models;

import pourmaster.Analyzer;
import pourmaster.Token;
import pourmaster.annotations.Field;
import pourmaster.fields.IntegerFieldType;
import pourmaster.fields.StringFieldType;
import pourmaster.terms.IntegerTerm;
import pourmaster.terms.StringTerm;

import java.util.Iterator;

public class TestDoc {

    @Field(
            type = StringFieldType.class,
            indexed = true,
            stored = true,
            indexAnalyzer = SimpleStringAnalyzer.class

    )
    public String author;

    @Field(
            type = StringFieldType.class,
            indexAnalyzer = SimpleStringAnalyzer.class,
            indexed = true,
            stored = false
    )
    public String content;

    @Field(
            type = IntegerFieldType.class,
            stored = true,
            indexed = false
    )
    public int id;


    public static class SimpleStringAnalyzer extends Analyzer<String> {

        @Override
        public Iterator<Token> analyze(String value) {
            final String[] words = value.split(" ");
            return new Iterator<Token>() {
                int index = 0;

                public boolean hasNext() {
                    return index < words.length;
                }

                public Token next() {
                    return new Token(new StringTerm(words[index]), index++);
                }

                public void remove() {

                }
            };
        }
    }

    public static class SimpleIntAnalyzer extends Analyzer<Integer> {

        @Override
        public Iterator<Token> analyze(final Integer value) {
            return new Iterator<Token>() {
                int index = 0;

                public boolean hasNext() {
                    return index < 1;
                }

                public Token next() {
                    return new Token(new IntegerTerm(value), index++);
                }

                public void remove() {

                }
            };
        }
    }

}
