package com.kaloer.searchlib.index.test.models;

import com.kaloer.searchlib.index.Analyzer;
import com.kaloer.searchlib.index.Token;
import com.kaloer.searchlib.index.annotations.Field;
import com.kaloer.searchlib.index.fields.IntegerFieldType;
import com.kaloer.searchlib.index.fields.StringFieldType;
import com.kaloer.searchlib.index.terms.IntegerTerm;
import com.kaloer.searchlib.index.terms.StringTerm;

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
            indexAnalyzer = SimpleIntAnalyzer.class,
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
