package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.fields.FieldTypeStore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class FieldInfoStore {

    private static final int BIT_INDEX_INDEXED = 0;
    private static final int BIT_INDEX_STORED = 1;

    private String filePath;
    private ArrayList<Field> fieldIdMapping = new ArrayList<Field>();
    private HashMap<String, Field> fieldNameMapping = new HashMap<String, Field>();
    private FieldTypeStore fieldTypeStore;

    public FieldInfoStore(String fieldStorePath, String fieldTypeStorePath) throws IOException, ReflectiveOperationException {
        this.filePath = fieldStorePath;
        this.fieldTypeStore = new FieldTypeStore(fieldTypeStorePath);
        new File(filePath).createNewFile();
        loadFromFile();
    }

    public Field getFieldById(int fieldId) {
        return fieldIdMapping.get(fieldId);
    }

    public Field getFieldByName(String name) {
        return fieldNameMapping.get(name);
    }

    public int getOrCreateField(Field f) throws IOException, ReflectiveOperationException {
        if (fieldIdMapping.size() > f.getFieldId()) {
            return fieldIdMapping.get(f.getFieldId()).getFieldId();
        }
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(filePath, "rw");
            file.seek(file.length());
            f.setFieldId(fieldIdMapping.size());
            file.writeUTF(f.getFieldName());
            int typeId = fieldTypeStore.getOrCreateTypeId(f.getFieldType());
            file.writeByte(typeId);
            BitSet bitSet = new BitSet(8);
            bitSet.set(BIT_INDEX_INDEXED, f.isIndexed());
            bitSet.set(BIT_INDEX_STORED, f.isStored());
            file.write(bitSet.toByteArray());
            // Add to indexes
            fieldIdMapping.add(f);
            fieldNameMapping.put(f.getFieldName(), f);

            return f.getFieldId();
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    private void loadFromFile() throws IOException, ReflectiveOperationException {
        RandomAccessFile file = null;
        try {
            int fieldId = 0;
            file = new RandomAccessFile(filePath, "r");
            while (file.getFilePointer() < file.length()) {
                String fieldName = file.readUTF();
                int fieldType = file.readUnsignedByte();
                BitSet bitSet = BitSet.valueOf(new byte[]{file.readByte()});
                boolean indexed = bitSet.get(BIT_INDEX_INDEXED);
                boolean stored = bitSet.get(BIT_INDEX_STORED);
                Field f = new Field();
                f.setFieldId(fieldId);
                f.setFieldName(fieldName);
                f.setIsIndexed(indexed);
                f.setIsStored(stored);
                f.setFieldType(fieldTypeStore.findTypeById(fieldType));
                fieldIdMapping.add(f);
                fieldNameMapping.put(f.getFieldName(), f);
                fieldId++;
            }
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }
}
