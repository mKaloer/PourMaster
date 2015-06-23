package com.kaloer.searchlib.index;

import java.nio.ByteBuffer;

/**
 * Represents an atomic term. Every {@link com.kaloer.searchlib.index.terms.Term} must consist of an atomic term.
 */
public final class AtomicTerm implements Comparable<AtomicTerm> {

    private DataType dataType;
    private Object value;

    public AtomicTerm(Object value, DataType dataType) {
        this.value = value;
        this.dataType = dataType;
    }

    public AtomicTerm(ByteBuffer byteBuffer) {
        // Get data type
        this.dataType = DataType.values()[byteBuffer.get()];

        if (dataType == AtomicTerm.DataType.DATA_TYPE_STRING) {
            int strLen = byteBuffer.getShort();
            byte[] stringData = new byte[strLen];
            byteBuffer.get(stringData);
            value = new String(stringData);
        } else if (dataType == AtomicTerm.DataType.DATA_TYPE_LONG) {
            value = byteBuffer.getLong();
        } else if (dataType == AtomicTerm.DataType.DATA_TYPE_INT) {
            value = byteBuffer.getInt();
        } else if (dataType == AtomicTerm.DataType.DATA_TYPE_BYTE) {
            value = (int) byteBuffer.get();
        } else if (dataType == AtomicTerm.DataType.DATA_TYPE_DOUBLE) {
            value = byteBuffer.getDouble();
        }
    }

    public DataType getDataType() {
        return dataType;
    }

    public byte[] serialize() {
        // Set data type
        int dataType = getDataType().ordinal();

        ByteBuffer buffer = null;
        // Write value
        if (getDataType() == AtomicTerm.DataType.DATA_TYPE_STRING) {
            String s = (String) getValue();
            byte[] stringData = s.getBytes();
            if (stringData.length > Short.MAX_VALUE) {
                throw new IllegalArgumentException(String.format("A single term must be at most %d bytes", Short.MAX_VALUE));
            }
            buffer = ByteBuffer.allocate(1 + 2 + stringData.length);
            buffer.put((byte) dataType);
            buffer.putShort((short) stringData.length);
            buffer.put(stringData);
        } else if (getDataType() == AtomicTerm.DataType.DATA_TYPE_LONG) {
            Long value = (Long) getValue();
            buffer = ByteBuffer.allocate(1 + 8);
            buffer.put((byte) dataType);
            buffer.putLong(value);
        } else if (getDataType() == AtomicTerm.DataType.DATA_TYPE_INT) {
            Integer value = (Integer) getValue();
            buffer = ByteBuffer.allocate(1 + 4);
            buffer.put((byte) dataType);
            buffer.putInt(value);
        } else if (getDataType() == AtomicTerm.DataType.DATA_TYPE_BYTE) {
            Byte value = (Byte) getValue();
            buffer = ByteBuffer.allocate(1 + 1);
            buffer.put((byte) dataType);
            buffer.put(value);
        } else if (getDataType() == AtomicTerm.DataType.DATA_TYPE_DOUBLE) {
            Double value = (Double) getValue();
            buffer = ByteBuffer.allocate(1 + 8);
            buffer.put((byte) dataType);
            buffer.putDouble(value);
        }

        return buffer.array();
    }


    public Object getValue() {
        return value;
    }

    public int compareTo(AtomicTerm o) {
        return value.toString().compareTo(o.getValue().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AtomicTerm that = (AtomicTerm) o;

        if (dataType != that.dataType) return false;
        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = dataType != null ? dataType.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public enum DataType {
        DATA_TYPE_STRING,
        DATA_TYPE_BYTE,
        DATA_TYPE_INT,
        DATA_TYPE_LONG,
        DATA_TYPE_DOUBLE;
    }

}
