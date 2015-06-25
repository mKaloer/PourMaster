package com.kaloer.pourmaster.fields;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Stores field types using their id and class name.
 */
public class FieldTypeStore {

    private String filePath;
    private ArrayList<FieldType> fieldTypeMapping = new ArrayList<FieldType>();

    public FieldTypeStore(String filePath) throws IOException, ReflectiveOperationException {
        this.filePath = filePath;
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        // Reload from disc
        loadFromFile();
    }

    public FieldType findTypeById(int id) {
        return fieldTypeMapping.get(id);
    }

    public int addType(FieldType type) throws IOException, ReflectiveOperationException {
        byte[] className = type.getClass().getName().getBytes();
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(filePath, "rw");
            // Append to at end of file
            long pointer = file.length();
            file.seek(pointer);
            file.writeShort(className.length);
            file.write(className);
            // Create new object and add to mapping
            fieldTypeMapping.add(type.getClass().newInstance());
            return fieldTypeMapping.size() - 1;
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    public int getOrCreateTypeId(FieldType fieldType) throws IOException, ReflectiveOperationException {
        int index = fieldTypeMapping.indexOf(fieldType);
        if (index == -1) {
            index = addType(fieldType);
        }
        return index;

    }

    private void loadFromFile() throws IOException, ReflectiveOperationException {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(filePath, "r");
            while (file.getFilePointer() < file.length()) {
                int classNameLength = file.readUnsignedShort();
                byte[] buffer = new byte[classNameLength];
                file.read(buffer);
                String className = new String(buffer);
                fieldTypeMapping.add((FieldType) Class.forName(className).newInstance());
            }
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }
}
