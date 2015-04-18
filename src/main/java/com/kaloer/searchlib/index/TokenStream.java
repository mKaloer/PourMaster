package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by mkaloer on 15/04/15.
 */
public class TokenStream implements Iterable<Token> {

    private Field field;
    private Pipeline<Object, Token> fieldPipeline;
    private ArrayList<Token> tokens;

    public TokenStream(Field f) {
        this.field = f;
        fieldPipeline = f.getIndexAnalysisPipeline();
        // Process data
        try {
            tokens = fieldPipeline.process(f.getFieldValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
            tokens = new ArrayList<Token>();
        }
    }

    public Iterator<Token> iterator() {
        return tokens.iterator();
    }
}
