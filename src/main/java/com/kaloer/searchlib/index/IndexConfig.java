package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.postings.Postings;
import com.kaloer.searchlib.index.postings.SequentialPostings;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration class for the inverted index. Used to configure the properties of the index.
 */
public class IndexConfig {

    public static final String BASE_DIR_ID = "index.baseDirectory";
    public static final String DOC_INDEX_CLASS_ID = "index.documentIndex";
    public static final String TERM_DICTIONARY_CLASS_ID = "index.termDictionary";
    public static final String POSTINGS_CLASS_ID = "index.postings";
    public static final String DOC_TYPE_FILE_PATH = "index.docTypeFilePath";

    private Properties configMap; // Map of various configuration values

    public IndexConfig() {
        configMap = new Properties();
    }

    public IndexConfig(String file) throws IOException {
        this();
        FileReader reader = new FileReader(file);
        configMap.load(reader);
    }

    public IndexConfig setDocumentIndex(Class<? extends DocumentIndex> docIndex) {
        this.set(DOC_INDEX_CLASS_ID, docIndex.getName());
        return this;
    }

    public Class<? extends DocumentIndex> getDocumentIndex() throws ClassNotFoundException {
        String className = configMap.getProperty(DOC_INDEX_CLASS_ID, SequentialDocumentIndex.class.getName());
        return Class.forName(className).asSubclass(DocumentIndex.class);
    }

    public IndexConfig setTermDictionary(Class<? extends TermDictionary> termDictionary) {
        this.set(TERM_DICTIONARY_CLASS_ID, termDictionary.getName());
        return this;
    }

    public Class<? extends TermDictionary> getTermDictionary() throws ClassNotFoundException {
        String className = configMap.getProperty(TERM_DICTIONARY_CLASS_ID, BTreeTermDictionary.class.getName());
        return Class.forName(className).asSubclass(TermDictionary.class);
    }

    public IndexConfig setPostings(Class<? extends Postings> postings) {
        this.set(POSTINGS_CLASS_ID, postings.getName());
        return this;
    }

    public Class<? extends Postings> getPostings() throws ClassNotFoundException {
        String className = configMap.getProperty(POSTINGS_CLASS_ID, SequentialPostings.class.getName());
        return Class.forName(className).asSubclass(Postings.class);
    }

    public String getDocumentTypeFilePath() {
        // Get path by id or create default.
        return getFilePath(DOC_TYPE_FILE_PATH, "docTypes.db");
    }

    public IndexConfig setDocumentTypeFilePath(String documentTypeFilePath) {
        this.set(DOC_TYPE_FILE_PATH, documentTypeFilePath);
        return this;
    }

    public String get(String key) {
        return configMap.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return configMap.getProperty(key, defaultValue);
    }

    public IndexConfig set(String key, String value) {
        configMap.setProperty(key, value);
        return this;
    }

    public boolean containsKey(String key) {
        return configMap.containsKey(key);
    }

    public IndexConfig setBaseDirectory(String dir) {
        set(BASE_DIR_ID, dir);
        return this;
    }

    public String getBaseDirectory() {
        return get(BASE_DIR_ID, "index");
    }

    public String getFilePath(String key, String defaultFileName) {
        return get(key, new File(getBaseDirectory(), defaultFileName).getPath());
    }
}
