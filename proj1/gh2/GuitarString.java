package gh2;

import deque.ArrayDeque;
import deque.Deque;

import java.util.HashSet;
import java.util.Set;

public class GuitarString {
    /** Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday. */
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor
    private static int capacity;

    /* Buffer for storing sound data. */
     private Deque<Double> buffer;

    /* Create a guitar string of the given frequency.  */
    public GuitarString(double frequency) {
        this.capacity = (int)Math.round(SR / frequency);
        buffer = new ArrayDeque<>(this.capacity);
        for (int i = 0; i < this.capacity; i++)
            buffer.addFirst(0.0);
    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        //       Make sure that your random numbers are different from each
        //       other. This does not mean that you need to check that the numbers
        //       are different from each other. It means you should repeatedly call
        //       Math.random() - 0.5 to generate new random numbers for each array index.
        for (int i = 0; i < capacity; i++)
            buffer.removeLast();
        Set<Double> visited = new HashSet<>();
        int idx = 0;
        while (idx < capacity) {
            double r = Math.random() - 0.5;
            while (visited.contains(r))
                r = Math.random() - 0.5;
            buffer.addLast(r);
            visited.add(r);
            idx++;
        }
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        double a = buffer.removeFirst(), b = buffer.get(0);
        double res = (a + b) / 2 * DECAY;
        buffer.addLast(res);
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        return buffer.get(0);
    }
}
