package idg.labs;

import org.apache.commons.math3.util.Pair;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static idg.labs.Lab3.grid;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparingInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Extra {
    private static final Random RANDOM = new Random();
    private static final String CSS_STYLE = "node { size: 0; stroke-width: 0; } edge { size: 2; fill-color: #555; }" +
            "node.hide { stroke-width: 0; fill-color: white; }" +
            "edge.hide { fill-color: white; z-index: 0; }" +
            "node.mark { size: 7; stroke-width: 0; fill-color: red; }" +
            "edge.mark { stroke-width: 0; fill-color: red; }" +
            "node.path { size: 7; stroke-width: 0; fill-color: #eee; }" +
            "edge.path { fill-color: #eee; }";

    public static void main(String... args) {
        SingleGraph graph = maze(30, 30);
        graph.display(false);

        Stack<Node> route = new Stack<>();
        Node startNode = graph.getNode(graph.getAttribute("maze.entry"));
        Node lastNode = graph.getNode(graph.getAttribute("maze.exit"));
        walker(startNode, node -> {
            delay();
            if (route.isEmpty()) {
                node.addAttribute("ui.class", "mark");
                route.push(node);
            } else if (route.peek() == node) {
                node.addAttribute("ui.class", "path");
                route.pop().getEdgeBetween(route.peek()).addAttribute("ui.class", "path");
            } else {
                node.addAttribute("ui.class", "mark");
                node.getEdgeBetween(route.peek()).addAttribute("ui.class", "mark");
                route.push(node);
            }
            return node == lastNode;
        });
    }

    public static SingleGraph maze(final int width, final int height) {
        if (width < 2 || height < 2) {
            throw new IllegalArgumentException("Arguments width and height should be >= 2");
        }

        SingleGraph graph = grid(width, height);
        graph.addAttribute("ui.stylesheet", CSS_STYLE);

        SingleGraph path = grid(width - 1, height - 1);
        Map<Edge, Integer> costMap = path.getEdgeSet().stream().collect(toMap(identity(), id -> 1 + RANDOM.nextInt(99)));
        Set<Edge> mst = primSpanningTree(path, costMap::get);

        mst.forEach(edge -> {
            int source = edge.getSourceNode().getIndex();
            int target = edge.getTargetNode().getIndex();
            int y = Math.max(source, target) / (width - 1);
            int x = Math.max(source, target) % (width - 1);
            graph.removeEdge(y * width + x, Math.abs(source - target) == 1 ? (y + 1) * width + x : y * width + (x + 1));
        });
        graph.removeEdge(0, 1);
        graph.removeEdge(graph.getNodeCount() - 2, graph.getNodeCount() - 1);

        graph.forEach(n -> n.setAttribute("xy", (double) (n.getIndex() % width), (double) (n.getIndex() / width)));
        path.forEach(n -> graph.addNode(pathId(n))
                .setAttribute("xy", (n.getIndex() % (width - 1)) + 0.5, (n.getIndex() / (width - 1)) + 0.5));
        mst.forEach(e -> graph.addEdge(pathId(e), pathId(e.getSourceNode()), pathId(e.getTargetNode()))
                .setAttribute("ui.class", "hide"));

        graph.setAttribute("maze.entry", pathId(path.getNode(0)));
        graph.setAttribute("maze.exit", pathId(path.getNode(path.getNodeCount() - 1)));

        return graph;
    }

    // from lab2
    public static Set<Edge> primSpanningTree(final Graph graph, final ToIntFunction<Edge> costFunction) {
        int nodeCount = graph.getNodeCount();
        if (nodeCount == 0) {
            return emptySet();
        }
        Set<Edge> mst = new HashSet<>(nodeCount - 1);
        Set<String> tree = new HashSet<>(nodeCount);
        PriorityQueue<Edge> cutSet = new PriorityQueue<>(comparingInt(costFunction));
        Node startNode = graph.iterator().next();
        tree.add(startNode.getId());
        cutSet.addAll(startNode.getEdgeSet());
        while (!cutSet.isEmpty()) {
            final Edge edge = cutSet.poll();
            final Node node = tree.contains(edge.getSourceNode().getId()) ? edge.getTargetNode() : edge.getSourceNode();
            tree.add(node.getId());
            mst.add(edge);
            if (mst.size() == nodeCount - 1) break;
            cutSet.removeIf(e -> e.getNode0().getId().equals(node.getId()) || e.getNode1().getId().equals(node.getId()));
            cutSet.addAll(node.getEdgeSet().stream()
                    .filter(e -> !tree.contains(e.getOpposite(node).getId()))
                    .collect(toList()));
        }
        return mst;
    }

    public static void walker(final Node startNode, final Function<Node, Boolean> visitor) {
        final Stack<Pair<Node, Iterator<Edge>>> stack = new Stack<>();
        final Set<String> visited = new HashSet<>();
        stack.push(Pair.create(startNode, startNode.getEdgeIterator()));
        visited.add(startNode.getId());
        if (visitor.apply(startNode)) return;
        while (!stack.isEmpty()) {
            final Pair<Node, Iterator<Edge>> entry = stack.peek();
            if (entry.getSecond().hasNext()) {
                final Edge edge = entry.getSecond().next();
                final Node node = edge.getOpposite(entry.getFirst());
                if (!visited.contains(node.getId())) {
                    visited.add(node.getId());
                    stack.push(Pair.create(node, node.getEdgeIterator()));
                    if (visitor.apply(node)) return;
                }
            } else {
                stack.pop();
                if (visitor.apply(entry.getFirst())) return;
            }
        }
    }

    private static String pathId(Element element) {
        return "Path:" + element.getId();
    }

    private static void delay() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
