package com.kaloer.pourmaster;

import com.kaloer.pourmaster.fields.Field;
import com.kaloer.pourmaster.fields.FieldData;
import com.kaloer.pourmaster.fields.FieldList;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

/**
 * Stores field data in a large heap file in no specific order.
 */
public class HeapFieldDataStore extends FieldDataStore {

    private String filePath;
    private FieldInfoStore fieldInfoStore;

    public HeapFieldDataStore(String filePath, String fieldInfoStorePath, String fieldTypeStorePath) throws IOException, ReflectiveOperationException {
        super();
        this.filePath = filePath;
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        this.fieldInfoStore = new FieldInfoStore(fieldInfoStorePath, fieldTypeStorePath);
    }

    @Override
    public FieldList getFields(long index) throws IOException {
        RandomAccessFile file = null;
        try {
            ArrayList<FieldData> fields;
            // Read field index
            file = new RandomAccessFile(filePath, "r");
            file.seek(index);
            int docType = file.readUnsignedByte();
            int numFields = file.readUnsignedByte();
            fields = new ArrayList<FieldData>(numFields);
            // Read fields
            for (int i = 0; i < numFields; i++) {
                FieldData fieldData = FieldData.createFromData(file, fieldInfoStore);
                fields.add(fieldData);
            }
            return new FieldList(fields, docType);
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    @Override
    public long appendFields(FieldList fields) throws IOException, ReflectiveOperationException {
        RandomAccessFile file = null;
        try {
            // Move to end of file
            file = new RandomAccessFile(filePath, "rw");
            file.seek(file.length());
            long pointer = file.getFilePointer();
            // Write doc type id
            file.writeByte(fields.getDocTypeId());
            // Write number of fields
            file.writeByte(fields.getFieldData().size()); // Write as unsigned byte
            // Write fields
            for (FieldData data : fields.getFieldData()) {
                // Make sure field info is stored
                int fieldId = fieldInfoStore.getOrCreateField(data.getField());
                data.getField().setFieldId(fieldId);

                data.writeToOutput(file);
            }
            return pointer;
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    @Override
    public Field getField(int id) throws IOException {
        return fieldInfoStore.getFieldById(id);
    }

    @Override
    public void deleteAll() throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        file.setLength(0);
        fieldInfoStore.deleteAll();
    }

    @Override
    public Field getField(String name) throws IOException {
        return fieldInfoStore.getFieldByName(name);
    }

}
