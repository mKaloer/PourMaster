package com.kaloer.searchlib.index.annotations;

import com.kaloer.searchlib.index.Analyzer;
import com.kaloer.searchlib.index.fields.FieldType;
import com.kaloer.searchlib.index.pipeline.Pipeline;
import com.kaloer.searchlib.index.pipeline.Stage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field {

    public Class<? extends FieldType> type();
    public boolean indexed() default true;
    public boolean stored() default false;
    public Class<? extends Analyzer> indexAnalyzer();

}
