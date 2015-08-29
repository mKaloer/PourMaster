package com.kaloer.pourmaster.postings;

import com.kaloer.pourmaster.terms.TermOccurrence;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Contains the data related to a specific document in the postings list.
 */
public class PostingsData {
    private final long documentId;
    private final ArrayList<TermOccurrence> positions;

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
