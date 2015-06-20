package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.Document;
import com.kaloer.searchlib.index.InvertedIndex;
import com.kaloer.searchlib.index.TermDictionary;
import com.kaloer.searchlib.index.postings.PostingsData;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.terms.TermOccurrence;
import com.kaloer.searchlib.index.util.IOIterator;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Query for matching terms with a single wildcard, including prefix-queries and suffix-queries.
 */
public class WildcardQuery extends Query {

    private Term prefix;
    private Term suffix;
    private String fieldName;

    public WildcardQuery(Term prefix, Term suffix, String fieldName) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.fieldName = fieldName;
    }

    @Override
    public Iterator<RankedDocument<Document>> search(InvertedIndex index) throws IOException {
        List<TermDictionary.TermData> matches = index.getDictionary().findTerm(prefix, suffix);
        if (matches.size() == 0) {
            // No results
            return Collections.emptyIterator();
        }
        int fieldId = index.getDocIndex().getFieldDataStore().getField(fieldName).getFieldId();
        PriorityQueue<RankedDocument<Document>> result = new PriorityQueue<RankedDocument<Document>>();
        for (TermDictionary.TermData termData : matches) {
            IOIterator<PostingsData> postingsData = index.getPostings().getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
            // Normalize scores and add to result set
            while (postingsData.hasNext()) {
                PostingsData data = postingsData.next();
                long docId = data.getDocumentId();
                Document doc = index.getDocIndex().getDocument(docId);
                // Calculate tf
                int tf = 0;
                for (TermOccurrence occurrence : data.getPositions()) {
                    if (occurrence.getFieldId() == fieldId) {
                        tf++;
                    }
                }
                if (tf > 0) {
                    // Return pure TF score
                    result.add(new RankedDocument<Document>(doc, tf));
                }
            }
        }
        return result.iterator();
    }

    public String getFieldName() {
        return fieldName;
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
