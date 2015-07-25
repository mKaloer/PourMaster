package com.kaloer.pourmaster.annotations;

import com.kaloer.pourmaster.Analyzer;
import com.kaloer.pourmaster.Token;
import com.kaloer.pourmaster.fields.FieldType;
import org.apache.commons.collections4.IteratorUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field {

    Class<? extends FieldType> type();

    boolean indexed() default true;

    boolean stored() default false;

    Class<? extends Analyzer> indexAnalyzer() default EmptyAnalyzer.class;

    /**
     * An analyzer which always returns an empty iterator.
     */
    class EmptyAnalyzer extends Analyzer {

        @Override
        public Iterator<Token> analyze(Object value) {
            return IteratorUtils.emptyIterator();
        }
    }

}
