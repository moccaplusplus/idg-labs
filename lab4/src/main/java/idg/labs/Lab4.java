package idg.labs;

import idg.labs.Generator.Topology;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static idg.labs.Generator.GENERATORS;
import static idg.labs.Generator.RANDOM;
import static java.lang.String.format;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;

public class Lab4 implements Runnable {
    private static final int[] TOKEN_COUNTS = {10, 20, 50, 100};

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String[] args) {
        new Lab4().run();
    }

    @Override
    public void run() {
        Map<Topology, SingleGraph> graphsByTopology = GENERATORS.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().get()));

        question1(graphsByTopology);
        delay(3000);

        question2(graphsByTopology);
    }

    private void question1(Map<Topology, SingleGraph> graphsByTopology) {
        graphsByTopology.forEach((topology, graph) -> {
            int diameter = bfsDiameter(graph);
            SortedMap<Integer, Long> degreeDistribution = degreeDistribution(graph);
            System.out.printf("Topology: %s%nDiameter: %d%n", topology, diameter);
            System.out.printf("Degree Distribution: %n%-15s%-15s%n", "Degree", "Node Count");
            degreeDistribution.forEach(
                    (degree, nodeCount) -> System.out.printf("%-15d%-15d%n", degree, nodeCount));
            System.out.println();
        });

    }

    private void question2(Map<Topology, SingleGraph> graphsByTopology) {
        int tokenCount = TOKEN_COUNTS[TOKEN_COUNTS.length / 2];
        graphsByTopology.forEach((topology, graph) -> {
            graph.display();
            delay(1000);
            randomWalk(graph, tokenCount, true, (moveNo, tokens) -> {
                range(0, tokenCount).forEach(i -> markToken(tokens, i));
                delay(30);
            });
            delay(2000);
            clearTokenMarks(graph);
        });

        graphsByTopology.forEach((topology, graph) -> {
            graph.display();
            delay(1000);
            randomWalk(graph, tokenCount, false, (moveNo, tokens) -> {
                if (moveNo == 0) {
                    range(0, tokenCount).forEach(i -> markToken(tokens, i));
                } else {
                    markToken(tokens, (moveNo - 1) % tokenCount);
                }
                delay(30);
            });
            delay(2000);
            clearTokenMarks(graph);
        });
    }

    private void question3(Map<Topology, SingleGraph> graphsByTopology) {
        int runCount = 20;
        for (int tokenCount : TOKEN_COUNTS) {
            graphsByTopology.forEach((topology, graph) -> {
                for (int i = 0; i < runCount; i++) {
                    randomWalk(graph, tokenCount, false);
                    randomWalk(graph, tokenCount, true);
                }
            });
        }
    }

    private static void markToken(Node[] tokens, int i) {
        double ratio = i / (tokens.length - 1.0);
        String color = format("rgb(%d,0,%d)", (int) (255 * ratio), (int) (255 * (1 - ratio)));
        String style = format("shape:box;fill-color:%s;size:5px;text-color:%s;", color, color);
        tokens[i].setAttribute("ui.style", style);
        tokens[i].setAttribute("label", "token " + i);
    }

    private void clearTokenMarks(SingleGraph graph) {
        graph.getNodeSet().forEach(node -> {
            node.setAttribute("ui.style", "size:5px;shape:box;fill-color:#333;");
            node.removeAttribute("label");
        });
    }

    private static SortedMap<Integer, Long> degreeDistribution(SingleGraph graph) {
        return graph.getNodeSet().stream().collect(groupingBy(
                Node::getDegree, () -> new TreeMap<>(Comparator.naturalOrder()), counting()));
    }

    public static Map<Node, Integer> bfsDistances(Node fromNode) {
        Map<Node, Integer> distanceMap = new HashMap<>();
        List<Node> queue = new ArrayList<>();
        List<Node> nextQueue = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        queue.add(fromNode);
        visited.add(fromNode);
        int dist = 0;
        distanceMap.put(fromNode, dist);
        while (!queue.isEmpty()) {
            dist++;
            for (Node node : queue) {
                Iterator<Node> it = node.getNeighborNodeIterator();
                while (it.hasNext()) {
                    Node targetNode = it.next();
                    if (!visited.contains(targetNode)) {
                        nextQueue.add(targetNode);
                        visited.add(targetNode);
                        distanceMap.put(targetNode, dist);
                    }
                }
            }
            queue.clear();
            queue.addAll(nextQueue);
            nextQueue.clear();
        }
        return distanceMap;
    }

    public static int eccentricity(Map<Node, Integer> distances) {
        return distances.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public static Map<Integer, Set<Node>> vertexEccentricities(Graph graph, Function<Node, Map<Node, Integer>> distancesProvider) {
        return graph.getNodeSet().stream().collect(groupingBy(node -> eccentricity(distancesProvider.apply(node)), toSet()));
    }

    public static int bfsDiameter(SingleGraph graph) {
        return vertexEccentricities(graph, Lab4::bfsDistances).keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public static int randomWalk(Graph graph, int tokenCount, boolean simultaneous) {
        return randomWalk(graph, tokenCount, simultaneous, (moveNo, tokens) -> {
        });
    }

    public static int randomWalk(
            Graph graph, int tokenCount, boolean simultaneous, BiConsumer<Integer, Node[]> onMove) {
        int moveCount = 0;
        Set<String> visitedNodes = new HashSet<>();
        Node[] tokens = range(0, tokenCount)
                .mapToObj(i -> (Node) graph.getNode(RANDOM.nextInt(graph.getNodeCount())))
                .toArray(Node[]::new);

        Arrays.stream(tokens).map(Node::getId).forEach(visitedNodes::add);
        onMove.accept(moveCount, tokens);

        if (simultaneous) {
            while (visitedNodes.size() < graph.getNodeCount()) {
                for (int i = 0; i < tokenCount; i++) {
                    tokens[i] = randomNeighbour(tokens[i]);
                    visitedNodes.add(tokens[i].getId());
                }
                moveCount++;
                onMove.accept(moveCount, tokens);
            }
        } else {
            while (visitedNodes.size() < graph.getNodeCount()) {
                int i = moveCount % tokenCount;
                tokens[i] = randomNeighbour(tokens[i]);
                visitedNodes.add(tokens[i].getId());
                moveCount++;
                onMove.accept(moveCount, tokens);
            }
        }
        return moveCount;
    }

    public static Node randomNeighbour(Node node) {
        return new ArrayList<Edge>(node.getEdgeSet()).get(RANDOM.nextInt(node.getDegree())).getOpposite(node);
    }

    private static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}