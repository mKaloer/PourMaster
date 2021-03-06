package pourmaster.postings;

import pourmaster.util.IOIterator;
import pourmaster.IndexConfig;
import pourmaster.PartialIndexData;
import pourmaster.terms.Term;
import pourmaster.terms.TermOccurrence;
import pourmaster.util.Tuple;

import java.io.*;
import java.util.*;

/**
 * Sequential implementation of a postings list.
 */
public class SequentialPostings implements Postings {

    public static final String CONFIG_FILE_ID = "postings.file";
    private static final String DEFAULT_FILE_NAME = "postings.db";
    private String filePath;

    public SequentialPostings() {
        // Empty constructor
    }

    /**
     * Private constructor used for temporary postings file.
     * @param file The postings file path.
     * @throws IOException If the file cannot be created.
     */
    private SequentialPostings(String file) throws IOException {
        this.filePath = file;
        File f = new File(this.filePath);
        if (!f.exists()) {
            f.createNewFile();
        }
    }

    public void init(IndexConfig conf) throws IOException {
        if (conf.containsKey(CONFIG_FILE_ID)) {
            this.filePath = conf.get(CONFIG_FILE_ID);
        } else {
            this.filePath = new File(conf.getBaseDirectory(), DEFAULT_FILE_NAME).getPath();
        }
        File f = new File(this.filePath);
        if (!f.exists()) {
            f.createNewFile();
        }
    }

    public void deleteAll() throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        file.setLength(0);
    }

    public IOIterator<PostingsData> getDocumentsForTerm(long index, int docCount) throws IOException {
        DataInputStream file = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
        file.skip(index);
        return new BufferedPostingsIterator(file, docCount, this);
    }

    protected PostingsData readPostingsData(DataInputStream file) throws IOException {
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
        File file = new File(outputFile);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
        try {
            long filePointer = file.length();
            for (Tuple<Term, PostingsData[]> termData : docs) {
                indices.add(new Tuple<Term, Long>(termData.getFirst(), filePointer));
                filePointer += writeDocsToFile(out, termData.getSecond());
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return indices;
    }

    public long insertTerm(PostingsData[] docs) throws IOException {
        File file = new File(filePath);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath, true)));
        try {

            long pointer = file.length();
            writeDocsToFile(out, docs);
            return pointer;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private long writeDocsToFile(DataOutput output, PostingsData[] docs) throws IOException {
        int written = 0;
        for (PostingsData doc : docs) {
            output.writeLong(doc.getDocumentId());
            output.writeInt(doc.getPositions().size());
            for (TermOccurrence occurrence : doc.getPositions()) {
                output.writeLong(occurrence.getPosition());
                output.writeInt(occurrence.getFieldId());
            }
            written += (8 + 4) * (doc.getPositions().size() + 1);
        }
        return written;
    }

    public ArrayList<Tuple<Term, Long>> writePartialPostingsToFile(String file, PartialIndexData partialIndex) throws IOException {
        SequentialPostings partialPostings = new SequentialPostings(file);
        // Sort by term
        ArrayList<Tuple<Term, PostingsData[]>> termDataList = new ArrayList<Tuple<Term, PostingsData[]>>();
        for (Tuple<Term, HashMap<Long, PostingsData>> term : partialIndex.getSortedPostings()) {
            PriorityQueue<Tuple<Long, PostingsData>> sortedDocs = new PriorityQueue<Tuple<Long, PostingsData>>(term.getSecond().size(),
                    new Comparator<Tuple<Long, PostingsData>>() {
                public int compare(Tuple<Long, PostingsData> o1, Tuple<Long, PostingsData> o2) {
                    return o1.getFirst().compareTo(o2.getFirst());
                }
            });
            for (Map.Entry<Long, PostingsData> doc : term.getSecond().entrySet()) {
                sortedDocs.add(new Tuple<Long, PostingsData>(doc.getKey(), doc.getValue()));
            }
            PostingsData[] termData = new PostingsData[sortedDocs.size()];
            int j = 0;
            while (sortedDocs.size() > 0) {
                termData[j++] = sortedDocs.poll().getSecond();
            }
            termDataList.add(new Tuple<Term, PostingsData[]>(term.getFirst(), termData));
        }
        // Insert term data in file
        return partialPostings.batchInsertTerm(termDataList, file);
    }

    public HashMap<Term, Long> mergePartialPostingsFiles(ArrayList<String> partialFiles,
                                                         ArrayList<ArrayList<Tuple<Term, Long>>> termsToPointer,
                                                         HashMap<Term, List<Integer>> docFreqs) throws IOException {
        DataOutputStream outputFile = null;
        long fileIndex = 0;
        // List of input files
        ArrayList<DataInputStream> inputFiles = new ArrayList<DataInputStream>(partialFiles.size());
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
            outputFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath, true)));

            // Open all partial files
            for (String partialPath : partialFiles) {
                inputFiles.add(new DataInputStream(new BufferedInputStream(new FileInputStream(partialPath))));
                termIndices.add(0);
                currentFiles.add(true);
                partialData.add(null);
                docsWritten.add(new HashMap<Term, Integer>());
            }

            // While some files contain unread data
            while (currentFiles.contains(true)) {
                Term minTerm = null;
                long minDocId = -1;
                int minIndex = -1;
                // Merge step
                // Find minimum key in all files at their current position
                for (int i = 0; i < inputFiles.size(); i++) {
                    if (!currentFiles.get(i)) {
                        continue;
                    }

                    Tuple<Term, Long> item = termsToPointer.get(i).get(termIndices.get(i));
                    Term t = item.getFirst();
                    if (partialData.get(i) == null) {
                        // Read data
                        partialData.set(i, readPostingsData(inputFiles.get(i)));
                    }
                    // Check if this is the smallest key (and update if so)
                    int comparison = minTerm == null ? -1 : t.compareTo(minTerm);
                    if (comparison < 0 || (comparison == 0 && minDocId > partialData.get(i).getDocumentId())) {
                        minTerm = t;
                        minDocId = partialData.get(i).getDocumentId();
                        minIndex = i;
                    }
                }

                // If first occurrence of term, store pointer to postings index
                if (!indices.containsKey(minTerm)) {
                    indices.put(minTerm, fileIndex);
                }
                if (minIndex >= 0) {
                    // Write postings to output file
                    fileIndex += writeDocsToFile(outputFile, new PostingsData[]{partialData.get(minIndex)});
                    // Mark buffer for this file as empty
                    partialData.set(minIndex, null);
                    // Update number of docs written with this term
                    if (!docsWritten.get(minIndex).containsKey(minTerm)) {
                        docsWritten.get(minIndex).put(minTerm, 0);
                    }
                    docsWritten.get(minIndex).put(minTerm, docsWritten.get(minIndex).get(minTerm) + 1);
                    // If all docs written, increment term index or mark file as complete
                    if (docsWritten.get(minIndex).get(minTerm).equals(docFreqs.get(minTerm).get(minIndex))) {
                        if (termIndices.get(minIndex) == termsToPointer.get(minIndex).size() - 1) {
                            // End of file.
                            currentFiles.set(minIndex, false);
                        } else {
                            termIndices.set(minIndex, termIndices.get(minIndex) + 1);
                        }
                    }
                }
            }

            return indices;
        } finally {
            if (outputFile != null) {
                outputFile.close();
            }
            for (InputStream in : inputFiles) {
                if (in != null) {
                    in.close();
                }
            }
        }
    }
}
