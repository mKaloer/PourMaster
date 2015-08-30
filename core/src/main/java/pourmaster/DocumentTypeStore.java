package pourmaster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Stores different document classes
 */
public class DocumentTypeStore {


    // Mapping from docType id to class
    private final ArrayList<Class> docId2TypeMapping = new ArrayList<Class>();
    // Mapping from class to id
    private final HashMap<Class, Integer> type2DocIdMapping = new HashMap<Class, Integer>();

    private final String filePath;

    public DocumentTypeStore(String docTypeStorePath) throws IOException, ReflectiveOperationException {
        this.filePath = docTypeStorePath;
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        loadFromFile();
    }

    public int getOrCreateDocumentType(Class docType) throws IOException {
        if (type2DocIdMapping.containsKey(docType)) {
            return type2DocIdMapping.get(docType);
        }
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(filePath, "rw");
            file.seek(file.length());
            // Insert new type
            int index = docId2TypeMapping.size();
            if (index > 255) {
                throw new IndexOutOfBoundsException("Too many document types!");
            }
            file.writeUTF(docType.getName());
            type2DocIdMapping.put(docType, index);
            docId2TypeMapping.add(docType);
            return index;
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    public Class getDocumentType(int docTypeId) {
        if (docTypeId >= docId2TypeMapping.size()) {
            throw new NoSuchElementException(String.format("No document with id %d found.", docTypeId));
        }
        return docId2TypeMapping.get(docTypeId);
    }

    private void loadFromFile() throws IOException, ReflectiveOperationException {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(filePath, "r");
            while (file.getFilePointer() < file.length()) {
                Class classType = Class.forName(file.readUTF());
                docId2TypeMapping.add(classType);
                type2DocIdMapping.put(classType, docId2TypeMapping.size() - 1);
            }
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    protected void deleteAll() throws IOException {
        // Delete file contents
        RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        file.setLength(0);
        // Delete cached contents
        docId2TypeMapping.clear();
        type2DocIdMapping.clear();
    }

}
