package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.stream.IntStream.range;

public class Generators {
    public static SingleGraph grid(String id, int width, int height, boolean moore) {
        SingleGraph graph = new SingleGraph(id);
        int n = width * height;
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(0, n).filter(i -> i % width != 0).forEach(i -> addEdge(graph, i - 1, i));
        range(width, n).forEach(i -> addEdge(graph, i - width, i));
        if (moore) {
            range(width, n).filter(i -> i % width != 0).forEach(i -> addEdge(graph, i - width - 1, i));
            range(width, n).filter(i -> i % width != 0).forEach(i -> addEdge(graph, i - width, i - 1));
        }
        return graph;
    }

    public static SingleGraph torus(String id, int width, int height, boolean moore) {
        SingleGraph graph = grid(id, width, height, moore);
        int n = graph.getNodeCount();
        range(0, width).forEach(i -> addEdge(graph, i, i + (n - width)));
        range(0, height).forEach(i -> addEdge(graph, i * width, ((i + 1) * width) - 1));
        if (moore) {
            range(0, width).forEach(i -> addEdge(graph, (i + 1) % width, i + n - width));
            range(0, width).forEach(i -> addEdge(graph, i, ((i + 1) % width) + n - width));
            range(1, height).forEach(i -> addEdge(graph, i * width, (i * width) - 1));
            range(0, height - 1).forEach(i -> addEdge(graph, i * width, ((i + 2) * width) - 1));
        }
        return graph;
    }

    private static Edge addEdge(Graph graph, int from, int to) {
        return graph.addEdge(format("%d -> %d", from, to), from, to);
    }
}
