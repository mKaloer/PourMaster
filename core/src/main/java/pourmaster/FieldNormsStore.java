package pourmaster;

import pourmaster.util.LargeMappedFloatBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Store for field normalization values.
 */
public class FieldNormsStore {

    private final long numDocs; // Size in number of documents.
    private final int numFields;
    private final String filePath;
    // MappedByteBuffer per field
    private final ArrayList<LargeMappedFloatBuffer> fieldBuffers;

    public FieldNormsStore(String filePath, int numFields, long numDocuments) throws IOException {
        fieldBuffers = new ArrayList<LargeMappedFloatBuffer>(numFields);
        for (int i = 0; i < numFields; i++) {
            fieldBuffers.add(null);
        }
        this.numDocs = numDocuments;
        this.numFields = numFields;
        this.filePath = filePath;
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        // Allocate length
        RandomAccessFile rFile = null;
        try {
            rFile = new RandomAccessFile(file, "rw");
            rFile.setLength(numFields * numDocuments * 4);
            rFile.close();
        } finally {
            if (rFile != null) {
                rFile.close();
            }
        }
    }

    public void setFieldNorm(int fieldId, long documentId, float value) throws IOException {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(this.filePath, "rw");
            file.seek((fieldId * numDocs + documentId) * 4);
            file.writeFloat(value);
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    public float getFieldNorm(int fieldId, long documentId) throws IOException {
        LargeMappedFloatBuffer buffer;
        if ((buffer = fieldBuffers.get(fieldId)) == null) {
            RandomAccessFile file = new RandomAccessFile(this.filePath, "r");
            buffer = new LargeMappedFloatBuffer(file.getChannel(), FileChannel.MapMode.READ_ONLY, numDocs, fieldId * numDocs * 4);
            fieldBuffers.set(fieldId, buffer);
        }
        return buffer.get(documentId);
    }

    public String getFile() {
        return filePath;
    }

    public void deleteAll() throws IOException {
        this.fieldBuffers.clear();
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(this.filePath, "rw");
            file.setLength(0);
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }
}
