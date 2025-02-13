package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class Exercise5 {
    public static void main(String... args) throws IOException, GraphParseException {
        SingleGraph graph = Tools.read("dgs/uncompletegrid_50-0.12.dgs");
        graph.display(true);

        Map<Integer, Set<Node>> eccentricitiesByNode = bfsVertexEccentricities(graph);
        int diameter = eccentricitiesByNode.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        int radius = eccentricitiesByNode.keySet().stream().mapToInt(Integer::intValue).min().orElse(0);

        System.out.printf("%-15s%-15s%n", "Eccentricity", "Nodes");
        eccentricitiesByNode.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .forEach(entry -> {
                    int eccentricity = entry.getKey();
                    Set<Node> nodes = entry.getValue();
                    System.out.printf("%-15d%-15s%n", eccentricity, nodes.stream().map(Node::getId).collect(joining(", ")));

                    double ratio = (eccentricity - radius) / (double) (diameter - radius);
                    int red = (int) (255 * ratio);
                    int blue = (int) (255 * (1 - ratio));
                    String style = format("shape:box;fill-color:rgb(%d,0,%d);size:10;", red, blue);
                    nodes.forEach(node -> node.setAttribute("ui.style", style));
                });
        System.out.println();
        System.out.printf("Diameter: %d, Radius %d%n", diameter, radius);
    }

    private static Map<Node, Integer> bfsDistances(Node startNode) {
        Map<Node, Integer> distanceMap = new HashMap<>();
        List<Node> queue = new ArrayList<>();
        List<Node> nextQueue = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        queue.add(startNode);
        visited.add(startNode);
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
        return graph.getNodeSet().stream().parallel()
                .collect(groupingBy(node -> eccentricity(distancesProvider.apply(node)), toSet()));
    }

    private static Map<Integer, Set<Node>> bfsVertexEccentricities(Graph graph) {
        return vertexEccentricities(graph, Exercise5::bfsDistances);
    }

    private static Map<Integer, Set<Node>> dijkstraVertexEccentricities(Graph graph, ToIntFunction<Edge> costFunction) {
        return vertexEccentricities(graph, node -> Exercise4.dijkstra(node, costFunction));
    }
}
