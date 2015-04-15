package com.kaloer.searchlib.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class SequentialFieldDataStore extends FieldDataStore {

    private String filePath;

    public SequentialFieldDataStore(String filePath) {
        super();
        this.filePath = filePath;
        // TODO: We need a field info (file?) for storing field name and other info. Should be also be in memory for fast lookup by field id
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
                Field f = Field.createFromData(file);
                fields.add(f);
            }
        } finally {
            if(file != null) {
                file.close();
            }
        }
        return fields;
    }
}
