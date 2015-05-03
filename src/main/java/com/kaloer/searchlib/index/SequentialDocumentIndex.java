package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.Field;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class SequentialDocumentIndex extends DocumentIndex {

    private String filePath;

    public SequentialDocumentIndex(String fileName, String fieldDataFileName, String fieldInfoFileName) throws IOException, ReflectiveOperationException {
        super(new SequentialFieldDataStore(fieldDataFileName, fieldInfoFileName));
        this.filePath = fileName;
    }

    @Override
    public void addDocument(Document doc) throws IOException, ReflectiveOperationException {
        RandomAccessFile file = null;
        try {
            long fieldIndex = getFieldDataStore().appendFields(doc.getFields());
            // Write field index
            file = new RandomAccessFile(filePath, "rw");
            file.seek(doc.getDocumentId() * 8);
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
            file.seek(docId * 8);
            long fieldIndex = file.readLong();
            // Read fields
            List<Field> fields = getFieldDataStore().getFields(fieldIndex);
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
}
