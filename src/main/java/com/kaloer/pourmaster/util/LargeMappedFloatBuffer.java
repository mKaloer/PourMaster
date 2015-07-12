package com.kaloer.pourmaster.util;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * {@link MappedByteBuffer} wrapper which allows for reading floats in large files.
 */
public class LargeMappedFloatBuffer {

    // Float size in bytes
    private final static int FLOAT_SIZE = Float.SIZE / 8;

    private MappedByteBuffer buffer;
    private FileChannel channel;
    private FileChannel.MapMode mapMode;
    private final long size;
    private final long offset;
    private int currentIndex = -1;
    private final int bufferSize;

    /**
     *
     * @param channel The file channel pointing at the file to map.
     * @param mapMode The file map mode.
     * @param size The size of the buffer (in number of floats).
     */
    public LargeMappedFloatBuffer(FileChannel channel, FileChannel.MapMode mapMode, long size) {
        this(channel, mapMode, size, 0);
    }

    /**
     *
     * @param channel The file channel pointing at the file to map.
     * @param mapMode The file map mode.
     * @param size The size of the buffer (in number of floats).
     * @param offset The offset of the file (in bytes).
     */
    public LargeMappedFloatBuffer(FileChannel channel, FileChannel.MapMode mapMode, long size, long offset) {
        this.channel = channel;
        this.mapMode = mapMode;
        this.size = size;
        this.offset = offset;
        this.bufferSize = (int) Math.min(Integer.MAX_VALUE, size * FLOAT_SIZE);
    }

    /**
     * Get a float from the buffer
     * @param position The position of the float
     * @return The float value
     * @throws IOException If the file could not be mapped.
     */
    public float get(long position) throws IOException {
        seek(position);
        int relPos = positionToRelative(position);
        if (relPos + FLOAT_SIZE > bufferSize) {
            throw new IndexOutOfBoundsException();
        }
        return buffer.getFloat(relPos);
    }

    /**
     * Inserts a float into the buffer.
     * @param position The position of the float.
     * @param value The value of the float.
     * @throws IOException If the file could not be mapped.
     */
    public void put(long position, float value) throws IOException {
        seek(position);
        int relPos = positionToRelative(position);
        buffer.putFloat(relPos, value);
    }

    /**
     * Maps the buffer needed to reach the float at the given position.
     * @param position The position to read.
     * @throws IOException If the file could not be mapped.
     */
    private void seek(long position) throws IOException {
        if (position > size) {
            throw new IndexOutOfBoundsException();
        }

        int index = (int) (position / bufferSize);
        if (index != currentIndex) {
            // Change buffer
            if (buffer != null) {
                buffer.force();
            }
            buffer = channel.map(mapMode, index * bufferSize + this.offset, bufferSize);
            currentIndex = index;
        }
    }

    /**
     * Converts an absolute position to a position relative to the current index.
     * @param position The absolute position.
     * @return The relative position.
     */
    private int positionToRelative(long position) {
        return (int) (position * FLOAT_SIZE - (currentIndex * bufferSize));
    }

}
