package com.kaloer.searchlib.index;

import java.util.ArrayList;

/**
 * Created by mkaloer on 15/04/15.
 */
public class AnalysisPipeline {

    private Tokenizer tokenizer;
    private ArrayList<TokenFilter> stages = new ArrayList<TokenFilter>();

    public AnalysisPipeline(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public AnalysisPipeline apply(TokenFilter filter) {
        stages.add(filter);
        return this;
    }

    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    public TokenStream getTokenStream() {
        return new TokenStre
    }

}
