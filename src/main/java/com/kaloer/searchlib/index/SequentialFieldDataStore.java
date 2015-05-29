package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.fields.FieldData;
import sun.reflect.FieldInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class SequentialFieldDataStore extends FieldDataStore {

    private String filePath;
    private FieldInfoStore fieldInfoStore;

    public SequentialFieldDataStore(String filePath, String fieldInfoStorePath, String fieldTypeStorePath) throws IOException, ReflectiveOperationException {
        super();
        this.filePath = filePath;
        new File(filePath).createNewFile();
        this.fieldInfoStore = new FieldInfoStore(fieldInfoStorePath, fieldTypeStorePath);
    }

    @Override
    public List<FieldData> getFields(long index) throws IOException {
        List<FieldData> fields = null;
        RandomAccessFile file = null;
        try {
            // Read field index
            file = new RandomAccessFile(filePath, "r");
            file.seek(index);
            int numFields = file.readUnsignedByte();
            fields = new ArrayList<FieldData>(numFields);
            // Read fields
            for(int i = 0; i < numFields; i++) {
                FieldData fieldData = FieldData.createFromData(file, fieldInfoStore);
                fields.add(fieldData);
            }
        } finally {
            if(file != null) {
                file.close();
            }
        }
        return fields;
    }

    @Override
    public long appendFields(List<FieldData> fields) throws IOException, ReflectiveOperationException {
        RandomAccessFile file = null;
        try {
            // Move to end of file
            file = new RandomAccessFile(filePath, "rw");
            file.seek(file.length());
            long pointer = file.getFilePointer();
            // Write number of fields
            file.writeByte(fields.size()); // Write as unsigned byte
            // Write fields
            for(FieldData data : fields) {
                // Make sure field info is stored
                int fieldId = fieldInfoStore.getOrCreateField(data.getField());
                data.getField().setFieldId(fieldId);

                data.writeToOutput(file);
            }
            return pointer;
        } finally {
            if(file != null) {
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
