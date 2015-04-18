package com.kaloer.searchlib.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class SequentialDocumentIndex extends DocumentIndex {

    private String filePath;

    public SequentialDocumentIndex(String fileName, String fieldDataFileName) {
        super(new SequentialFieldDataStore(fieldDataFileName));
        this.filePath = fileName;
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
