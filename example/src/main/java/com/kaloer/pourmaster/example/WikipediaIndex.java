package com.kaloer.pourmaster.example;

import com.sun.deploy.util.StringUtils;
import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;
import pourmaster.BTreeTermDictionary;
import pourmaster.IndexConfig;
import pourmaster.InvertedIndex;
import pourmaster.SequentialDocumentIndex;
import pourmaster.postings.SequentialPostings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.*;

public class WikipediaIndex {

    protected static final File TMP_DIR = new File("wiki");
    private InvertedIndex index;
    private final String wikiPath;

    public WikipediaIndex(String wikiPath) throws IOException, ReflectiveOperationException {
        this.wikiPath = wikiPath;
        IndexConfig conf = new IndexConfig()
                .setDocumentIndex(SequentialDocumentIndex.class)
                .setBaseDirectory("idx")
                .setPostings(SequentialPostings.class)
                .setTermDictionary(BTreeTermDictionary.class)
                .set(BTreeTermDictionary.CONFIG_SUPPORT_WILDCARD_ID, Boolean.toString(true))
                .setTmpDir(TMP_DIR.getPath());
        index = new InvertedIndex(conf);
    }


    public void indexWiki() throws IOException, ReflectiveOperationException {
        index.indexDocuments(parseWikiPages());
    }

    public InvertedIndex getIndex() {
        return index;
    }

    private Iterator<Object> parseWikiPages() {
        final BlockingQueue<WikiPage> docQueue = new LinkedBlockingQueue<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            final Future<Void> parseTask = executorService.submit(new WikiParseTask(wikiPath, docQueue));
            return new Iterator<Object>() {
                int count = 0;
                public boolean hasNext() {
                    return !(docQueue.isEmpty() && parseTask.isDone());
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
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        return null;
    }


    public void close() {
        index.close();
    }

    private static class WikiParseTask implements Callable<Void> {

        private final WikiXMLParser wxsp;
        private BlockingQueue<WikiPage> docQueue;

        public WikiParseTask(String wikiPath, BlockingQueue<WikiPage> queue) {
            this.wxsp = WikiXMLParserFactory.getSAXParser(wikiPath);
            this.docQueue = queue;
        }

        @Override
        public Void call() throws Exception {
            wxsp.setPageCallback(new PageCallbackHandler() {
                public void process(edu.jhu.nlp.wikipedia.WikiPage page) {
                    try {
                        WikiPage indexedPage = new WikiPage();
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

            return null;
        }
    }

}
