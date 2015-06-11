package com.kaloer.searchlib.index.test;

import com.kaloer.searchlib.index.*;
import com.kaloer.searchlib.index.exceptions.ConflictingFieldTypesException;
import com.kaloer.searchlib.index.postings.SequentialPostings;
import com.kaloer.searchlib.index.search.MultiTermQuery;
import com.kaloer.searchlib.index.search.RankedDocument;
import com.kaloer.searchlib.index.search.TermQuery;
import com.kaloer.searchlib.index.terms.IntegerTerm;
import com.kaloer.searchlib.index.terms.StringTerm;
import com.kaloer.searchlib.index.test.models.*;
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

    protected static InvertedIndex createIndex() throws IOException, ReflectiveOperationException, BTreeAlreadyManagedException {
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
            index.indexDocuments(docs, tmpDir);
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
            index.indexDocuments(docs, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, results.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInDifferentFieldNoMatches() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "test";
        d1.id = 42;
        TestDoc d2 = new TestDoc();
        d2.author = "Bob";
        d2.content = "test";
        d2.id = 43;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        try {
            index.indexDocuments(docs, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "author"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, results.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInDifferentFieldOneMatch() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "test";
        d1.id = 42;
        TestDoc d2 = new TestDoc();
        d2.author = "test";
        d2.content = "test";
        d2.id = 43;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        try {
            index.indexDocuments(docs, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "author"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, results.size());
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
            index.indexDocuments(docs, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id2"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, results.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected=ConflictingFieldTypesException.class)
    public void testDifferentFieldTypes() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "test"; // Not stored
        d1.id = 42;

        TestDocIntAuthor d2 = new TestDocIntAuthor();
        d2.author = 42;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        try {
            index.indexDocuments(docs, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id2"));
            List<RankedDocument> results = index.search(query, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected=ClassCastException.class)
    public void testFieldTypeDataTypeMismatch() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex();

        TestDocTypeMismatch d = new TestDocTypeMismatch();
        d.author = 42;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d);
        try {
            index.indexDocuments(docs, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id2"));
            List<RankedDocument> results = index.search(query, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected=ClassCastException.class)
    public void testFieldIncompatibleAnalyzer() throws BTreeAlreadyManagedException, IOException, ReflectiveOperationException {
        final InvertedIndex index = createIndex();

        TestDocInvalidAnalyzer d = new TestDocInvalidAnalyzer();
        d.author = 42;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d);
        try {
            index.indexDocuments(docs, tmpDir);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id2"));
            List<RankedDocument> results = index.search(query, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
