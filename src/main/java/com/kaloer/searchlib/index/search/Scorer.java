package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.PostingsData;

/**
 * Created by mkaloer on 06/05/15.
 */
public interface Scorer {

    double scoreDocument(PostingsData data);

}
