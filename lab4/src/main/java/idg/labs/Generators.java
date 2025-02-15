package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.stream.IntStream.range;

public class Generators {
    public static final Random RANDOM = new Random();
    private static final String STYLESHEET =
            "graph { fill-color:white;padding:40px; }"
                    + "node { size:7px;shape:circle;fill-color:#222; }"
                    + "edge { size:1px;fill-color:#bbb;}";

    public static SingleGraph grid(String id, int width, int height, boolean moore) {
        SingleGraph graph = empty(id);
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

    public static SingleGraph erdosRenyi(String id, int n, double p) {
        SingleGraph graph = empty(id);
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(0, n - 1).forEach(i -> range(i + 1, n)
                .filter(j -> RANDOM.nextDouble() < p)
                .forEach(j -> addEdge(graph, i, j)));
        return graph;
    }

    public static SingleGraph planar(String id, int n) {
        SingleGraph graph = empty(id);
        addEdge(graph, graph.addNode("0"), graph.addNode("1"));
        range(2, n).forEach(i -> {
            Node w = graph.addNode(valueOf(i));
            Edge e = graph.getEdge(RANDOM.nextInt(graph.getEdgeCount()));
            addEdge(graph, w, e.getNode0());
            addEdge(graph, w, e.getNode1());
        });
        return graph;
    }

    public static SingleGraph fullyConnected(String id, int n) {
        SingleGraph graph = empty(id);
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(0, n).forEach(i -> range(i + 1, n).forEach(j -> addEdge(graph, i, j)));
        return graph;
    }

    public static SingleGraph ring(String id, int n) {
        SingleGraph graph = empty(id);
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(1, n).forEach(i -> addEdge(graph, i - 1, i));
        addEdge(graph, n - 1, 0);
        return graph;
    }

    public static SingleGraph preferentialAttachment(String id, int n, int k) {
        SingleGraph graph = fullyConnected(id, k);
        range(k, n).forEach(i -> {
            LinkedList<Node> candidates = new LinkedList<>(graph.getNodeSet());
            int totalWeight = candidates.stream().mapToInt(Node::getDegree).sum();
            Node u = graph.addNode(valueOf(i));
            for (int j = 0; j < k; j++) {
                int randomValue = RANDOM.nextInt(totalWeight);
                int currentWeight = 0;
                Iterator<Node> it = candidates.iterator();
                while (it.hasNext()) {
                    Node v = it.next();
                    currentWeight += v.getDegree();
                    if (currentWeight > randomValue) {
                        it.remove();
                        totalWeight -= v.getDegree();
                        addEdge(graph, u, v);
                        break;
                    }
                }
            }
        });
        return graph;
    }

    private static SingleGraph empty(String id) {
        SingleGraph graph = new SingleGraph(id);
        graph.addAttribute("ui.stylesheet", STYLESHEET);
        graph.addAttribute("ui.antialias");
        return graph;
    }

    private static Edge addEdge(Graph graph, Node from, Node to) {
        return addEdge(graph, from.getIndex(), to.getIndex());
    }

    private static Edge addEdge(Graph graph, int from, int to) {
        return graph.addEdge(format("%d -> %d", from, to), from, to);
    }
}