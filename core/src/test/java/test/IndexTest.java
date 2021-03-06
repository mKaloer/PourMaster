package test;

import pourmaster.*;
import pourmaster.exceptions.ConflictingFieldTypesException;
import pourmaster.postings.PostingsData;
import pourmaster.postings.SequentialPostings;
import pourmaster.terms.StringTerm;
import pourmaster.util.IOIterator;
import pourmaster.search.MultiTermQuery;
import pourmaster.search.RankedDocument;
import pourmaster.search.TermQuery;
import pourmaster.terms.IntegerTerm;
import pourmaster.terms.Term;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.models.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IndexTest {

    protected static final File TMP_DIR = new File("tmp");
    private final File indexDir = new File("idx");

    @Before
    public void setup() {
        TMP_DIR.mkdirs();
        indexDir.mkdirs();
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(indexDir);
            FileUtils.deleteDirectory(TMP_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static InvertedIndex createIndex(boolean wildcards) throws IOException, ReflectiveOperationException {
        IndexConfig conf = new IndexConfig()
                .setDocumentIndex(SequentialDocumentIndex.class)
                .setBaseDirectory("idx")
                .setPostings(SequentialPostings.class)
                .setTermDictionary(BTreeTermDictionary.class)
                .set(BTreeTermDictionary.CONFIG_SUPPORT_WILDCARD_ID, Boolean.toString(wildcards))
                .setTmpDir(TMP_DIR.getPath());
        return new InvertedIndex(conf);
    }

    @Test
    public void testStored() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex(false);

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
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "content"));
            query.add(new TermQuery(new StringTerm("test"), "content2"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected two results", 2, results.size());
            for (RankedDocument doc : results) {
                if (doc.getDocument() instanceof TestDoc) {
                    Assert.assertTrue("Expected content to be null", ((TestDoc) doc.getDocument()).content == null);
                } else if (doc.getDocument() instanceof TestDoc2) {
                    Assert.assertFalse("Expected content to be set", ((TestDoc2) doc.getDocument()).content2 == null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIndexed() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.author = "Alice";
        d1.content = "test"; // Not stored
        d1.id = 42;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, results.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInDifferentFieldNoMatches() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex(false);

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
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "author"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected zero results", 0, results.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInDifferentFieldOneMatch() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex(false);

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
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new StringTerm("test"), "author"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, results.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIntegerField() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex(false);

        TestDoc2 d1 = new TestDoc2();
        d1.author2 = "Alice";
        d1.content2 = "test"; // Not stored
        d1.id2 = 42;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id2"));
            List<RankedDocument> results = index.search(query, -1);
            Assert.assertEquals("Expected one result", 1, results.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = ConflictingFieldTypesException.class)
    public void testDifferentFieldTypes() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex(false);

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
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id2"));
            List<RankedDocument> results = index.search(query, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = ClassCastException.class)
    public void testFieldTypeDataTypeMismatch() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex(false);

        TestDocTypeMismatch d = new TestDocTypeMismatch();
        d.author = 42;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d);
        try {
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id2"));
            List<RankedDocument> results = index.search(query, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = ClassCastException.class)
    public void testFieldIncompatibleAnalyzer() throws IOException, ReflectiveOperationException {
        final InvertedIndex index = createIndex(false);

        TestDocInvalidAnalyzer d = new TestDocInvalidAnalyzer();
        d.author = 42;
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d);
        try {
            index.indexDocuments(docs);
            MultiTermQuery query = new MultiTermQuery();
            query.add(new TermQuery(new IntegerTerm(42), "id2"));
            List<RankedDocument> results = index.search(query, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDocIndexSingleDoc() throws ReflectiveOperationException, IOException {
        final InvertedIndex index = createIndex(false);

        TestDoc d = new TestDoc();
        d.content = "This is a test";
        d.author = "Alice and Bob";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d);
        try {
            index.indexDocuments(docs);
            TestDoc.SimpleStringAnalyzer analyzer = new TestDoc.SimpleStringAnalyzer();
            Iterator<Token> tokens = analyzer.analyze(d.content);
            int tokenIndex = 0;
            while (tokens.hasNext()) {
                Term term = tokens.next().getValue();
                TermDictionary.TermData data = index.getDictionary().findTerm(term);
                IOIterator<PostingsData> postingsIterator = index.getPostings().getDocumentsForTerm(data.getPostingsIndex(), data.getDocFrequency());
                PostingsData postingsData = postingsIterator.next();
                Assert.assertEquals(0, postingsData.getDocumentId());
                Assert.assertEquals(String.format("Expected %s to be found in %d places", term, postingsData.getPositions().size()),
                        1, postingsData.getPositions().size());
                Assert.assertEquals(String.format("Expected %s to be found at index %d", term, tokenIndex),
                        tokenIndex, postingsData.getPositions().get(0).getPosition());
                tokenIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDocIndexMultipleDocs() throws ReflectiveOperationException, IOException {
        final InvertedIndex index = createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.content = "This is a test";
        d1.author = "Alice and Bob";
        TestDoc d2 = new TestDoc();
        d2.content = "Hello This is a test";
        d2.author = "Alice and Bob";
        TestDoc d3 = new TestDoc();
        d3.content = "Hello hello This is a test";
        d3.author = "Alice and Bob";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        docs.add(d3);
        try {
            index.indexDocuments(docs);
            TestDoc.SimpleStringAnalyzer analyzer = new TestDoc.SimpleStringAnalyzer();
            Iterator<Token> tokens = analyzer.analyze(d1.content);
            int tokenIndex = 0;
            while (tokens.hasNext()) {
                Term term = tokens.next().getValue();
                TermDictionary.TermData data = index.getDictionary().findTerm(term);
                IOIterator<PostingsData> postingsIterator = index.getPostings().getDocumentsForTerm(data.getPostingsIndex(), data.getDocFrequency());
                Assert.assertEquals(3, data.getDocFrequency());
                for (int i = 0; i < 3; i++) {
                    PostingsData postingsData = postingsIterator.next();
                    Assert.assertEquals(i, postingsData.getDocumentId());
                    Assert.assertEquals(String.format("Expected %s to be found in %d places", term, postingsData.getPositions().size()),
                            1, postingsData.getPositions().size());
                    Assert.assertEquals(String.format("Expected %s to be found at index %d", term, tokenIndex),
                            tokenIndex + i, postingsData.getPositions().get(0).getPosition());
                }
                tokenIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSameTermInTwoFields() throws ReflectiveOperationException, IOException {
        final InvertedIndex index = createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.content = "This is a test and this is a test";
        d1.author = "test and test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs);
            TermDictionary.TermData data = index.getDictionary().findTerm(new StringTerm("test"));
            Assert.assertEquals("Expected 'test' to be found in one document", 1, data.getDocFrequency());
            IOIterator<PostingsData> postingsIterator = index.getPostings().getDocumentsForTerm(data.getPostingsIndex(), data.getDocFrequency());
            PostingsData postingsData = postingsIterator.next();
            Assert.assertEquals(4, postingsData.getPositions().size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDocIndexDuplicatesInOneDoc() throws ReflectiveOperationException, IOException {
        final InvertedIndex index = createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.content = "This is a test and This is should work";
        d1.author = "Alice Bob";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        try {
            index.indexDocuments(docs);

            // 'This'
            String term = "This";
            TermDictionary.TermData data = index.getDictionary().findTerm(new StringTerm(term));
            IOIterator<PostingsData> postingsIterator = index.getPostings().getDocumentsForTerm(data.getPostingsIndex(), data.getDocFrequency());
            Assert.assertEquals(1, data.getDocFrequency());
            PostingsData postingsData = postingsIterator.next();
            Assert.assertEquals(0, postingsData.getDocumentId());
            Assert.assertEquals(String.format("Expected '%s' to be found in %d places", term, 2),
                    2, postingsData.getPositions().size());
            Assert.assertEquals(String.format("Expected '%s' to be found at index %d", term, 0),
                    0, postingsData.getPositions().get(0).getPosition());
            Assert.assertEquals(String.format("Expected '%s' to be found at index %d", term, 5),
                    5, postingsData.getPositions().get(1).getPosition());
            // 'is'
            term = "is";
            data = index.getDictionary().findTerm(new StringTerm(term));
            postingsIterator = index.getPostings().getDocumentsForTerm(data.getPostingsIndex(), data.getDocFrequency());
            Assert.assertEquals(1, data.getDocFrequency());
            postingsData = postingsIterator.next();
            Assert.assertEquals(0, postingsData.getDocumentId());
            Assert.assertEquals(String.format("Expected '%s' to be found in %d places", term, 2),
                    2, postingsData.getPositions().size());
            Assert.assertEquals(String.format("Expected '%s' to be found at index %d", term, 1),
                    1, postingsData.getPositions().get(0).getPosition());
            Assert.assertEquals(String.format("Expected '%s' to be found at index %d", term, 6),
                    6, postingsData.getPositions().get(1).getPosition());
            // 'a'
            term = "a";
            data = index.getDictionary().findTerm(new StringTerm(term));
            postingsIterator = index.getPostings().getDocumentsForTerm(data.getPostingsIndex(), data.getDocFrequency());
            Assert.assertEquals(1, data.getDocFrequency());
            postingsData = postingsIterator.next();
            Assert.assertEquals(0, postingsData.getDocumentId());
            Assert.assertEquals(String.format("Expected '%s' to be found in %d places", term, 1),
                    1, postingsData.getPositions().size());
            Assert.assertEquals(String.format("Expected '%s' to be found at index %d", term, 2),
                    2, postingsData.getPositions().get(0).getPosition());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchBeforeIndexed() throws IOException, ReflectiveOperationException {

        final InvertedIndex index = createIndex(false);

        MultiTermQuery query = new MultiTermQuery();
        query.add(new TermQuery(new IntegerTerm(42), "id"));
        List<RankedDocument> results = index.search(query, -1);
        Assert.assertEquals("Expected zero results", 0, results.size());
    }

    @Test
    public void testLoadIndex() throws IOException, ReflectiveOperationException {
        InvertedIndex index = createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.content = "This is a test and this is a test";
        d1.author = "test and test";
        TestDoc2 d2 = new TestDoc2();
        d2.content2 = "This is a test";
        d2.author2 = "test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        index.indexDocuments(docs);

        // Reload index
        index.close();
        index = createIndex(false);

        List<RankedDocument> results = index.search(new TermQuery(new StringTerm("test"), "content"), -1);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(TestDoc.class, results.get(0).getDocument().getClass());
        results = index.search(new TermQuery(new StringTerm("test"), "content2"), -1);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(TestDoc2.class, results.get(0).getDocument().getClass());

        TermDictionary.TermData data = index.getDictionary().findTerm(new StringTerm("test"));
        Assert.assertEquals("Expected 'test' to be found in two documents", 2, data.getDocFrequency());
        IOIterator<PostingsData> postingsIterator = index.getPostings().getDocumentsForTerm(data.getPostingsIndex(), data.getDocFrequency());
        PostingsData postingsData = postingsIterator.next();
        Assert.assertEquals(4, postingsData.getPositions().size());
        postingsData = postingsIterator.next();
        Assert.assertEquals(2, postingsData.getPositions().size());
    }

    @Test
    public void testReIndex() throws IOException, ReflectiveOperationException {
        InvertedIndex index = createIndex(false);

        TestDoc d1 = new TestDoc();
        d1.content = "This is a test and this is a test";
        d1.author = "test and test";
        TestDoc2 d2 = new TestDoc2();
        d2.content2 = "This is a test";
        d2.author2 = "test";
        final ArrayList<Object> docs = new ArrayList<Object>();
        docs.add(d1);
        docs.add(d2);
        index.indexDocuments(docs);

        List<RankedDocument> results = index.search(new TermQuery(new StringTerm("test"), "content"), -1);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(TestDoc.class, results.get(0).getDocument().getClass());
        results = index.search(new TermQuery(new StringTerm("test"), "content2"), -1);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(TestDoc2.class, results.get(0).getDocument().getClass());

        TermDictionary.TermData data = index.getDictionary().findTerm(new StringTerm("test"));
        Assert.assertEquals("Expected 'test' to be found in two documents", 2, data.getDocFrequency());
        IOIterator<PostingsData> postingsIterator = index.getPostings().getDocumentsForTerm(data.getPostingsIndex(), data.getDocFrequency());
        PostingsData postingsData = postingsIterator.next();
        Assert.assertEquals(4, postingsData.getPositions().size());
        postingsData = postingsIterator.next();
        Assert.assertEquals(2, postingsData.getPositions().size());

        index.close();
        // Index again
        index.indexDocuments(docs);

        results = index.search(new TermQuery(new StringTerm("test"), "content"), -1);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(TestDoc.class, results.get(0).getDocument().getClass());
        results = index.search(new TermQuery(new StringTerm("test"), "content2"), -1);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(TestDoc2.class, results.get(0).getDocument().getClass());

        data = index.getDictionary().findTerm(new StringTerm("test"));
        Assert.assertEquals("Expected 'test' to be found in two documents", 2, data.getDocFrequency());
        postingsIterator = index.getPostings().getDocumentsForTerm(data.getPostingsIndex(), data.getDocFrequency());
        postingsData = postingsIterator.next();
        Assert.assertEquals(4, postingsData.getPositions().size());
        postingsData = postingsIterator.next();
        Assert.assertEquals(2, postingsData.getPositions().size());
    }

}
