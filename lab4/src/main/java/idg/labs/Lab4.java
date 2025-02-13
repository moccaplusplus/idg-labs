package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.util.ExportUtils;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static idg.labs.Generators.RANDOM;
import static java.lang.String.format;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;

public class Lab4 {
    private static final int[] TOKEN_COUNTS = {10, 20, 50, 100};
    private static final int RUN_COUNT = 20;
    private static final int CHART_WIDTH = 800;
    private static final int CHART_HEIGHT = 600;

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String[] args) throws IOException {
        question1();
        question2();
        question3();
    }

    private static void question1() throws IOException {
        Topology[] topologies = Topology.values();
        List<SingleGraph> graphs = Arrays.stream(topologies).map(Topology::graph).collect(toList());
        int[] diameters = graphs.stream().mapToInt(Lab4::bfsDiameter).toArray();
        List<SortedMap<Integer, Long>> degreeDistributions = graphs.stream()
                .map(Lab4::degreeDistribution).collect(toList());

        withFileAndConsoleWriter("diameter-and-degree-distribution.txt", writer -> {
            writer.println("Diameters And Degree Distribution");
            writer.printf("%-40s%-13s%s%n", "Topology:", "Diameter:", "Degree (Node Count):");
            range(0, topologies.length).forEach(i -> {
                writer.printf("%-40s%-13d", topologies[i], diameters[i]);
                writer.println(degreeDistributions.get(i).entrySet().stream()
                        .map(entry -> format("%d (%d)", entry.getKey(), entry.getValue()))
                        .collect(joining(", ")));
            });
        });

        JFreeChart diameterChart = Charts.barChart(
                "Graph Diameter by Topology", "", "by Topology", "Diameter",
                toDiameterDataset(topologies, diameters));
        saveAndDisplayChart(diameterChart, "diameter.png");
        hitAKey("Hit a key to continue");

        JFreeChart degreeDistributionChart = Charts.scatterPlot(
                "Degree Distribution", "", "Degree", "Node Count",
                toDegreeDistributionDataset(topologies, degreeDistributions));
        saveAndDisplayChart(degreeDistributionChart, "degree-distribution.png");
        hitAKey("Hit a key to continue");
    }

    private static void question2() throws IOException {
        Topology[] topologies = Topology.values();
        int tokenCount = TOKEN_COUNTS[TOKEN_COUNTS.length / 2];
        boolean[] simultaneousOrNot = {false, true};
        for (boolean simultaneous : simultaneousOrNot) {
            for (Topology topology : topologies) {
                SingleGraph graph = topology.graph();
                System.out.printf(
                        "Random Walk Visualization%nTopology: %s, Token Count: %d, %sSimultaneous%n",
                        graph.getId(), tokenCount, simultaneous ? "" : "Non-");
                graph.display(true);
                delay(1000);
                int moveCount = randomWalk(graph, tokenCount, simultaneous, (moveNo, tokens) -> {
                    range(0, tokenCount).forEach(i -> markToken(tokens, i));
                    delay(20);
                });
                System.out.printf("Move count: %d%n", moveCount);
                hitAKey("Hit a key to continue");
            }
        }
    }

    private static void question3() throws IOException {
        Topology[] topologies = Topology.values();
        for (boolean simultaneous : new boolean[]{false, true}) {
            List<double[]> avgCoverTimes = Arrays.stream(topologies)
                    .map(Topology::graph)
                    .map(graph -> Arrays.stream(TOKEN_COUNTS)
                            .mapToDouble(tokenCount -> range(0, RUN_COUNT)
                                    .map(runNo -> randomWalk(graph, tokenCount, simultaneous))
                                    .average()
                                    .orElse(0))
                            .toArray())
                    .collect(toList());

            String title = "Average Cover Time by Topology and Token Count";
            String subTitle = format("Run Count: %d, %sSimultaneous", RUN_COUNT, simultaneous ? "" : "Non-");
            String fileNameBase = format("cover-time%s-simultaneous", simultaneous ? "" : "-non");

            withFileAndConsoleWriter(fileNameBase + ".txt", writer -> {
                writer.println(title);
                writer.println(subTitle);
                writer.printf("%-40s%s%n", "Topology:", "Token Count:");
                writer.printf("%-40s", "");
                writer.println(Arrays.stream(TOKEN_COUNTS).mapToObj(i -> format("%-10d", i)).collect(joining()));
                range(0, topologies.length).forEach(i -> {
                    writer.printf("%-40s", topologies[i]);
                    Arrays.stream(avgCoverTimes.get(i)).forEach(avgTime -> writer.printf("%-10.1f", avgTime));
                    writer.println();
                });
            });
            JFreeChart chart = Charts.lineChart(
                    title, subTitle, "Token Count", "Average Move Count",
                    toAvgCoverTimeDataset(topologies, avgCoverTimes));
            saveAndDisplayChart(chart, fileNameBase + ".png");
            hitAKey("Hit a key to continue");
        }
    }

    private static void markToken(Node[] tokens, int i) {
        double ratio = i / (tokens.length - 1.0);
        String color = format("rgb(%d,0,%d)", (int) (255 * ratio), (int) (255 * (1 - ratio)));
        String style = format("shape:circle;fill-color:%s;size:5px;text-color:%s;", color, color);
        tokens[i].setAttribute("ui.style", style);
        tokens[i].setAttribute("label", "token " + i);
    }

    private static SortedMap<Integer, Long> degreeDistribution(SingleGraph graph) {
        return graph.getNodeSet().stream().collect(groupingBy(Node::getDegree, TreeMap::new, counting()));
    }

    private static Map<Node, Integer> bfsDistances(Node fromNode) {
        Map<Node, Integer> distanceMap = new HashMap<>();
        List<Node> queue = new ArrayList<>();
        List<Node> nextQueue = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        queue.add(fromNode);
        visited.add(fromNode);
        int dist = 0;
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

    private static int eccentricity(Map<Node, Integer> distances) {
        return distances.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    private static Map<Integer, Set<Node>> vertexEccentricities(Graph graph, Function<Node, Map<Node, Integer>> distancesProvider) {
        return graph.getNodeSet().stream().collect(groupingBy(node -> eccentricity(distancesProvider.apply(node)), toSet()));
    }

    private static int bfsDiameter(SingleGraph graph) {
        return vertexEccentricities(graph, Lab4::bfsDistances).keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    private static int randomWalk(Graph graph, int tokenCount, boolean simultaneous) {
        return randomWalk(graph, tokenCount, simultaneous, (moveNo, tokens) -> {
        });
    }

    @SuppressWarnings("ConditionalBreakInInfiniteLoop")
    private static int randomWalk(
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

    private static Node randomNeighbour(Node node) {
        return new ArrayList<Edge>(node.getEdgeSet()).get(RANDOM.nextInt(node.getDegree())).getOpposite(node);
    }

    private static CategoryDataset toDiameterDataset(Topology[] topologies, int[] diameters) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        range(0, topologies.length).forEach(i -> dataset.addValue(diameters[i], topologies[i], "Diameter"));
        return dataset;
    }

    private static XYDataset toDegreeDistributionDataset(
            Topology[] topologies, List<SortedMap<Integer, Long>> degreeDistributions) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        range(0, topologies.length).forEach(i -> {
            XYSeries series = new XYSeries(topologies[i]);
            degreeDistributions.get(i).forEach(series::add);
            dataset.addSeries(series);
        });
        return dataset;
    }

    private static XYDataset toAvgCoverTimeDataset(Topology[] topologies, List<double[]> avgCoverTimes) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        range(0, topologies.length).forEach(i -> {
            XYSeries series = new XYSeries(topologies[i]);
            double[] avgTime = avgCoverTimes.get(i);
            range(0, TOKEN_COUNTS.length).forEach(j -> series.add(TOKEN_COUNTS[j], avgTime[j]));
            dataset.addSeries(series);
        });
        return dataset;
    }

    private static void saveAndDisplayChart(JFreeChart chart, String fileName) throws IOException {
        ExportUtils.writeAsPNG(chart, CHART_WIDTH, CHART_HEIGHT, new File(fileName));
        Charts.displayChart(chart, CHART_WIDTH, CHART_HEIGHT);
    }

    private static void withFileAndConsoleWriter(String fileName, Consumer<PrintWriter> consumer) throws IOException {
        try (
                OutputStream fileStream = Files.newOutputStream(Paths.get(fileName));
                PrintWriter writer = new PrintWriter(multiOutputStream(System.out, fileStream))
        ) {
            consumer.accept(writer);
        }
    }

    private static OutputStream multiOutputStream(OutputStream... outputStreams) {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                for (OutputStream outputStream : outputStreams) {
                    outputStream.write(b);
                }
            }

            @Override
            public void flush() throws IOException {
                for (OutputStream outputStream : outputStreams) {
                    outputStream.flush();
                }
            }
        };
    }

    private static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void hitAKey(String msg) throws IOException {
        System.out.println(msg);
        System.in.read();
    }
}