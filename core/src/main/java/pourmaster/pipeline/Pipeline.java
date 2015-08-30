package pourmaster.pipeline;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by mkaloer on 18/04/15.
 */
public class Pipeline<U, V> {

    private final ArrayList<Stage<?, ?>> stages = new ArrayList<Stage<?, ?>>();
    private final BlockingQueue<U> sourceQueue = new LinkedBlockingQueue<U>();
    private final BlockingQueue<V> destinationQueue = new LinkedBlockingQueue<V>();

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
