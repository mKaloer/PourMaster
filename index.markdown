---
layout: default
title: PourMaster
---
# PourMaster

PourMaster is a lightweight easy-to-use inverted index written in Java.

### Contents
  1. [Installation](#installation)
  2. [Usage](#usage)
  2. [Implementation](#implementation)

## Installation
TODO

## Usage

### Document Declaration
Different types of indexable documents are declared as classes with annotations.

For example, the following class represents a book. A `@Field` annotation indicates that a field is part of the document. Fields are typically indexed (searchable) and/or stored (retrievable). In the `Books` class, the `author` field is both indexed and stored, and analyzed using a `SimpleStringAnalyzer`. The `content` field is indexed but not stored. Storing a field takes up disc space, and in this case we do not want the content to be stored. The `id` field is stored but not indexed, as we will not be searching for an id but may need it to identify a book. In addition, a the `@Field` annotation sets the type of analyzer used for an indexed field. An analyzer defines how the content is pre-processed before being inserted into the index (e.g. tokenization, stemming etc).
{% highlight java linenos %}
public class Book {
    @Field(
           type = StringFieldType.class,
           indexed = true,
           stored = true,
           indexAnalyzer = SimpleStringAnalyzer.class
           )
    public String author;

    @Field(
           type = StringFieldType.class,
           indexAnalyzer = SimpleStringAnalyzer.class,
           indexed = true,
           stored = false
           )
    public String content;

    @Field(
           type = IntegerFieldType.class,
           indexed = false,
           stored = true
           )
    public int id;
}
{% endhighlight %}
An analyzer is defined as a subclass of the `Analyzer` class, and must override the `analyze()` method which returns an iterator of tokens.
{% highlight java linenos %}
public static class SimpleStringAnalyzer extends Analyzer<String> {

    @Override
    public Iterator<Token> analyze(String value) {
        final String[] words = value.split(" ");
        return new Iterator<Token>() {
            int index = 0;

            public boolean hasNext() {
                  return index < words.length;
			}

            public Token next() {
                  return new Token(new StringTerm(words[index]), index++);
            }

            public void remove() { }
        };
    }
}
{% endhighlight %}

### Creating an Index
A index is represented with the `InvertedIndex` class and configured with the `IndexConfig` class, as shown in the following example:
{% highlight java linenos %}
IndexConfig conf = new IndexConfig()
    .setDocumentIndex(SequentialDocumentIndex.class)
    .setBaseDirectory("idx")
    .setPostings(SequentialPostings.class)
    .setTermDictionary(BTreeTermDictionary.class)
    .set(BTreeTermDictionary.CONFIG_SUPPORT_WILDCARD_ID, Boolean.toString(wildcards))
    .setTmpDir(".tmp/");
InvertedIndex index = new InvertedIndex(conf);
{% endhighlight %}
The config file has reasonable default for most preferences.
### Indexing Documents
When an instance of the `InvertedIndex` class is created, we can start indexing documents. PourMaster currently only supports bulk insertion, meaning that all documents have to be indexed at once. Indexing is done with the `indexDocuments(Iterable<Object> docStream)` and `indexDocuments(Iterator<Object> docStream)` methods. These takes iterators of document classes as arguments:
{% highlight java linenos %}
ArrayList<Book> books = new ArrayList<Book>();
docs.add(book1);
docs.add(book2);
index.indexDocuments(books);
{% endhighlight %}

### Quering Documents
Search queries are defined as `Query` or `FieldQuery` subclasses. The most basic kind of query in PourMaster is the `TermQuery` class. It searches for a single specific term in a specific field. More fancy are the `WildCardQuery` and `PhraseQuery` query classes, which match terms with a single wildcard and multiple terms with specific relative positions, respectively. Each of these queries are `FieldQueries`, meaning that the query is only defined for a single field. Multiple instances of `FieldQuery` can be combined with the `MultiTermQuery` class. The following snippet shows how to search for "william shakespeare" using the `MultiTerm` query.

{% highlight java linenos %}
MultiTermQuery query = new MultiTermQuery();
query.add(new TermQuery(new StringTerm("William"), "author"));
query.add(new TermQuery(new StringTerm("Shakespeare"), "author"));
List<RankedDocument> results = index.search(query);
{% endhighlight %}

#### Document Scoring
The `TermQuery` ad `WildcardQuery` classes use length-normalized, smoothed [tf-idf](https://en.wikipedia.org/wiki/Tf%E2%80%93idf) weighting, while the `PhraseQuery` uses a plain length-normalized tf weighting. It is currently not possible to customize the scoring function. The `MultiTermQuery` uses a sum of the individual query scores.

## Implementation

### Data Stores
The main data stores of the PourMaster are the term dictionary, the postings lists, the document index and the field normalizations store.

#### The Term Dictionary
The term dictionary maps terms into an index corresponding to a [postings list](#the-postings-lists). It is implemented as a persistent B-tree from the [Apache Mavibot project](https://directory.apache.org/mavibot/){:target="_blank"}. If possible, the B-tree is kept in memory and regularly persisted on the disk. The term dictionary is created at index time and not changed afterwards. By default, PourMaster uses a single B-tree for all terms. If wildcard search is enabled, it will store an additional B-tree in which each term is stored in its reversed order (e.g. 'foo' is stored as 'oof').

#### The Postings Lists
A postings list is a list of occurrences of a specific term. It contains the id of every field the term occurs in, and the index(es) of the occurrence in each field. All postings lists are stored sequentially in a single file on the disk.

#### The Document Index
The *document index* maps a document id to an actual document with its stored field values. The document index consists of three files. One file stores the number of indexed documents (at index 0) and, for each document in ascending order, an index in the second file (the *field data store*), which contains the actual field values.

The third file, the *field info store*, contains general information about each field. This includes their names and ids as well as their type and whether they are stored and indexed. A mapping between field ids and field names is kept in memory for fast lookup.

#### The Field Normalizations Store
When documents are scored in a search, the score is normalized by the length of the corresponding field. To avoid retrieving all document data just for scoring the documents, the normalization factor is calculated at index-time and stored separately. It is represented as a matrix in which each row is a field and each column is a document.

To reduce the number of disk accesses, a memory mapped buffer is used per field to cache part of the corresponding row in memory. Because the columns are sorted by ascending document id, which also tend to be the order in which documents are scored, it is reasonable to assume that most reads will be cache hits.

### Indexing
TODO
