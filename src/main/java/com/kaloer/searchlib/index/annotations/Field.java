package com.kaloer.searchlib.index.annotations;

import com.kaloer.searchlib.index.Analyzer;
import com.kaloer.searchlib.index.fields.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field {

    Class<? extends FieldType> type();

    boolean indexed() default true;

    boolean stored() default false;

    Class<? extends Analyzer> indexAnalyzer();

}
