package com.kaloer.pourmaster.postings;

import com.kaloer.pourmaster.IndexConfig;
import com.kaloer.pourmaster.PartialIndexData;
import com.kaloer.pourmaster.terms.Term;
import com.kaloer.pourmaster.util.IOIterator;
import com.kaloer.pourmaster.util.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Interface for Postings list used to retrieve documents for a given term.
 */
public interface Postings {

    IOIterator<PostingsData> getDocumentsForTerm(long index, int docCount) throws IOException;

    /**
     * Wrties a partial postings list to a file which is later merged with other similar files. Please note that this
     * may be called on any postings instance and thus it should not access the state of the object.
     *
     * @param file         The output file.
     * @param partialIndex The partial postings to write.
     * @return A list of {@code <term, postings_index>} pairs.
     */
    ArrayList<Tuple<Term, Long>> writePartialPostingsToFile(String file, PartialIndexData partialIndex) throws IOException;

    /**
     * Merges partial postings files into one (and replaces existing if exists).
     *
     * @param partialFiles   The list of partial postings files to merge.
     * @param termsToPointer A mapping from terms to pointer in each partial file.
     * @param docFreqs       A mapping from terms to their document frequencies in each partial file.
     * @return A map of {@code <term, postings_index>} pairs.
     */
    HashMap<Term, Long> mergePartialPostingsFiles(ArrayList<String> partialFiles,
                                                  ArrayList<ArrayList<Tuple<Term, Long>>> termsToPointer,
                                                  HashMap<Term, List<Integer>> docFreqs) throws IOException;

    void init(IndexConfig conf) throws IOException;

    void deleteAll() throws IOException;
}
