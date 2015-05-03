package com.kaloer.searchlib.index;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mkaloer on 12/04/15.
 */
public class PostingsData {
    private long documentId;
    private ArrayList<Long> positions;

    public PostingsData(long docId, Collection<Long> positions) {
        this.documentId = docId;
        this.positions = new ArrayList<Long>(positions);
    }

    public PostingsData(long docId, long position) {
        this.documentId = docId;
        this.positions = new ArrayList<Long>();
        addPosition(position);
    }

    public ArrayList<Long> getPositions() {
        return positions;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void addPosition(long position) {
        positions.add(position);
    }
}
