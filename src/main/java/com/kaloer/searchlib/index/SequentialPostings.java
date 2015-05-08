package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.terms.TermOccurrence;

import java.io.*;
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

    public Iterator<PostingsData> getDocumentsForTerm(long index, int docCount) throws IOException {
        RandomAccessFile file = null;
        ArrayList<PostingsData> docs;
        // TODO: Do not read all docs but buffer with iterator
        try {
            file = new RandomAccessFile(filePath, "r");
            file.seek(index);
            docs = new ArrayList<PostingsData>(docCount);
            for(int i = 0; i < docCount; i++) {
                docs.add(readPostingsData(file));
            }
        } finally {
            if(file != null) {
                file.close();
            }
        }
        return docs.iterator();
    }

    private PostingsData readPostingsData(RandomAccessFile file) throws IOException {
        long docId = file.readLong();
        int arrLength = file.readInt();
        ArrayList<TermOccurrence> positions = new ArrayList<TermOccurrence>(arrLength);
        for (int j = 0; j < arrLength; j++) {
            positions.add(new TermOccurrence(file.readLong(), file.readInt()));
        }
        return new PostingsData(docId, positions);
    }

    public ArrayList<Map.Entry<Term, Long>> batchInsertTerm(ArrayList<Map.Entry<Term, PostingsData[]>> docs) throws IOException {
        return this.batchInsertTerm(docs, filePath);
    }

    public ArrayList<Map.Entry<Term, Long>> batchInsertTerm(ArrayList<Map.Entry<Term, PostingsData[]>> docs, String outputFile) throws IOException {
        ArrayList<Map.Entry<Term, Long>> indices = new ArrayList<Map.Entry<Term, Long>>(docs.size());
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(outputFile, "rw");
            long pointer = file.length();
            file.seek(pointer);
            int i = 0;
            for(Map.Entry<Term, PostingsData[]> termData : docs) {
                indices.add(new AbstractMap.SimpleEntry<Term, Long>(termData.getKey(), file.getFilePointer()));
                writeDocsToFile(file, termData.getValue());
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
    public ArrayList<Map.Entry<Term, Long>> writePartialPostingsToFile(String file, Map<Term, HashMap<Long, PostingsData>> postings) throws IOException {
        SequentialPostings partialPostings = new SequentialPostings(file);
        // Sort by term
        TreeMap<Term, HashMap<Long, PostingsData>> sortedDict = new TreeMap<Term, HashMap<Long, PostingsData>>();
        sortedDict.putAll(postings);
        ArrayList<Map.Entry<Term, PostingsData[]>> termDataList = new ArrayList<Map.Entry<Term, PostingsData[]>>(sortedDict.size());
        for(Map.Entry<Term, HashMap<Long, PostingsData>> term : sortedDict.entrySet()) {
            TreeMap<Long, PostingsData> sortedDocs = new TreeMap<Long, PostingsData>();
            sortedDocs.putAll(term.getValue());
            PostingsData[] termData = new PostingsData[sortedDocs.size()];
            int j = 0;
            for(Map.Entry<Long, PostingsData> doc : sortedDocs.entrySet()) {
                termData[j++] = doc.getValue();
            }
            termDataList.add(new AbstractMap.SimpleEntry<Term, PostingsData[]>(term.getKey(), termData));
        }
        // Insert term data in file
        return partialPostings.batchInsertTerm(termDataList, file);
    }

    @Override
    public HashMap<Term, Long> mergePartialPostingsFiles(ArrayList<String> partialFiles, ArrayList<ArrayList<Map.Entry<Term, Long>>> termsToPointer) throws IOException {
        RandomAccessFile outputFile = null;
        ArrayList<RandomAccessFile> inputFiles = new ArrayList<RandomAccessFile>(partialFiles.size());
        HashMap<Term, Long> indices = new HashMap<Term, Long>();
        // Keep index of terms currently merged per partial file
        ArrayList<Integer> termIndices = new ArrayList<Integer>(partialFiles.size());
        ArrayList<PostingsData> partialData = new ArrayList<PostingsData>(partialFiles.size());
        ArrayList<Boolean> currentFiles = new ArrayList<Boolean>();

        try {
            outputFile = new RandomAccessFile(filePath, "rw");

            // Open all partial files
            for(String partialPath : partialFiles) {
                inputFiles.add(new RandomAccessFile(partialPath, "r"));
                termIndices.add(0);
                currentFiles.add(true);
                partialData.add(null);
            }

            while(currentFiles.contains(true)) {
                Term minTerm = null;
                long minDocId = -1;
                int minIndex = -1;
                // Merge
                // Find minimum key
                for (int i = 0; i < inputFiles.size(); i++) {
                    if(!currentFiles.get(i) || termsToPointer.get(i).size() == 0) {
                        continue;
                    }

                    if (partialData.get(i) == null) {
                        // Read data
                        inputFiles.get(i).seek(termsToPointer.get(i).get(termIndices.get(i)).getValue());
                        partialData.set(i, readPostingsData(inputFiles.get(i)));

                        // Check for end of file and mark for next iteration
                        if(inputFiles.get(i).getFilePointer() >= inputFiles.get(i).length()) {
                            currentFiles.set(i, false);
                        }
                    }
                    Term t = termsToPointer.get(i).get(termIndices.get(i)).getKey();
                    if (minTerm == null || t.compareTo(minTerm) > 0) {
                        if (minDocId == -1 || minDocId > partialData.get(i).getDocumentId()) {
                            minTerm = t;
                            minDocId = partialData.get(i).getDocumentId();
                            minIndex = i;
                        }
                    }
                }
                if(!indices.containsKey(minTerm)) {
                    indices.put(minTerm, outputFile.getFilePointer());
                }
                // Write postings to output file
                writeDocsToFile(outputFile, new PostingsData[]{partialData.get(minIndex)});
                partialData.set(minIndex, null);
                termIndices.set(minIndex, termIndices.get(minIndex) + 1);
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
