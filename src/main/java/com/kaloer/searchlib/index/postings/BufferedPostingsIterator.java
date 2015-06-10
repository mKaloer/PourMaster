package com.kaloer.searchlib.index.postings;

import com.kaloer.searchlib.index.util.IOIterator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Buffered iterator for reading postings from disc
 */
public class BufferedPostingsIterator implements IOIterator<PostingsData> {

    private final RandomAccessFile file;
    private final int docCount;
    private final SequentialPostings sequentialPostings;
    private int index = 0;
    private int bufferLength;
    private Queue<PostingsData> buffer;

    public BufferedPostingsIterator(RandomAccessFile file, int docCount, SequentialPostings sequentialPostings) {
        this(file, docCount, sequentialPostings, 100);
    }

    public BufferedPostingsIterator(RandomAccessFile file, int docCount, SequentialPostings sequentialPostings, int bufferLength) {
        this.file = file;
        this.docCount = docCount;
        this.sequentialPostings = sequentialPostings;
        this.bufferLength = bufferLength;
        this.buffer = new LinkedList<PostingsData>();
    }

    public boolean hasNext() {
        return index < docCount;
    }

    public PostingsData next() throws IOException {
        // Fill buffer if empty
        if(buffer.isEmpty() && hasNext()) {
            for (int i = 0; i < Math.min(bufferLength, docCount - index); i++) {
                buffer.add(sequentialPostings.readPostingsData(file));
            }
            // Everything has been read: close file.
            if(index + buffer.size() == docCount) {
                file.close();
            }
        }
        index++;
        return buffer.poll();
    }
}