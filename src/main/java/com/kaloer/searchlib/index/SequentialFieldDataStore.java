package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.fields.FieldTypeStore;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class SequentialFieldDataStore extends FieldDataStore {

    private String filePath;
    private FieldTypeStore fieldTypeStore;

    public SequentialFieldDataStore(String filePath, String fieldTypeStorePath) throws IOException, ReflectiveOperationException {
        super();
        this.filePath = filePath;
        this.fieldTypeStore = new FieldTypeStore(fieldTypeStorePath);
    }

    @Override
    public List<Field> getFields(long index) throws IOException {
        List<Field> fields = null;
        RandomAccessFile file = null;
        try {
            // Read field index
            file = new RandomAccessFile(filePath, "r");
            file.seek(index);
            int numFields = file.readUnsignedByte();
            fields = new ArrayList<Field>(numFields);
            // Read fields
            for(int i = 0; i < numFields; i++) {
                Field f = Field.createFromData(file, fieldTypeStore);
                fields.add(f);
            }
        } finally {
            if(file != null) {
                file.close();
            }
        }
        return fields;
    }

    @Override
    public long appendFields(List<Field> fields) throws IOException, ReflectiveOperationException {
        RandomAccessFile file = null;
        try {
            // Move to end of file
            file = new RandomAccessFile(filePath, "rw");
            file.seek(file.length());
            long pointer = file.getFilePointer();
            // Write number of fields
            file.writeByte(fields.size()); // Write as unsigned byte
            // Write fields
            for(Field field : fields) {
                field.writeToOutput(file, fieldTypeStore);
            }
            return pointer;
        } finally {
            if(file != null) {
                file.close();
            }
        }
    }
}
