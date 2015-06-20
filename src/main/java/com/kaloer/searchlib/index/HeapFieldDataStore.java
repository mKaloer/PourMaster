package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.fields.FieldData;
import com.kaloer.searchlib.index.fields.FieldList;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Created by mkaloer on 13/04/15.
 */
public class HeapFieldDataStore extends FieldDataStore {

    private String filePath;
    private FieldInfoStore fieldInfoStore;

    public HeapFieldDataStore(String filePath, String fieldInfoStorePath, String fieldTypeStorePath) throws IOException, ReflectiveOperationException {
        super();
        this.filePath = filePath;
        new File(filePath).createNewFile();
        this.fieldInfoStore = new FieldInfoStore(fieldInfoStorePath, fieldTypeStorePath);
    }

    @Override
    public FieldList getFields(long index) throws IOException {
        RandomAccessFile file = null;
        try {
            ArrayList<FieldData> fields = null;
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
    public Field getField(String name) throws IOException {
        return fieldInfoStore.getFieldByName(name);
    }

}
