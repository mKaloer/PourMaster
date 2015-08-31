package com.kaloer.pourmaster.example;

import org.apache.commons.cli.*;
import pourmaster.Token;
import pourmaster.search.MultiTermQuery;
import pourmaster.search.RankedDocument;
import pourmaster.search.TermQuery;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("i", false, "Index documents");
        options.addOption("s", true, "Wikipedia source file");
        options.addOption("q", true, "Search query");
        CommandLineParser parser = new DefaultParser();

        final boolean doIndex;
        final String wikiPath;
        final String queryString;
        try {
            CommandLine line = parser.parse(options, args);
            doIndex = line.hasOption("i");
            wikiPath = line.getOptionValue("s");
            queryString = line.getOptionValue("q");
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        try {
            WikipediaIndex index = new WikipediaIndex(wikiPath);
            if (doIndex) {
                index.indexWiki();
            }
            Iterator<Token> tokens = new WikiPage.SimpleStringAnalyzer().analyze(queryString);
            MultiTermQuery query = new MultiTermQuery();
            while (tokens.hasNext()) {
                Token t = tokens.next();
                // Search for token in the 'content' field
                query.add(new TermQuery(t.getValue(), "content"));
            }
            List<RankedDocument> result = index.getIndex().search(query);
            System.out.println(String.format("Got %s results!", result.size()));
            int i = 0;
            for (RankedDocument doc : result) {
                com.kaloer.pourmaster.example.WikiPage page = (com.kaloer.pourmaster.example.WikiPage) doc.getDocument();
                System.out.println(String.format("%s (%f)", page.title, doc.getScore()));
                if (i > 20)  {
                    break;
                }
                i++;
            }
            index.close();
        } catch (IOException | ReflectiveOperationException e) {
            e.printStackTrace();

        }
    }

}
