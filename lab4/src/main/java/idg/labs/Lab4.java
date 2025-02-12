package idg.labs;

import org.apache.commons.math3.util.Pair;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
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

import static idg.labs.Generators.RANDOM;
import static java.lang.String.format;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;

public class Lab4 {
    private static final int[] TOKEN_COUNTS = {10, 20, 50, 100};
    private static final int RUN_COUNT = 20;

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String[] args) {
        question1();

//        question2();

        question3();
    }

    private static void question1() {
        Topology.forEach((topology, graph) -> {
            int diameter = bfsDiameter(graph);
            SortedMap<Integer, Long> degreeDistribution = degreeDistribution(graph);
            System.out.printf("Topology: %s%nDiameter: %d%n", topology, diameter);
            System.out.printf("Degree Distribution: %n%-15s%-15s%n", "Degree", "Node Count");
            degreeDistribution.forEach(
                    (degree, nodeCount) -> System.out.printf("%-15d%-15d%n", degree, nodeCount));
            System.out.println();
        });
        hitAKey("Hit a key to continue");
    }

    private static void question2() {
        int tokenCount = TOKEN_COUNTS[TOKEN_COUNTS.length / 2];
        range(0, 2).mapToObj(i -> i == 1).forEach(simultaneous -> Topology.forEach((topology, graph) -> {
            System.out.printf(
                    "Random walk visualization%nTopology: %s, tokenCount: %d, simultaneous: %s%n",
                    topology, tokenCount, simultaneous);
            graph.display(true);
            delay(1000);
            int moveCount = randomWalk(graph, tokenCount, simultaneous, (moveNo, tokens) -> {
                range(0, tokenCount).forEach(i -> markToken(tokens, i));
                delay(20);
            });
            System.out.printf("Move count: %d%n", moveCount);
            hitAKey("Hit a key to continue");
        }));
    }

    private static void question3() {
        List<Pair<Topology, List<Pair<Integer, Integer>>>> dataset = Topology.stream()
                .map(topology -> {
                    SingleGraph graph = topology.graph.get();
                    return Pair.create(
                            topology,
                            Arrays.stream(TOKEN_COUNTS).boxed().map(tokenCount -> Pair.create(
                                            tokenCount, (int) range(0, RUN_COUNT).parallel()
                                                    .flatMap(runNo -> range(0, 2)
                                                            .map(i -> randomWalk(graph, tokenCount, i == 1)))
                                                    .average().orElse(0)))
                                    .collect(toList()));
                })
                .collect(toList());

        Chart.showAndSave(dataset);
        printResults(dataset);
    }

    private static void markToken(Node[] tokens, int i) {
        double ratio = i / (tokens.length - 1.0);
        String color = format("rgb(%d,0,%d)", (int) (255 * ratio), (int) (255 * (1 - ratio)));
        String style = format("shape:circle;fill-color:%s;size:5px;text-color:%s;", color, color);
        tokens[i].setAttribute("ui.style", style);
        tokens[i].setAttribute("label", "token " + i);
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

    @SuppressWarnings("ConditionalBreakInInfiniteLoop")
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
            while (true) {
                for (int i = 0; i < tokenCount; i++) {
                    tokens[i] = randomNeighbour(tokens[i]);
                    visitedNodes.add(tokens[i].getId());
                }
                moveCount++;
                onMove.accept(moveCount, tokens);
                if (visitedNodes.size() == graph.getNodeCount()) {
                    break;
                }
            }
        } else {
            while (true) {
                int i = moveCount % tokenCount;
                tokens[i] = randomNeighbour(tokens[i]);
                visitedNodes.add(tokens[i].getId());
                moveCount++;
                onMove.accept(moveCount, tokens);
                if (visitedNodes.size() == graph.getNodeCount()) {
                    break;
                }
            }
        }
        return moveCount;
    }

    public static Node randomNeighbour(Node node) {
        return new ArrayList<Edge>(node.getEdgeSet()).get(RANDOM.nextInt(node.getDegree())).getOpposite(node);
    }

    private static void printResults(List<Pair<Topology, List<Pair<Integer, Integer>>>> dataset) {
        try (PrintWriter writer = new PrintWriter(new OutputStream() {
            private final OutputStream fileStream = Files.newOutputStream(
                    Paths.get("avgMovesByTopologyAndTokenCount.txt"));

            @Override
            public void write(int b) throws IOException {
                System.out.write(b);
                fileStream.write(b);
            }

            @Override
            public void close() throws IOException {
                fileStream.close();
            }
        })) {
            writer.println("Average Cover Time by Topology than by Token Count");
            writer.printf("%-25s%s%n", "Topology", "Tokens");
            writer.printf("%-25s", "");
            Arrays.stream(TOKEN_COUNTS).forEach(i -> writer.printf("%-15d", i));
            writer.println();
            dataset.forEach(topologyAndResults -> {
                writer.printf("%-25s", topologyAndResults.getFirst());
                topologyAndResults.getSecond().forEach(tokenCountAndAvgMoves -> writer.printf(
                        "%-15d", tokenCountAndAvgMoves.getSecond()));
                writer.println();
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void delay(long millis) {
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