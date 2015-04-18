package com.kaloer.searchlib.index.pipeline;

import com.kaloer.searchlib.index.Token;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by mkaloer on 15/04/15.
 */
public abstract class Stage<U, V> {

    private BlockingQueue<U> source;
    private BlockingQueue<V> destination;

    protected Stage() {

    }

    protected void setInputQueue(BlockingQueue<U> source) {
        this.source = source;
    }

    protected void setOutputQueue(BlockingQueue<V> destination) {
        this.destination = destination;
    }

    protected void emit(V value) throws InterruptedException {
        destination.offer(value);
    }

    public void poll() throws InterruptedException {
        U item;
        while((item = source.poll()) != null) {
            produce(item);
        }
    }

    public boolean hasNext() {
        return getOutputQueue().size() > 0;
    }

    public V getNext() {
        return getOutputQueue().poll();
    }

    protected abstract void produce(U input) throws InterruptedException;

    protected BlockingQueue<V> getOutputQueue() {
        return destination;
    }
}
