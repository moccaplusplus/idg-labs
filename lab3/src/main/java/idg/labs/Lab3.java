package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.stream.IntStream.range;

public class Lab3 {
    public static void main(String[] args) {
        ring(5).display(true);
        sleep();

        ring(10).display(true);
        sleep();

        ring(11).display(true);
        sleep();

        kAryTree(3, 3).display(true);
        sleep();

        kAryTree(2, 5).display(true);
        sleep();

        kAryTree(4, 2).display(true);
        sleep();

        grid(2, 3).display(true);
        sleep();

        grid(8, 12).display(true);
        sleep();

        grid(10, 10).display(true);
        sleep();

        honeycomb(3).display(true);
        sleep();

        honeycomb(8).display(true);
        sleep();

        honeycomb(11).display(true);

    }

    public static SingleGraph ring(final int n) {
        if (n < 3) {
            throw new IllegalArgumentException(format("Expected n to be at least 3, but was %d.", n));
        }
        SingleGraph graph = new SingleGraph(format("Ring of order %d", n));
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(1, n).forEach(i -> addEdge(graph, i - 1, i));
        addEdge(graph, n - 1, 0);
        return graph;
    }

    public static SingleGraph kAryTree(final int k, final int height) {
        if (k < 1) {
            throw new IllegalArgumentException(format("Expected k > 0, but was %d.", k));
        }
        if (height < -1) {
            throw new IllegalArgumentException(format("Expected height >= -1, but was %d.", height));
        }
        SingleGraph graph = new SingleGraph(format("k-ary tree of %d degree", k));
        if (height == -1) {
            return graph;
        }
        int nodeCount = IntStream.rangeClosed(0, height).map(i -> (int) Math.pow(k, i)).sum();
        range(0, nodeCount).forEach(i -> graph.addNode(valueOf(i)));
        range(0, nodeCount).forEach(i -> addEdge(graph, (i - 1) / k, i));
        return graph;
    }

    public static SingleGraph grid(int width, int height) {
        SingleGraph graph = new SingleGraph("grid");
        int n = width * height;
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(1, n).filter(i -> i % width != 0).forEach(i -> addEdge(graph, i - 1, i));
        range(width, n).forEach(i -> addEdge(graph, i - width, i));
        return graph;
    }

    public static SingleGraph honeycomb(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(format("Expected n >= 0, but was %d.", n));
        }
        SingleGraph graph = new SingleGraph(format("Honeycomb grid of size %d", n));
        if (n == 0) {
            return graph;
        }

        int lines = n + 1;
        int nodesInLine = 2 * (n + 1);
        int nodeCount = lines * nodesInLine;

        range(0, nodeCount).forEach(i -> graph.addNode(valueOf(i)));
        range(1, nodeCount)
                .filter(i -> i % nodesInLine != 0)
                .forEach(i -> addEdge(graph, i - 1, i));
        range(nodesInLine, nodeCount)
                .filter(i -> i % 2 == (i / nodesInLine) % 2)
                .forEach(i -> addEdge(graph, i - nodesInLine, i));

        graph.removeNode(nodeCount - (n % 2 == 0 ? 1 : nodesInLine));
        graph.removeNode(0);

        return graph;
    }

    private static Edge addEdge(Graph graph, int from, int to) {
        return graph.addEdge(format("%d -> %d", from, to), from, to);
    }

    private static void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}