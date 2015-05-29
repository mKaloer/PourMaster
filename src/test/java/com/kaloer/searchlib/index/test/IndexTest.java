package com.kaloer.searchlib.index.test;

import com.kaloer.searchlib.index.fields.FieldData;
import com.kaloer.searchlib.index.fields.StringFieldType;
import com.kaloer.searchlib.index.search.MultiTermQuery;
import com.kaloer.searchlib.index.search.RankedDocument;
import com.kaloer.searchlib.index.search.TermQuery;
import com.kaloer.searchlib.index.terms.StringTerm;
import com.kaloer.searchlib.index.terms.StringTermType;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
                    setDocumentIndex(new SequentialDocumentIndex("idx/docs.idx", "idx/docs_fields.idx", "idx/fields.db", "idx/field_types.db"))
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

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is a test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(new Iterator<Object>() {
                int index = 0;

                public Object next() {
                    return docs.get(index++);
                }

                public boolean hasNext() {
                    return docs.size() > index;
                }

                public void remove() {

                }
            }, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "content"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEmptyResult() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is a test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(new Iterator<Object>() {
                int index = 0;

                public Object next() {
                    return docs.get(index++);
                }

                public boolean hasNext() {
                    return docs.size() > index;
                }

                public void remove() {

                }
            }, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("Foo"), "content"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultipleResults() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is a test";
        TestDoc d2 = new TestDoc();
        d2.author = "Bob";
        d2.content = "This is a very long test text with lots of words";
        TestDoc d3 = new TestDoc();
        d3.author = "Alice";
        d3.content = "test";
        TestDoc d4 = new TestDoc();
        d4.author = "Frank";
        d4.content = "This should not match anything";
        TestDoc d5 = new TestDoc();
        d5.author = "Lars";
        d5.content = "This should also not match anything";
        TestDoc d6 = new TestDoc();
        d6.author = "Peter";
        d6.content = "This should also not match anything";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        docs.add(d4);
        docs.add(d5);
        docs.add(d6);
        try {
            index.indexDocuments(new Iterator<Object>() {
                int index = 0;

                public Object next() {
                    return docs.get(index++);
                }

                public boolean hasNext() {
                    return docs.size() > index;
                }

                public void remove() {

                }
            }, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "content"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected three results", 3, d.size());
            Assert.assertEquals("Expected doc3 first", d.get(0).getDocument().getDocumentId(), 2);
            Assert.assertEquals("Expected doc1 second", d.get(1).getDocument().getDocumentId(), 0);
            Assert.assertEquals("Expected doc2 third", d.get(2).getDocument().getDocumentId(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
