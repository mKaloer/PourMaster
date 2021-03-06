package pourmaster.search;

import pourmaster.InvertedIndex;
import pourmaster.TermDictionary;
import pourmaster.fields.Field;
import pourmaster.postings.PostingsData;
import pourmaster.terms.Term;
import pourmaster.terms.TermOccurrence;
import pourmaster.util.IOIterator;
import pourmaster.util.PriorityQueueIterator;

import java.io.IOException;
import java.util.*;

/**
 * Query for matching terms with a single wildcard, including prefix-queries and suffix-queries.
 */
public class WildcardQuery extends FieldQuery {

    private final Term prefix;
    private final Term suffix;

    public WildcardQuery(Term prefix, Term suffix, String fieldName) {
        super(fieldName);
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public Iterator<RankedDocumentId> search(InvertedIndex index) throws IOException {
        List<TermDictionary.TermData> matches = index.getDictionary().findTerm(prefix, suffix);
        if (matches.size() == 0) {
            // No results
            return Collections.emptyIterator();
        }
        long documentCount = index.getDocIndex().getDocumentCount();
        Field queryField = index.getDocIndex().getFieldDataStore().getField(getField());
        int fieldId = queryField.getFieldId();
        PriorityQueue<RankedDocumentId> result = new PriorityQueue<RankedDocumentId>();
        for (TermDictionary.TermData termData : matches) {
            IOIterator<PostingsData> postingsData = index.getPostings().getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
            // Normalize scores and add to result set
            while (postingsData.hasNext()) {
                PostingsData data = postingsData.next();
                long docId = data.getDocumentId();
                // Calculate tf
                int tf = 0;
                for (TermOccurrence occurrence : data.getPositions()) {
                    if (occurrence.getFieldId() == fieldId) {
                        tf++;
                    }
                }
                if (tf > 0) {
                    double idf = Math.log((double) documentCount / (double) (1 + termData.getFieldDocFrequency(queryField.getFieldId())));
                    float fieldNorm = index.getFieldNormsStore().getFieldNorm(fieldId, docId);
                    result.add(new RankedDocumentId(docId, tf * idf * getBoost() * fieldNorm));
                }
            }
        }
        return new PriorityQueueIterator<RankedDocumentId>(result);
    }

    @Override
    public double getBoost() {
        return super.getBoost();
    }

    public Term getPrefix() {
        return prefix;
    }

    public Term getSuffix() {
        return suffix;
    }
}
