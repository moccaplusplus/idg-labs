package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.stream.IntStream.range;

public class Generator {
    public enum Topology {
        grid,
        torus,
        erdosRenyi,
        planar,
        fullyConnected,
        ring,
        preferentialAttachment
    }

    public static final Map<Topology, Supplier<SingleGraph>> GENERATORS;

    public static final Random RANDOM = new Random();
    private static final String STYLESHEET =
            "graph { fill-color: white; padding: 40px; }"
                    + "node { size: 5px; shape: box; fill-color: red; }"
                    + "edge { size : 1px; fill-color: blue;}";

    static {
        EnumMap<Topology, Supplier<SingleGraph>> generators = new EnumMap<>(Generator.Topology.class);
        generators.put(Topology.grid, () -> grid(10, 10));
        generators.put(Topology.torus, () -> torus(10));
        generators.put(Topology.erdosRenyi, () -> erdosRenyi(100, 0.1));
        generators.put(Topology.planar, () -> planar(100));
        generators.put(Topology.fullyConnected, () -> fullyConnected(100));
        generators.put(Topology.ring, () -> ring(100));
        generators.put(Topology.preferentialAttachment, () -> preferentialAttachment(100, 5));
        GENERATORS = Collections.unmodifiableMap(generators);
    }

    public static SingleGraph grid(int width, int height) {
        SingleGraph graph = singleGraph("grid: " + width + ", " + height);
        int n = width * height;
        range(0, n).forEach(i -> graph.addNode(valueOf(i)));
        range(1, n).filter(i -> i % width != 0).forEach(i -> addEdge(graph, i - 1, i));
        range(width, n).forEach(i -> addEdge(graph, i - width, i));
        return graph;
    }

    public static SingleGraph torus(int dim) {
        SingleGraph graph = singleGraph("torus: " + dim + "x" + dim);
        range(0, dim).forEach(row -> range(0, dim).forEach(col -> {
            Node u = graph.addNode(row + "," + col);
            u.addAttribute("x", row);
            u.addAttribute("y", col);
        }));
        range(0, dim).forEach(row -> range(0, dim).forEach(col -> {
            int nextCol = (col + 1) % dim;
            Node u = graph.getNode(row * dim + col);
            Node v = graph.getNode(row * dim + nextCol);
            addEdge(graph, u, v);
            Node w = graph.getNode(((row + 1) % dim) * dim + col);
            addEdge(graph, u, w);
        }));
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

    private static SingleGraph fullyConnected(int n) {
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