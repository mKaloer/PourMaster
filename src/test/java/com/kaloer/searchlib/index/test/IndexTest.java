package com.kaloer.searchlib.index.test;

import com.kaloer.searchlib.index.fields.StringFieldType;
import com.kaloer.searchlib.index.terms.StringTerm;
import com.kaloer.searchlib.index.terms.StringTermType;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.*;
import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.pipeline.Pipeline;
import com.kaloer.searchlib.index.pipeline.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.directory.mavibot.btree.exception.BTreeAlreadyManagedException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class IndexTest {

    private final File tmpDir = new File("tmp");
    private final File indexDir = new File("idx");

    @Before
    public void setup() {
        tmpDir.mkdirs();
        indexDir.mkdirs();
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(indexDir);
            FileUtils.deleteDirectory(tmpDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InvertedIndex createIndex() throws IOException, ReflectiveOperationException, BTreeAlreadyManagedException {
        IndexConfig conf = null;
        try {
            conf = new IndexConfig().
                    setDocumentIndex(new SequentialDocumentIndex("idx/docs.idx", "idx/docs_fields.idx", "idx/fields.db"))
                    .setPostings(new SequentialPostings("idx/postings.db"))
                    .setTermDictionary(new BTreeTermDictionary("idx/dict.db", StringTermType.getInstance()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new InvertedIndex(conf);
    }

    @Test
    public void testOneResult() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = createIndex();

        Document d1 = new Document();
        Field f1 = new Field();
        f1.setFieldType(new StringFieldType());
        f1.setFieldId(1);
        f1.setFieldValue("Hello World");
        Pipeline<String, Token<StringTerm>> pipeline = new Pipeline<String, Token<StringTerm>>(new Stage<String, Token<StringTerm>>() {

            @Override
            protected void produce(String input) throws InterruptedException {
                int i = 1;
                for(String s : input.split(" ")) {
                    Token t = new Token(new StringTerm(s), i);
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
        try {
            index.indexDocuments(new DocumentStream() {
                int index = 0;

                @Override
                protected boolean hasNextDocument() {
                    return docs.size() > index;
                }

                @Override
                protected FieldStream nextDocument() {
                    return new FieldStream(docs.get(index++));
                }
            }, tmpDir);
            List<Document> d = index.findDocuments(new StringTerm("Hello"));
            Assert.assertEquals("Expected one result", 1, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEmptyResult() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        Document d1 = new Document();
        Field f1 = new Field();
        f1.setFieldType(new StringFieldType());
        f1.setFieldId(1);
        f1.setFieldValue("Hello World");
        Pipeline<String, Token<StringTerm>> pipeline = new Pipeline<String, Token<StringTerm>>(new Stage<String, Token<StringTerm>>() {

            @Override
            protected void produce(String input) throws InterruptedException {
                int i = 1;
                for(String s : input.split(" ")) {
                    Token t = new Token(new StringTerm(s), i);
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
        try {
            File tmpDir = new File("tmp");
            tmpDir.mkdirs();
            index.indexDocuments(new DocumentStream() {
                int index = 0;

                @Override
                protected boolean hasNextDocument() {
                    return docs.size() > index;
                }

                @Override
                protected FieldStream nextDocument() {
                    return new FieldStream(docs.get(index++));
                }
            }, tmpDir);
            List<Document> d = index.findDocuments(new StringTerm("Foo"));
            Assert.assertEquals("Expected zero results", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
