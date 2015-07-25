package com.kaloer.pourmaster.test;

import com.kaloer.pourmaster.Analyzer;
import com.kaloer.pourmaster.Token;
import com.kaloer.pourmaster.annotations.Field;
import com.kaloer.pourmaster.fields.IntegerFieldType;
import com.kaloer.pourmaster.fields.StringFieldType;
import com.kaloer.pourmaster.terms.IntegerTerm;
import com.kaloer.pourmaster.terms.StringTerm;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

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
