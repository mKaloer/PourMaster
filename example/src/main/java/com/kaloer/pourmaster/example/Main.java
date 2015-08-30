package com.kaloer.pourmaster.example;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            new PerformanceTest().testIndexing();
        } catch (IOException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
