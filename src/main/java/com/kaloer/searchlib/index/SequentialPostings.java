package com.kaloer.searchlib.index;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by mkaloer on 12/04/15.
 */
public class SequentialPostings extends Postings {

    private String filePath;
    public SequentialPostings(String file) throws IOException {
        this.filePath = file;
        File f = new File(file);
        f.createNewFile();
    }

    public PostingsData[] getDocumentsForTerm(long index, int docCount) throws IOException {
        RandomAccessFile file = null;
        PostingsData[] docs;
        try {
            file = new RandomAccessFile(filePath, "r");
            file.seek(index);
            docs = new PostingsData[docCount];
            for(int i = 0; i < docCount; i++) {
                long docId = file.readLong();
                int arrLength = file.readInt();
                long[] positions = new long[arrLength];
                for (int j = 0; j < arrLength; j++) {
                    positions[j] = file.readLong();
                }
                docs[i] = new PostingsData(docId, positions);
            }
        } finally {
            if(file != null) {
                file.close();
            }
        }
        return docs;
    }

    public long insertTerm(PostingsData[] docs) throws IOException {
        long pointer = -1;
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(filePath, "rw");
            pointer = file.length();
            file.seek(pointer);
            for(int i = 0; i < docs.length; i++) {
                file.writeLong(docs[i].getDocumentId());
                for (int j = 0; j < docs[i].getPositions().length; j++) {
                    file.writeLong(docs[i].getPositions()[j]);
                }
            }

        } finally {
            if(file != null) {
                file.close();
            }
        }
        return pointer;
    }

}
