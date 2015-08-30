package pourmaster.search;

/**
 * Represents a document by id and its relevance score.
 */
public class RankedDocumentId extends RankedDocument<Long> {

    public RankedDocumentId(long documentId, double score) {
        super(documentId, score);
    }

}
