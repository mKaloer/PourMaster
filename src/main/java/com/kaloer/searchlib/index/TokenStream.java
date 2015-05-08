package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.FieldType;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by mkaloer on 15/04/15.
 */
public class TokenStream<T, V extends FieldType<T>> implements Iterable<Token> {

    private Field<T, V> field;
    private Pipeline<T, Token> fieldPipeline;
    private ArrayList<Token> tokens;

    public TokenStream(Field<T, V> f) {
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

    public Field<T, V> getField() {
        return field;
    }

    public Iterator<Token> iterator() {
        return tokens.iterator();
    }
}
