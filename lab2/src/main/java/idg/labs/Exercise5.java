package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

import static java.lang.String.format;

public class Exercise5 implements Runnable {
    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String... args) {
        new Exercise5().run();
    }

    public static Map<Node, Integer> vertexEccentricities(Graph graph, ToIntFunction<Edge> costFunction) {
        Map<Node, Integer> vertexEccentricities = new HashMap<>();
        for (Node node : graph.getNodeSet()) {
            Map<Node, Integer> cost = Exercise4.dijkstra(node, costFunction);
            int eccentricity = 0;
            for (int c : cost.values()) {
                eccentricity += c;
            }
            vertexEccentricities.put(node, eccentricity);
        }
        return vertexEccentricities;
    }

    @Override
    public void run() {
        Graph graph = Tools.read("dgs/uncompletegrid_50-0.12.dgs");

        Map<Node, Integer> eccentricitiesByNode = vertexEccentricities(graph, edge -> 1);
        int diameter = eccentricitiesByNode.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int radius = eccentricitiesByNode.values().stream().mapToInt(Integer::intValue).min().orElse(0);

        System.out.println("Eccentricities By Node:");
        eccentricitiesByNode.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .forEach(entry -> {
                    Node node = entry.getKey();
                    int eccentricity = entry.getValue();
                    System.out.printf("Node %s: %d%n", node.getId(), eccentricity);
                    double ratio = (eccentricity - radius) / (double) (diameter - radius);
                    int red = (int) (255 * ratio);
                    int blue = (int) (255 * (1 - ratio));
                    String style = format("shape:box;fill-color:rgb(%d,0,%d);size:7;", red, blue);
                    node.setAttribute("ui.style", style);
                });
        System.out.println();
        System.out.printf("Diameter: %d, Radius %d%n", diameter, radius);

        graph.display(true);
    }
}
