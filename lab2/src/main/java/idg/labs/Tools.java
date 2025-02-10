package idg.labs;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import java.io.IOException;
import java.util.Random;

public class Tools {
    public static final Random RANDOM = new Random();

    public static SingleGraph read(String file) {
        SingleGraph graph = new SingleGraph("Graph from file: " + file);
        try {
            graph.read(file);
        } catch (IOException | GraphParseException e) {
            throw new IllegalStateException(e);
        }
        return graph;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void hitAKey(String msg) {
        System.out.println(msg);
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
