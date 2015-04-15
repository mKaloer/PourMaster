package com.kaloer.searchlib.index;

import java.io.IOException;

/**
 * Created by mkaloer on 12/04/15.
 */
public abstract class Postings {

    public abstract PostingsData[] getDocumentsForTerm(long index, int docCount) throws IOException;

    public abstract long insertTerm(PostingsData[] docs) throws IOException;
}
