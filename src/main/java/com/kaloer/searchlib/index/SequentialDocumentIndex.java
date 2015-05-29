package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.FieldData;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class SequentialDocumentIndex extends DocumentIndex {

    /**
     * Size of metadata part in bytes.
     */
    private static final int METADATA_OFFSET = 8;

    private String filePath;
    private long docCount = -1;

    public SequentialDocumentIndex(String fileName, String fieldDataFileName, String fieldInfoFileName, String fieldTypesFileName) throws IOException, ReflectiveOperationException {
        super(new HeapFieldDataStore(fieldDataFileName, fieldInfoFileName, fieldTypesFileName));
        this.filePath = fileName;
        new File(filePath).createNewFile();
    }

    @Override
    public void addDocument(Document doc) throws IOException, ReflectiveOperationException {
        RandomAccessFile file = null;
        try {
            long fieldIndex = getFieldDataStore().appendFields(doc.getFields());

            file = new RandomAccessFile(filePath, "rw");
            // Increase doc count
            docCount = getDocumentCount() + 1;
            file.writeLong(docCount);
            // Write field index
            file.seek(docIdToPointer(doc.getDocumentId()));
            file.writeLong(fieldIndex);
        } finally {
            if(file != null) {
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
            List<FieldData> fields = getFieldDataStore().getFields(fieldIndex);
            doc = new Document();
            doc.setDocumentId(docId);
            doc.setFields(fields);
        } finally {
            if(file != null) {
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
        if(docCount == -1) {
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(filePath, "rw");
                if(file.length() == 0) {
                    file.writeLong(0);
                    docCount = 0;
                } else {
                    docCount = file.readLong();
                }
            } finally {
                if(file != null) {
                    file.close();
                }
            }
        }
        return docCount;
    }
}
