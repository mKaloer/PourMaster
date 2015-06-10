package com.kaloer.searchlib.index.postings;

import com.kaloer.searchlib.index.terms.TermOccurrence;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by mkaloer on 12/04/15.
 */
public class PostingsData {
    private long documentId;
    private ArrayList<TermOccurrence> positions;

    public PostingsData(long docId, Collection<TermOccurrence> positions) {
        this.documentId = docId;
        this.positions = new ArrayList<TermOccurrence>(positions);
    }

    public PostingsData(long docId) {
        this.documentId = docId;
        this.positions = new ArrayList<TermOccurrence>();
    }

    public PostingsData(long docId, long position, int fieldId) {
        this(docId, new TermOccurrence(position, fieldId));
    }

    public PostingsData(long docId, TermOccurrence occurrence) {
        this.documentId = docId;
        this.positions = new ArrayList<TermOccurrence>();
        this.positions.add(occurrence);
    }

    public ArrayList<TermOccurrence> getPositions() {
        return positions;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void addPosition(long position, int fieldId) {
        addPosition(new TermOccurrence(position, fieldId));
    }

    public void addPosition(TermOccurrence position) {
        positions.add(position);
    }

}
