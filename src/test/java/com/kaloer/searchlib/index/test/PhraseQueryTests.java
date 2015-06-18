package com.kaloer.searchlib.index.test;

import com.kaloer.searchlib.index.InvertedIndex;
import com.kaloer.searchlib.index.search.PhraseQuery;
import com.kaloer.searchlib.index.search.RankedDocument;
import com.kaloer.searchlib.index.search.WildcardQuery;
import com.kaloer.searchlib.index.terms.StringTerm;
import com.kaloer.searchlib.index.test.models.TestDoc;
import org.apache.commons.io.FileUtils;
import org.apache.directory.mavibot.btree.exception.BTreeAlreadyManagedException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhraseQueryTests {

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

    @Test
    public void testOneResult() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is a test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs, tmpDir);
            PhraseQuery query = new PhraseQuery("content", new StringTerm("a"), new StringTerm("test"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAllMatches() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);


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
        d3.content = "This is not a test";
        d3.id = 18;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        try {
            index.indexDocuments(docs, tmpDir);
            PhraseQuery query = new PhraseQuery("content", new StringTerm("This"), new StringTerm("is"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected three results", 3, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNoMatches() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);


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
        d3.content = "This is not a test";
        d3.id = 18;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        try {
            index.indexDocuments(docs, tmpDir);
            PhraseQuery query = new PhraseQuery("content", new StringTerm("This"), new StringTerm("is"), new StringTerm("stupid"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTermInMiddle() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);


        TestDoc d1 = new TestDoc();
        d1.author = "test";
        d1.content = "This FOOO is a test";
        d1.id = 42;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs, tmpDir);
            PhraseQuery query = new PhraseQuery("content", new StringTerm("This"), new StringTerm("is"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPartialMatches() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);


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
        d3.content = "This is not a test";
        d3.id = 18;

        TestDoc d4 = new TestDoc();
        d4.author = "Alice";
        d4.content = "Should not match anything";
        d4.id = 18;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        docs.add(d4);
        try {
            index.indexDocuments(docs, tmpDir);
            PhraseQuery query = new PhraseQuery("content", new StringTerm("This"), new StringTerm("is"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected three results", 3, d.size());
            assertDocumentInResultSet(d, d1);
            assertDocumentInResultSet(d, d2);
            assertDocumentInResultSet(d, d3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRanking() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);


        TestDoc d1 = new TestDoc();
        d1.author = "test";
        d1.content = "This is a test";
        d1.id = 42;
        TestDoc d2 = new TestDoc();
        d2.author = "test";
        d2.content = "This is This is This is";
        d2.id = 0xFF;
        TestDoc d3 = new TestDoc();
        d3.author = "Alice";
        d3.content = "This is not a test. This is nothing";
        d3.id = 18;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        try {
            index.indexDocuments(docs, tmpDir);
            PhraseQuery query = new PhraseQuery("content", new StringTerm("This"), new StringTerm("is"));
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected three results", 3, d.size());
            Assert.assertEquals(((TestDoc) d.get(0).getDocument()).id, d2.id);
            Assert.assertEquals(((TestDoc) d.get(1).getDocument()).id, d3.id);
            Assert.assertEquals(((TestDoc) d.get(2).getDocument()).id, d1.id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asserts that the document is part of the result set.
     * @param results The result set.
     * @param d The document to check.
     */
    private static void assertDocumentInResultSet(List<RankedDocument> results, TestDoc d) {
        boolean contained = false;
        for(RankedDocument r : results) {
            TestDoc doc = (TestDoc) r.getDocument();
            if(d.author.equals(doc.author) && d.id == doc.id) {
                contained = true;
                break;
            }
        }
        Assert.assertTrue(contained);
    }

}
