package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.stream.IntStream.range;

public class Generators {
    public static final Random RANDOM = new Random();
    private static final String STYLESHEET =
            "graph { fill-color:white;padding:40px; }"
                    + "node { size:7px;shape:circle;fill-color:#222; }"
                    + "edge { size:1px;fill-color:#bbb;}";

    public static SingleGraph grid(int width, int height, boolean moore) {
        SingleGraph graph = singleGraph("grid: " + width + ", " + height);
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

    public static SingleGraph torus(int width, int height, boolean moore) {
        SingleGraph graph = singleGraph("torus: " + width + ", " + height);
        int n = width * height;
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(0, n).filter(i -> i % width != 0).forEach(i -> addEdge(graph, i - 1, i));
        range(width, n).forEach(i -> addEdge(graph, i - width, i));
        range(0, width).forEach(i -> addEdge(graph, i, i + (n - width)));
        range(0, height).forEach(i -> addEdge(graph, i * width, ((i + 1) * width) - 1));
        if (moore) {
            range(width, n).filter(i -> i % width != 0).forEach(i -> addEdge(graph, i - width - 1, i));
            range(width, n).filter(i -> i % width != 0).forEach(i -> addEdge(graph, i - width, i - 1));
            range(0, width).forEach(i -> addEdge(graph, (i + 1) % width, i + n - width));
            range(0, width).forEach(i -> addEdge(graph, i, ((i + 1) % width) + n - width));
            range(1, height).forEach(i -> addEdge(graph, i * width, (i * width) - 1));
            range(0, height - 1).forEach(i -> addEdge(graph, i * width, ((i + 2) * width) - 1));
        }
        return graph;
    }

    public static SingleGraph erdosRenyi(int n, double p) {
        SingleGraph graph = singleGraph("erdos-renyi: " + n + ", " + p);
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(0, n - 1).forEach(i -> IntStream.range(i + 1, n)
                .filter(j -> RANDOM.nextDouble() < p)
                .forEach(j -> addEdge(graph, i, j)));
        return graph;
    }

    public static SingleGraph planar(int n) {
        SingleGraph graph = singleGraph("planar: " + n);
        Node u = graph.addNode("0");
        Node v = graph.addNode("1");
        addEdge(graph, u, v);
        for (int i = 2; i < n; i++) {
            Node w = graph.addNode(String.valueOf(i));
            Edge e = graph.getEdge(RANDOM.nextInt(graph.getEdgeCount()));
            u = e.getNode0();
            v = e.getNode1();
            addEdge(graph, w, u);
            addEdge(graph, w, v);
        }
        return graph;
    }

    public static SingleGraph fullyConnected(int n) {
        SingleGraph graph = singleGraph("fullyConnected: " + n);
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(0, n).forEach(i -> range(i + 1, n).forEach(j -> addEdge(graph, i, j)));
        return graph;
    }

    public static SingleGraph ring(final int n) {
        SingleGraph graph = singleGraph("ring: " + n);
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(1, n).forEach(i -> addEdge(graph, i - 1, i));
        addEdge(graph, n - 1, 0);
        return graph;
    }

    public static SingleGraph preferentialAttachment(int n, int k) {
        SingleGraph graph = fullyConnected(k);
        range(graph.getNodeCount(), n).forEach(i -> {
            List<Node> candidates = new ArrayList<>(graph.getNodeSet());
            Node u = graph.addNode(String.valueOf(i));
            IntStream.range(0, k).forEach(j -> {
                Node v = weightedRandomSampling(candidates);
                addEdge(graph, u, v);
                candidates.remove(v);
            });
        });
        return graph;
    }

    private static Node weightedRandomSampling(List<Node> candidates) {
        int totalWeight = candidates.stream().mapToInt(Node::getDegree).sum();
        int val = RANDOM.nextInt(totalWeight);
        int currentWeight = 0;
        for (Node n : candidates) {
            currentWeight += n.getDegree();
            if (currentWeight > val) {
                return n;
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    private static SingleGraph singleGraph(String name) {
        SingleGraph graph = new SingleGraph(name);
        graph.addAttribute("ui.stylesheet", STYLESHEET);
        graph.addAttribute("ui.antialias");
        return graph;
    }

    private static Edge addEdge(Graph graph, Node from, Node to) {
        return graph.addEdge(format("%s -> %s", from.getId(), to.getId()), from, to);
    }

    private static Edge addEdge(Graph graph, int from, int to) {
        return graph.addEdge(format("%d -> %d", from, to), from, to);
    }
}