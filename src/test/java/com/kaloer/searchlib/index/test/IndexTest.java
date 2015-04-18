package com.kaloer.searchlib.index.test;

import com.kaloer.searchlib.index.*;
import com.kaloer.searchlib.index.Field;
import com.kaloer.searchlib.index.pipeline.Pipeline;
import com.kaloer.searchlib.index.pipeline.Stage;
import org.apache.directory.mavibot.btree.exception.BTreeAlreadyManagedException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class IndexTest {

    InvertedIndex index;

    @After
    public void tearDown() {
        if(index != null) {
            // TODO: Delete index
        }
    }


    @Test
    public void testTest() throws BTreeAlreadyManagedException {
        IndexConfig conf = null;
        try {
            conf = new IndexConfig().
                    setDocumentIndex(new SequentialDocumentIndex("docs.idx", "docs_fields.idx"))
                    .setPostings(new SequentialPostings("posings.db"))
                    .setTermDictionary(new BTreeTermDictionary("dict.db"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final InvertedIndex index = new InvertedIndex(conf);

        Document d1 = new Document();
        Field f1 = new Field();
        f1.setFieldValue("Hello world");
        Pipeline<String, Token> pipeline = new Pipeline<String, Token>(new Stage<String, Token>() {
            @Override
            protected void produce(String input) throws InterruptedException {
                int i = 1;
                for(String s : input.split(" ")) {
                    Token t = new Token(s, i);
                    emit(t);
                    i++;
                }
            }
        });
        f1.setIndexAnalysisPipeline(pipeline);
        ArrayList<Field> fields = new ArrayList<Field>();
        fields.add(f1);
        d1.setFields(fields);
        final ArrayList<Document> docs = new ArrayList<Document>();
        docs.add(d1);
        index.indexDocuments(new DocumentStream() {
            int index = 0;

            @Override
            protected boolean hasNextDocument() {
                return docs.size() > index;
            }

            @Override
            protected FieldStream nextDocument() {
                return new FieldStream(docs.get(index++).getFields());
            }
        });
        try {
            List<Document> d = index.findDocuments("Hello");
            Assert.assertEquals("Expected empty result set", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
