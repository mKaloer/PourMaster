package com.kaloer.pourmaster.test;

import com.kaloer.pourmaster.InvertedIndex;
import com.kaloer.pourmaster.search.MultiTermQuery;
import com.kaloer.pourmaster.search.RankedDocument;
import com.kaloer.pourmaster.terms.StringTerm;
import com.kaloer.pourmaster.test.models.TestDoc;
import com.kaloer.pourmaster.test.models.TestDoc2;
import com.kaloer.pourmaster.search.TermQuery;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiTermQueryTest {

    private final File indexDir = new File("idx");

    @Before
    public void setup() {
        IndexTest.TMP_DIR.mkdirs();
        indexDir.mkdirs();
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(indexDir);
            FileUtils.deleteDirectory(IndexTest.TMP_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOneResult() throws IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is a test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "content"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEmptyResult() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = IndexTest.createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is a test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("Foo"), "content"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultipleResults() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = IndexTest.createIndex(false);

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
            index.indexDocuments(docs);
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
    public void testBoosting() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = IndexTest.createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is a test";
        d1.id = 42;
        TestDoc d2 = new TestDoc();
        d2.author = "Bob";
        d2.content = "This is a very long test text with lots of words";
        TestDoc d3 = new TestDoc();
        d3.author = "Alice";
        d3.content = "test";
        TestDoc d4 = new TestDoc();
        d4.author = "test";
        d4.content = "Hello there";
        d4.id = 0xDEADBEEF;
        TestDoc d5 = new TestDoc();
        d5.author = "Should not match anything";
        d5.content = "Foo";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        docs.add(d4);
        docs.add(d5);
        try {
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "content"));
            TermQuery testAuthorQuery = new TermQuery(new StringTerm("test"), "author");
            testAuthorQuery.setBoost(10.0);
            query.add(testAuthorQuery);
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Unexpected number of results", 4, d.size());
            Assert.assertEquals("Expected doc4 first", ((TestDoc) d.get(0).getDocument()).author, "test");
            Assert.assertEquals("Expected doc3 second", ((TestDoc) d.get(1).getDocument()).author, "Alice");
            Assert.assertEquals("Expected doc1 third", ((TestDoc) d.get(2).getDocument()).author, "Mads");
            Assert.assertEquals("Expected doc2 fourth", ((TestDoc) d.get(3).getDocument()).author, "Bob");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTwoFieldMatch() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = IndexTest.createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.author = "test";
        d1.content = "This is a test";
        d1.id = 42;
        TestDoc d2 = new TestDoc();
        d2.author = "test";
        d2.content = "This is a very long test text with lots of words";
        d2.id = 0xFF;
        TestDoc d3 = new TestDoc();
        d3.author = "Alice";
        d3.content = "Should not match";
        d3.id = 18;
        TestDoc d4 = new TestDoc();
        d4.author = "test";
        d4.content = "Hello there";
        d4.id = 0xDEADBEEF;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        docs.add(d4);
        try {
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "content"));
            query.add(new TermQuery(new StringTerm("test"), "author"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Unexpected number of results", 3, d.size());
            assertDocumentInResultSet(d, d1);
            assertDocumentInResultSet(d, d2);
            assertDocumentInResultSet(d, d4);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPartialMatchResult() throws IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is a test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "content"));
            query.add(new TermQuery(new StringTerm("ThisShouldNotBeFoundAnywhere"), "content"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asserts that the document is part of the result set.
     *
     * @param results The result set.
     * @param d       The document to check.
     */
    private static void assertDocumentInResultSet(List<RankedDocument> results, TestDoc d) {
        boolean contained = false;
        for (RankedDocument r : results) {
            TestDoc doc = (TestDoc) r.getDocument();
            if (d.author.equals(doc.author) && d.id == doc.id) {
                contained = true;
                break;
            }
        }
        Assert.assertTrue(contained);
    }


}
