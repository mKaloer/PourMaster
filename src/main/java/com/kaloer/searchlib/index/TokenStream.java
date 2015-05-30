package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.FieldData;
import com.kaloer.searchlib.index.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by mkaloer on 15/04/15.
 */
public class TokenStream implements Iterable<Token> {

    private FieldData field;
    private Pipeline fieldPipeline;
    private ArrayList<Token> tokens;

    public TokenStream(FieldData f) {
        this.field = f;
        fieldPipeline = f.getField().getIndexAnalysisPipeline();
        // Process data
        try {
            tokens = fieldPipeline.process(f.getValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
            tokens = new ArrayList<Token>();
        }
    }

    public FieldData getFieldData() {
        return field;
    }

    public Iterator<Token> iterator() {
        return tokens.iterator();
    }
}
