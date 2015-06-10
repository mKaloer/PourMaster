package com.kaloer.searchlib.index.postings;

import com.kaloer.searchlib.index.PartialIndexData;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.util.IOIterator;
import com.kaloer.searchlib.index.util.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mkaloer on 12/04/15.
 */
public abstract class Postings {

    public abstract IOIterator<PostingsData> getDocumentsForTerm(long index, int docCount) throws IOException;

    public abstract long insertTerm(PostingsData[] docs) throws IOException;

    /**
     * Wrties a partial postings list to a file which is later merged with other similar files. Please note that this
     * may be called on any postings instance and thus it should not access the state of the object.
     * @param file The output file.
     * @param partialIndex The partial postings to write.
     *
     * @return A list of {@code <term, postings_index>} pairs.
     */
    public abstract ArrayList<Tuple<Term, Long>> writePartialPostingsToFile(String file, PartialIndexData partialIndex) throws IOException;

    /**
     * Merges partial postings files into one (and replaces existing if exists).
     * @param partialFiles The list of partial postings files to merge.
     * @param termsToPointer A mapping from terms to pointer in each partial file.
     * @param docFreqs A mapping from terms to their document frequencies in each partial file.
     *
     * @return A map of {@code <term, postings_index>} pairs.
     */
    public abstract HashMap<Term, Long> mergePartialPostingsFiles(ArrayList<String> partialFiles,
                                                                  ArrayList<ArrayList<Tuple<Term, Long>>> termsToPointer,
                                                                  ArrayList<HashMap<Term, Integer>> docFreqs) throws IOException;
}
