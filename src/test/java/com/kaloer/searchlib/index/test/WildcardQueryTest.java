package com.kaloer.searchlib.index.test;

import com.kaloer.searchlib.index.InvertedIndex;
import com.kaloer.searchlib.index.search.MultiTermQuery;
import com.kaloer.searchlib.index.search.RankedDocument;
import com.kaloer.searchlib.index.search.TermQuery;
import com.kaloer.searchlib.index.search.WildcardQuery;
import com.kaloer.searchlib.index.terms.StringTerm;
import com.kaloer.searchlib.index.test.models.TestDoc;
import com.kaloer.searchlib.index.test.models.TestDoc2;
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

public class WildcardQueryTest {

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
        d1.content = "This is an example of a fooverybar content";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs, tmpDir);
            WildcardQuery query = new WildcardQuery(new StringTerm("foo"), new StringTerm("bar"), "content");
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultipleResults() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is an example of a fooverybar content";
        TestDoc d2 = new TestDoc();
        d2.author = "Alice";
        d2.content = "This is an example of a fooveryverybar content";
        TestDoc d3 = new TestDoc();
        d3.author = "Bob";
        d3.content = "fooveryverymuchbar";
        TestDoc d4 = new TestDoc();
        d4.author = "Ken";
        d4.content = "Not Matchin' Anything(TM)";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        docs.add(d4);
        try {
            index.indexDocuments(docs, tmpDir);
            WildcardQuery query = new WildcardQuery(new StringTerm("foo"), new StringTerm("bar"), "content");
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected three results", 3, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNoResults() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);

        TestDoc d1 = new TestDoc();
        d1.author = "Mads";
        d1.content = "This is an example of content";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs, tmpDir);
            WildcardQuery query = new WildcardQuery(new StringTerm("foo"), new StringTerm("bar"), "content");
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPrefixQuery() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "This is an example of content";
        TestDoc d2 = new TestDoc();
        d2.author = "Bob";
        d2.content = "This should not match anything helloexa";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        try {
            index.indexDocuments(docs, tmpDir);
            WildcardQuery query = new WildcardQuery(new StringTerm("exa"), null, "content");
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSuffixQuery() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "This is an example of content";
        TestDoc d2 = new TestDoc();
        d2.author = "Bob";
        d2.content = "This should not match anything mpletest";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        try {
            index.indexDocuments(docs, tmpDir);
            WildcardQuery query = new WildcardQuery(null, new StringTerm("mple"), "content");
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEqualPrefixSuffixQuery() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "This is a palindrome: abbabba";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs, tmpDir);
            WildcardQuery query = new WildcardQuery(new StringTerm("abbabba"), new StringTerm("abbabba"), "content");
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPrefixInSuffixQuery() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "This is a palindrome: abbabba";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs, tmpDir);
            WildcardQuery query = new WildcardQuery(new StringTerm("abbabba"), new StringTerm("ba"), "content");
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSuffixInPrefixQuery() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = IndexTest.createIndex(true);

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "This is a palindrome: abbabba";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs, tmpDir);
            WildcardQuery query = new WildcardQuery(new StringTerm("ab"), new StringTerm("abbabba"), "content");
            List<RankedDocument> d = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, d.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
