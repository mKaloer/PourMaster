package com.kaloer.searchlib.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public abstract class FieldDataStore {

    public abstract List<Field> getFields(long index) throws IOException;

}
