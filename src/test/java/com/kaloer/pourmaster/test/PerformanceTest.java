package com.kaloer.pourmaster.test;

import com.kaloer.pourmaster.BTreeTermDictionary;
import com.kaloer.pourmaster.IndexConfig;
import com.kaloer.pourmaster.InvertedIndex;
import com.kaloer.pourmaster.SequentialDocumentIndex;
import com.kaloer.pourmaster.postings.SequentialPostings;
import com.kaloer.pourmaster.search.RankedDocument;
import com.kaloer.pourmaster.search.TermQuery;
import com.kaloer.pourmaster.terms.StringTerm;
import com.sun.deploy.util.StringUtils;
import edu.jhu.nlp.wikipedia.*;
import edu.jhu.nlp.wikipedia.WikiPage;
import opennlp.tools.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by mkaloer on 18/07/15.
 */
public class PerformanceTest {

    private static final String WIKI_DATA_PATH = "WIKI_DATA_PATH";
    protected static final File TMP_DIR = new File("wiki");
    private InvertedIndex index;

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
    public void testIndexing() throws IOException, ReflectiveOperationException {
        index = PerformanceTest.createIndex(true);
        index.indexDocuments(parseWikiPages());
        TermQuery q = new TermQuery(new StringTerm("fish"), "content");
        List<RankedDocument> result = index.search(q);
        System.out.println(String.format("Got %s results!", result.size()));
        int i = 0;
        for (RankedDocument doc : result) {
            com.kaloer.pourmaster.test.WikiPage page = (com.kaloer.pourmaster.test.WikiPage) doc.getDocument();
            System.out.println(String.format("%s (%f)", page.title, doc.getScore()));
            if (i > 20) break;
            i++;
        }
    }

    private Iterator<Object> parseWikiPages() {
        final WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(System.getenv(WIKI_DATA_PATH));
        try {
            final BlockingQueue<com.kaloer.pourmaster.test.WikiPage> docQueue = new LinkedBlockingQueue<com.kaloer.pourmaster.test.WikiPage>();
            final boolean[] finishedParsing = {false};
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                public void run() {
                    try {
                        wxsp.setPageCallback(new PageCallbackHandler() {
                            public void process(WikiPage page) {
                                try {
                                    com.kaloer.pourmaster.test.WikiPage indexedPage = new com.kaloer.pourmaster.test.WikiPage();
                                    indexedPage.title = page.getTitle();
                                    indexedPage.categories = StringUtils.join(page.getCategories(), " ");
                                    indexedPage.id = page.getID();
                                    indexedPage.content = page.getText();
                                    docQueue.put(indexedPage);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        wxsp.parse();
                        finishedParsing[0] = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return new Iterator<Object>() {
                int count = 0;
                public boolean hasNext() {
                    return !(docQueue.isEmpty() && finishedParsing[0]);
                }

                public Object next() {
                    try {
                        count++;
                        return docQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                public void remove() {
                    throw new NotImplementedException();
                }
            };
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
