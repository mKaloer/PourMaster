package com.kaloer.searchlib.index.test;

import com.kaloer.searchlib.index.*;
import com.kaloer.searchlib.index.search.MultiTermQuery;
import com.kaloer.searchlib.index.search.RankedDocument;
import com.kaloer.searchlib.index.search.TermQuery;
import com.kaloer.searchlib.index.terms.IntegerTerm;
import com.kaloer.searchlib.index.terms.StringTerm;
import com.kaloer.searchlib.index.terms.StringTermType;
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
                    .setTermDictionary(new BTreeTermDictionary("idx/dict.db"))
                    .setDocumentTypeFilePath("idx/te");
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
        d1.id = 42;
        TestDoc2 d2 = new TestDoc2();
        d2.author2 = "Bob";
        d2.content2 = "This is a very long test text with lots of words";
        TestDoc2 d3 = new TestDoc2();
        d3.author2 = "Alice";
        d3.content2 = "test";
        TestDoc d4 = new TestDoc();
        d4.author = "Frank";
        d4.content = "This should not match anything";
        d4.id = 0xDEADBEEF;
        TestDoc d5 = new TestDoc();
        d5.author = "Lars";
        d5.content = "This should also not match anything";
        d5.id = 10;
        TestDoc d6 = new TestDoc();
        d6.author = "Peter";
        d6.content = "This should also not match anything";
        d6.id = -10;
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
            query.add(new TermQuery(new StringTerm("test"), "content2"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected three results", 3, d.size());
            Assert.assertTrue("Expected doc3 first", d.get(0).getDocument() instanceof TestDoc2);
            Assert.assertEquals("Expected doc3 first", ((TestDoc2) d.get(0).getDocument()).author2, "Alice");
            Assert.assertTrue("Expected doc1 second", d.get(1).getDocument() instanceof TestDoc);
            Assert.assertEquals("Expected doc1 second", ((TestDoc) d.get(1).getDocument()).author, "Mads");
            Assert.assertTrue("Expected doc2 third", d.get(2).getDocument() instanceof TestDoc2);
            Assert.assertEquals("Expected doc2 third", ((TestDoc2) d.get(2).getDocument()).author2, "Bob");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStored() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "test"; // Not stored
        d1.id = 42;
        TestDoc2 d2 = new TestDoc2();
        d2.author2 = "Bob";
        d2.content2 = "test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
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
            query.add(new TermQuery(new StringTerm("test"), "content2"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected two results", 2, results.size());
            for(RankedDocument doc : results) {
                if(doc.getDocument() instanceof TestDoc) {
                    Assert.assertTrue("Expected content to be null", ((TestDoc) doc.getDocument()).content == null);
                } else if(doc.getDocument() instanceof TestDoc2) {
                    Assert.assertFalse("Expected content to be set", ((TestDoc2) doc.getDocument()).content2 == null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIndexed() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "test"; // Not stored
        d1.id = 42;
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
            query.add(new TermQuery(new IntegerTerm(42), "id"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, results.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIntegerField() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        TestDoc2 d1 = new TestDoc2();
        d1.author2 = "Alice";
        d1.content2 = "test"; // Not stored
        d1.id2 = 42;
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
            query.add(new TermQuery(new IntegerTerm(42), "id2"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, results.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
