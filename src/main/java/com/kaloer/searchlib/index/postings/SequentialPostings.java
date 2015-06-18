package com.kaloer.searchlib.index.postings;

import com.kaloer.searchlib.index.PartialIndexData;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.terms.TermOccurrence;
import com.kaloer.searchlib.index.util.IOIterator;
import com.kaloer.searchlib.index.util.Tuple;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

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

    public IOIterator<PostingsData> getDocumentsForTerm(long index, int docCount) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        file.seek(index);
        return new BufferedPostingsIterator(file, docCount, this);
    }

    protected PostingsData readPostingsData(RandomAccessFile file) throws IOException {
        long docId = file.readLong();
        int arrLength = file.readInt();
        ArrayList<TermOccurrence> positions = new ArrayList<TermOccurrence>(arrLength);
        for (int j = 0; j < arrLength; j++) {
            positions.add(new TermOccurrence(file.readLong(), file.readInt()));
        }
        return new PostingsData(docId, positions);
    }

    public ArrayList<Tuple<Term, Long>> batchInsertTerm(ArrayList<Tuple<Term, PostingsData[]>> docs) throws IOException {
        return this.batchInsertTerm(docs, filePath);
    }

    public ArrayList<Tuple<Term, Long>> batchInsertTerm(ArrayList<Tuple<Term, PostingsData[]>> docs, String outputFile) throws IOException {
        ArrayList<Tuple<Term, Long>> indices = new ArrayList<Tuple<Term, Long>>(docs.size());
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(outputFile, "rw");
            long pointer = file.length();
            file.seek(pointer);
            int i = 0;
            for(Tuple<Term, PostingsData[]> termData : docs) {
                indices.add(new Tuple<Term, Long>(termData.getFirst(), file.getFilePointer()));
                writeDocsToFile(file, termData.getSecond());
            }
        } finally {
            if(file != null) {
                file.close();
            }
        }
        return indices;
    }

    public long insertTerm(PostingsData[] docs) throws IOException {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(filePath, "rw");
            long pointer = file.length();
            file.seek(pointer);
            writeDocsToFile(file, docs);
            return pointer;
        } finally {
            if(file != null) {
                file.close();
            }
        }
    }

    private void writeDocsToFile(DataOutput output, PostingsData[] docs) throws IOException {
        for(int i = 0; i < docs.length; i++) {
            output.writeLong(docs[i].getDocumentId());
            output.writeInt(docs[i].getPositions().size());
            for (TermOccurrence occurrence : docs[i].getPositions()) {
                output.writeLong(occurrence.getPosition());
                output.writeInt(occurrence.getFieldId());
            }
        }
    }

    @Override
    public ArrayList<Tuple<Term, Long>> writePartialPostingsToFile(String file, PartialIndexData partialIndex) throws IOException {
        Map<Term, HashMap<Long, PostingsData>> postings;
        SequentialPostings partialPostings = new SequentialPostings(file);
        // Sort by term
        ArrayList<Tuple<Term, PostingsData[]>> termDataList = new ArrayList<Tuple<Term, PostingsData[]>>();
        for(Tuple<Term, HashMap<Long, PostingsData>> term : partialIndex.getSortedPostings()) {
            TreeMap<Long, PostingsData> sortedDocs = new TreeMap<Long, PostingsData>();
            sortedDocs.putAll(term.getSecond());
            PostingsData[] termData = new PostingsData[sortedDocs.size()];
            int j = 0;
            for(Map.Entry<Long, PostingsData> doc : sortedDocs.entrySet()) {
                termData[j++] = doc.getValue();
            }
            termDataList.add(new Tuple<Term, PostingsData[]>(term.getFirst(), termData));
        }
        // Insert term data in file
        return partialPostings.batchInsertTerm(termDataList, file);
    }

    @Override
    public HashMap<Term, Long> mergePartialPostingsFiles(ArrayList<String> partialFiles,
                                                         ArrayList<ArrayList<Tuple<Term, Long>>> termsToPointer,
                                                         ArrayList<HashMap<Term, Integer>> docFreqs) throws IOException {
        RandomAccessFile outputFile = null;
        // List of input files
        ArrayList<RandomAccessFile> inputFiles = new ArrayList<RandomAccessFile>(partialFiles.size());
        // Map from term to postings index (in merged file)
        HashMap<Term, Long> indices = new HashMap<Term, Long>();
        // Keep index of terms currently merged per partial file
        ArrayList<Integer> termIndices = new ArrayList<Integer>(partialFiles.size());
        // Buffer containing one postings entry per file
        ArrayList<PostingsData> partialData = new ArrayList<PostingsData>(partialFiles.size());
        // Whether files have not reached EOF
        ArrayList<Boolean> currentFiles = new ArrayList<Boolean>();
        // Number of documents written for a specific term (per file)
        ArrayList<HashMap<Term, Integer>> docsWritten = new ArrayList<HashMap<Term, Integer>>();

        try {
            outputFile = new RandomAccessFile(filePath, "rw");

            // Open all partial files
            for(String partialPath : partialFiles) {
                inputFiles.add(new RandomAccessFile(partialPath, "r"));
                termIndices.add(0);
                currentFiles.add(true);
                partialData.add(null);
                docsWritten.add(new HashMap<Term, Integer>());
            }

            // While some files contain unread data
            while(currentFiles.contains(true)) {
                Term minTerm = null;
                long minDocId = -1;
                int minIndex = -1;
                // Merge step
                // Find minimum key in all files at their current position
                for (int i = 0; i < inputFiles.size(); i++) {
                    if(!currentFiles.get(i) || termsToPointer.get(i).size() == 0) {
                        continue;
                    }

                    Tuple<Term, Long> item = termsToPointer.get(i).get(termIndices.get(i));
                    Term t = item.getFirst();
                    if (partialData.get(i) == null) {
                        // Seek to term location if not already visited
                        if(inputFiles.get(i).getFilePointer() < item.getSecond()) {
                            inputFiles.get(i).seek(item.getSecond());
                        }
                        // Read data
                        partialData.set(i, readPostingsData(inputFiles.get(i)));

                        // Check for end of file and mark for next iteration
                        if(inputFiles.get(i).getFilePointer() >= inputFiles.get(i).length()) {
                            currentFiles.set(i, false);
                        }
                    }
                    // Check if this is the smallest key (and update if so)
                    if (minTerm == null || t.compareTo(minTerm) > 0) {
                        if (minDocId == -1 || minDocId > partialData.get(i).getDocumentId()) {
                            minTerm = t;
                            minDocId = partialData.get(i).getDocumentId();
                            minIndex = i;
                        }
                    }
                }

                // If first occurrence of term, store pointer to postings index
                if(!indices.containsKey(minTerm)) {
                    indices.put(minTerm, outputFile.getFilePointer());
                }
                if(minIndex >= 0) {
                    // Write postings to output file
                    writeDocsToFile(outputFile, new PostingsData[]{partialData.get(minIndex)});
                    // Mark buffer for this file as empty
                    partialData.set(minIndex, null);
                    // Update number of docs written with this term
                    if(!docsWritten.get(minIndex).containsKey(minTerm)) {
                        docsWritten.get(minIndex).put(minTerm, 0);
                    }
                    docsWritten.get(minIndex).put(minTerm, docsWritten.get(minIndex).get(minTerm) + 1);
                    // If all docs written, increment  term index
                    if(docsWritten.get(minIndex).get(minTerm) == docFreqs.get(minIndex).get(minTerm)) {
                        termIndices.set(minIndex, termIndices.get(minIndex) + 1);
                    }
                }
            }

            return indices;
        } finally {
            if(outputFile != null) {
                outputFile.close();
            }
            for(RandomAccessFile f : inputFiles) {
                if(f != null) {
                    f.close();
                }
            }
        }
    }
}
