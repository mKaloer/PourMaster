package com.kaloer.searchlib.index.pipeline;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by mkaloer on 18/04/15.
 */
public class Pipeline<U, V> {

    private ArrayList<Stage<?, ?>> stages = new ArrayList<Stage<?, ?>>();
    private BlockingQueue<U> sourceQueue = new LinkedBlockingQueue<U>();
    private BlockingQueue<V> destinationQueue = new LinkedBlockingQueue<V>();
    private boolean hasMore = true;

    public Pipeline(Stage<U, V> s) {
        stages.add(s);
        s.setInputQueue(sourceQueue);
        s.setOutputQueue(destinationQueue);
    }

    protected Pipeline() {
        // Empty constructor
    }

    public <T> Pipeline<U, T> append(Stage<V, T> stage) {
        // Create new pipeline from U to T based on current instance.
        Pipeline<U, T> newPipeline = new Pipeline<U, T>();
        for (Stage s : stages) {
            newPipeline.stages.add(s);
        }
        stage.setInputQueue(getDestination().getOutputQueue());
        stage.setOutputQueue(newPipeline.destinationQueue);
        newPipeline.stages.add(stage);
        return newPipeline;
    }

    public ArrayList<V> process(U input) throws InterruptedException {
        sourceQueue.offer(input);
        for (Stage s : stages) {
            s.poll();
        }
        ArrayList<V> output = new ArrayList<V>(destinationQueue.size());
        destinationQueue.drainTo(output);
        return output;
    }

    public Stage<?, V> getDestination() {
        return (Stage<?, V>) stages.get(stages.size() - 1);
    }

    public Stage<U, ?> getSource() {
        return (Stage<U, ?>) stages.get(0);
    }

}
