package idg.labs;

import idg.labs.Generator.Topology;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static idg.labs.Generator.GENERATORS;
import static idg.labs.Generator.RANDOM;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

public class Lab4 implements Runnable {
    private static final int[] TOKEN_COUNTS = {10, 20, 50, 100};

    public static void main(String[] args) {
        new Lab4().run();
    }

    @Override
    public void run() {
        Map<Topology, SingleGraph> graphsByTopology = GENERATORS.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().get()));

        question1(graphsByTopology);

        question2(graphsByTopology);
    }

    private void question1(Map<Topology, SingleGraph> graphsByTopology) {
        graphsByTopology.forEach((topology, graph) -> {
            int diameter = bfsDiameter(graph);
            SortedMap<Integer, Long> degreeDistribution = degreeDistribution(graph);
            System.out.printf("Topology: %s%nDiameter: %d%n", topology, diameter);
            System.out.printf("Degree Distribution: %n%15s%15s%n", "Degree", "Node Count");
            degreeDistribution.forEach((degree, nodeCount) -> System.out.printf("%15d%15d%n", degree, nodeCount));
            System.out.println();
        });

    }

    private void question2(Map<Topology, SingleGraph> graphsByTopology) {
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

    private static SortedMap<Integer, Long> degreeDistribution(SingleGraph graph) {
        return graph.getNodeSet().stream().collect(groupingBy(
                Node::getDegree, () -> new TreeMap<>(Comparator.naturalOrder()), counting()));
    }

    public static Map<Node, Integer> bfsDist(Node startNode) {
        Map<Node, Integer> distanceMap = new HashMap<>();
        List<Node> queue = new ArrayList<>();
        List<Node> nextQueue = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        queue.add(startNode);
        visited.add(startNode);
        int dist = 0;
        distanceMap.put(startNode, dist);
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

    public static Map<Node, Integer> bfsVertexEccentricities(Graph graph) {
        Map<Node, Integer> vertexEccentricities = new HashMap<>();
        for (Node node : graph.getNodeSet()) {
            Map<Node, Integer> cost = bfsDist(node);
            int eccentricity = 0;
            for (int c : cost.values()) {
                eccentricity += c;
            }
            vertexEccentricities.put(node, eccentricity);
        }
        return vertexEccentricities;
    }

    public static int bfsDiameter(SingleGraph graph) {
        return bfsVertexEccentricities(graph).values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public static int randomWalk(Graph graph, int tokenCount, boolean simultaneous) {
        Node[] tokens = range(0, tokenCount)
                .mapToObj(i -> (Node) graph.getNode(RANDOM.nextInt(graph.getNodeCount())))
                .toArray(Node[]::new);
        int moveCount = 0;
        Set<String> visitedNodes = new HashSet<>();
        if (simultaneous) {
            while (visitedNodes.size() < graph.getNodeCount()) {
                for (int i = 0; i < tokenCount; i++) {
                    visitedNodes.add((tokens[i] = randomNeighbour(tokens[i])).getId());
                }
                moveCount++;
            }
        } else {
            while (visitedNodes.size() < graph.getNodeCount()) {
                int i = moveCount % tokenCount;
                visitedNodes.add((tokens[i] = randomNeighbour(tokens[i])).getId());
                moveCount++;
            }
        }
        return moveCount;
    }

    public static Node randomNeighbour(Node node) {
        return new ArrayList<Edge>(node.getEdgeSet()).get(RANDOM.nextInt(node.getDegree())).getOpposite(node);
    }

    private static void delay() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}