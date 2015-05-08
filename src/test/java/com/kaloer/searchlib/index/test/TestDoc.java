package com.kaloer.searchlib.index.test;

import com.kaloer.searchlib.index.Token;
import com.kaloer.searchlib.index.annotations.Document;
import com.kaloer.searchlib.index.annotations.Field;
import com.kaloer.searchlib.index.fields.StringFieldType;
import com.kaloer.searchlib.index.pipeline.Pipeline;
import com.kaloer.searchlib.index.pipeline.Stage;
import com.kaloer.searchlib.index.terms.StringTerm;

import java.nio.channels.Pipe;
import java.util.StringTokenizer;

/**
 * Created by mkaloer on 03/05/15.
 */
@Document(name = "TestDocument")
public class TestDoc {

    @Field(
            type = StringFieldType.class,
            name = "author",
            indexed = true,
            indexAnalyzer = {Test.class}

    )
    public String author;

    @Field(
            type = StringFieldType.class,
            name = "hello",
            indexAnalyzer = {Test.class, Test1.class}
    )
    public String content;


    private class Test extends Stage<String, Token<StringTerm>> {

        @Override
        protected void produce(String input) throws InterruptedException {
            emit(new Token<StringTerm>(new StringTerm("hello"), 0));
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
