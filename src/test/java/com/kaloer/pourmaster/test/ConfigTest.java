package com.kaloer.pourmaster.test;

import com.kaloer.pourmaster.BTreeTermDictionary;
import com.kaloer.pourmaster.IndexConfig;
import com.kaloer.pourmaster.InvertedIndex;
import com.kaloer.pourmaster.SequentialDocumentIndex;
import com.kaloer.pourmaster.postings.SequentialPostings;
import org.apache.commons.io.FileUtils;
import org.apache.directory.mavibot.btree.exception.BTreeAlreadyManagedException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigTest {

    private final File indexDir = new File("idx");
    private final File configFile = new File("config.proerties");

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
            configFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createConfigFile(String contents) throws IOException {
        configFile.createNewFile();
        FileWriter writer = new FileWriter(configFile);
        writer.write(contents);
        writer.close();
    }

    protected static InvertedIndex createIndex(boolean wildcards) throws IOException, ReflectiveOperationException, BTreeAlreadyManagedException {
        IndexConfig conf = new IndexConfig()
                .setDocumentIndex(SequentialDocumentIndex.class)
                .setBaseDirectory("idx")
                .setPostings(SequentialPostings.class)
                .setTermDictionary(BTreeTermDictionary.class)
                .set(BTreeTermDictionary.CONFIG_SUPPORT_WILDCARD_ID, Boolean.toString(wildcards))
                .setTmpDir(IndexTest.TMP_DIR.getPath());
        return new InvertedIndex(conf);
    }

    @Test
    public void testLoadConfig() throws IOException, ClassNotFoundException {
        final String baseDir = "idx";
        final Class seqDocIndex = SequentialDocumentIndex.class;
        final String docTypeFilePath = "idx/docTypes.idx";
        final Class postingsClass = SequentialPostings.class;
        final Class termDictClass = BTreeTermDictionary.class;
        String propFileContents = String.format("%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s",
                IndexConfig.BASE_DIR_ID, baseDir,
                IndexConfig.DOC_INDEX_CLASS_ID, seqDocIndex.getName(),
                IndexConfig.DOC_TYPE_FILE_PATH, docTypeFilePath,
                IndexConfig.POSTINGS_CLASS_ID, postingsClass.getName(),
                IndexConfig.TERM_DICTIONARY_CLASS_ID, termDictClass.getName());
        createConfigFile(propFileContents);
        IndexConfig config = new IndexConfig(configFile.getPath());

        Assert.assertEquals(baseDir, config.get(IndexConfig.BASE_DIR_ID));
        Assert.assertEquals(seqDocIndex, config.getDocumentIndex());
        Assert.assertEquals(docTypeFilePath, config.get(IndexConfig.DOC_TYPE_FILE_PATH));
        Assert.assertEquals(postingsClass, config.getPostings());
        Assert.assertEquals(termDictClass, config.getTermDictionary());
    }

    @Test(expected = ClassCastException.class)
    public void testLoadInvalidClass() throws IOException, ClassNotFoundException {
        final Class invalidDocIndex = BTreeTermDictionary.class; // Not valid doc index
        String propFileContents = String.format("%s: %s", IndexConfig.DOC_INDEX_CLASS_ID, invalidDocIndex.getName());
        createConfigFile(propFileContents);
        IndexConfig config = new IndexConfig(configFile.getPath());

        config.getDocumentIndex();
    }

    public void testSetConfig() throws ClassNotFoundException {
        final Class termDictClass = BTreeTermDictionary.class;
        IndexConfig config = new IndexConfig();
        config.setTermDictionary(termDictClass);
        Assert.assertEquals(termDictClass, config.getTermDictionary());
    }

    public void testCustomConfig() {
        final String myPrefKey = "my.custom.pref";
        final String myPrefValue = "This is my pref";
        IndexConfig config = new IndexConfig();
        config.set(myPrefKey, myPrefValue);
        Assert.assertEquals(myPrefValue, config.get(myPrefKey));
    }

    public void testUnknownPrefConfig() {
        final String myPrefKey = "my.custom.pref";
        final String myPrefValue = "This is my pref";
        IndexConfig config = new IndexConfig();
        Assert.assertEquals(myPrefValue, config.get(myPrefKey, myPrefValue));
    }

    public void testUnknownPrefConfig2() {
        final String myPrefKey = "my.custom.pref";
        IndexConfig config = new IndexConfig();
        Assert.assertNull(config.get(myPrefKey));
    }

    public void testDefaultValue() throws ClassNotFoundException {
        final Class termDictClass = BTreeTermDictionary.class;
        IndexConfig config = new IndexConfig();
        Assert.assertEquals(termDictClass, config.getTermDictionary());
    }
}
