package pourmaster.search;

/**
 * Represents a document and its relevance score.
 */
public class RankedDocument<T extends Object> implements Comparable<RankedDocument> {

    private final T document;
    private final double score;

    public RankedDocument(T document, double score) {
        this.document = document;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public T getDocument() {
        return document;
    }

    public int compareTo(RankedDocument o) {
        return Double.compare(o.score, score);
    }

}
