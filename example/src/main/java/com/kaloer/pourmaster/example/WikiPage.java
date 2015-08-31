package com.kaloer.pourmaster.example;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerStream;
import pourmaster.Analyzer;
import pourmaster.Token;
import pourmaster.annotations.Field;
import pourmaster.fields.StringFieldType;
import pourmaster.terms.IntegerTerm;
import pourmaster.terms.StringTerm;

import java.util.Iterator;

/**
 * Created by mkaloer on 18/07/15.
 */
public class WikiPage {

    @Field(
            type = StringFieldType.class,
            indexed = true,
            stored = true,
            indexAnalyzer = SimpleStringAnalyzer.class

    )
    public String title;

    @Field(
            type = StringFieldType.class,
            indexAnalyzer = SimpleStringAnalyzer.class,
            indexed = true,
            stored = false
    )
    public String content;

    @Field(
            type = StringFieldType.class,
            indexAnalyzer = SimpleStringAnalyzer.class,
            indexed = true,
            stored = true
    )
    public String categories;

    @Field(
            type = StringFieldType.class,
            stored = true,
            indexed = false
    )
    public String id;


    public static class SimpleStringAnalyzer extends Analyzer<String> {

        @Override
        public Iterator<Token> analyze(String value) {
            final String[] words = SimpleTokenizer.INSTANCE.tokenize(value);
            return new Iterator<Token>() {
                int index = 0;

                public boolean hasNext() {
                    return index < words.length;
                }

                public Token next() {
                    return new Token(new StringTerm(words[index].toLowerCase()), index++);
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
