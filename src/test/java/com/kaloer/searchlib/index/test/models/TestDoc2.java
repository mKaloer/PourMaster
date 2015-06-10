package com.kaloer.searchlib.index.test.models;

import com.kaloer.searchlib.index.Analyzer;
import com.kaloer.searchlib.index.Token;
import com.kaloer.searchlib.index.annotations.Field;
import com.kaloer.searchlib.index.fields.IntegerFieldType;
import com.kaloer.searchlib.index.fields.StringFieldType;
import com.kaloer.searchlib.index.pipeline.Pipeline;
import com.kaloer.searchlib.index.pipeline.Stage;
import com.kaloer.searchlib.index.terms.StringTerm;

import java.util.Iterator;

/**
 * Created by mkaloer on 03/05/15.
 */
public class TestDoc2 {

    @Field(
            type = StringFieldType.class,
            indexed = true,
            stored = true,
            indexAnalyzer = Test.class

    )
    public String author2;

    @Field(
            type = StringFieldType.class,
            stored = true,
            indexAnalyzer = Test.class
    )
    public String content2;

    @Field(
            type = IntegerFieldType.class,
            stored = true,
            indexed = true,
            indexAnalyzer = TestDoc.SimpleIntAnalyzer.class
    )
    public int id2;


    public static class Test extends Analyzer<String> {

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

    private class Test1 extends Stage<String, Token<StringTerm>> {

        @Override
        protected void produce(String input) throws InterruptedException {
            emit(new Token<StringTerm>(new StringTerm("hello"), 0));
        }
    }

    private Pipeline<String, Token<StringTerm>> p = new Pipeline<String, Token<StringTerm>>(new Stage<String, Token<StringTerm>>() {
        @Override
        protected void produce(String input) throws InterruptedException {
            emit(new Token<StringTerm>(new StringTerm("hello"), 0));
        }
    }).append(new Stage<Token<StringTerm>, Token<StringTerm>>() {
        @Override
        protected void produce(Token<StringTerm> input) throws InterruptedException {
            emit(new Token<StringTerm>(new StringTerm("hello"), 0));
            emit(new Token<StringTerm>(new StringTerm("world"), 0));
        }
    }).append(new Stage<Token<StringTerm>, Token<StringTerm>>() {
        @Override
        protected void produce(Token<StringTerm> input) throws InterruptedException {
            emit(new Token<StringTerm>(new StringTerm("hello"), 0));
        }
    });

}
