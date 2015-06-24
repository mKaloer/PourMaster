package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.FieldList;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a sequential document index, ordered by document id.
 */
public class SequentialDocumentIndex extends DocumentIndex {

    public static final String CONFIG_DOCS_FILE_PATH_ID = "docIndex.file";
    public static final String CONFIG_FIELD_DATA_PATH_ID = "docIndex.fieldData.file";
    public static final String CONFIG_FIELD_INFO_PATH_ID = "docIndex.fieldInfo.file";
    public static final String CONFIG_FIELD_TYPES_PATH_ID = "docIndex.fieldTypes.file";

    private static final String DEFAULT_DOCS_FILE_NAME = "docIndex.idx";
    private static final String DEFAULT_FIELD_DATA_FILE_NAME = "fieldData.idx";
    private static final String DEFAULT_FIELD_INFO_FILE_NAME= "fieldInfo.idx";
    private static final String DEFAULT_FIELD_TYPES_FILE_NAME = "fieldTypes.idx";
    /**
     * Size of metadata part in bytes.
     */
    private static final int METADATA_OFFSET = 8;

    private String filePath;
    private long docCount = -1;

    @Override
    public void init(IndexConfig config) throws IOException {
        this.filePath = config.getFilePath(CONFIG_DOCS_FILE_PATH_ID, DEFAULT_DOCS_FILE_NAME);
        String fieldDataFileName = config.getFilePath(CONFIG_FIELD_DATA_PATH_ID, DEFAULT_FIELD_DATA_FILE_NAME);
        String fieldInfoFileName = config.getFilePath(CONFIG_FIELD_INFO_PATH_ID, DEFAULT_FIELD_INFO_FILE_NAME);
        String fieldTypesFileName = config.getFilePath(CONFIG_FIELD_TYPES_PATH_ID, DEFAULT_FIELD_TYPES_FILE_NAME);

        this.filePath = config.getFilePath(CONFIG_DOCS_FILE_PATH_ID, DEFAULT_DOCS_FILE_NAME);
        try {
            setFieldDataStore(new HeapFieldDataStore(fieldDataFileName, fieldInfoFileName, fieldTypesFileName));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addDocument(Document doc) throws IOException, ReflectiveOperationException {
        RandomAccessFile file = null;
        try {
            long fieldIndex = getFieldDataStore().appendFields(new FieldList(doc.getFields(), doc.getDocumentType()));

            file = new RandomAccessFile(filePath, "rw");
            // Increase doc count
            docCount = getDocumentCount() + 1;
            file.writeLong(docCount);
            // Write field index
            file.seek(docIdToPointer(doc.getDocumentId()));
            file.writeLong(fieldIndex);
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    @Override
    public Document getDocument(long docId) throws IOException {
        Document doc = null;
        RandomAccessFile file = null;
        try {
            // Read field index
            file = new RandomAccessFile(filePath, "r");
            file.seek(docIdToPointer(docId));
            long fieldIndex = file.readLong();
            // Read fields
            FieldList fields = getFieldDataStore().getFields(fieldIndex);
            doc = new Document();
            doc.setDocumentId(docId);
            doc.setFields(fields.getFieldData());
            doc.setDocumentType(fields.getDocTypeId());
        } finally {
            if (file != null) {
                file.close();
            }
        }
        return doc;
    }

    private long docIdToPointer(long docId) {
        return docId * 8 + METADATA_OFFSET;
    }

    @Override
    public long getDocumentCount() throws IOException {
        if (docCount == -1) {
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(filePath, "rw");
                if (file.length() == 0) {
                    file.writeLong(0);
                    docCount = 0;
                } else {
                    docCount = file.readLong();
                }
            } finally {
                if (file != null) {
                    file.close();
                }
            }
        }
        return docCount;
    }
}
