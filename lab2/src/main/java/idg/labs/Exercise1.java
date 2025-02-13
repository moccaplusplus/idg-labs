package idg.labs;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import java.io.IOException;

import static java.lang.String.format;

public class Exercise1 {
    private static final int REFERENCE_COST = 30;
    private static final String EXCEEDED_COST_STYLE = "fill-color:red;size:30;";

    public static void main(String... args) throws IOException, GraphParseException {
        SingleGraph graph = Tools.read("dgs/firstgraphlab2.dgs");
        for (Node node : graph) {
            int neighbourCostSum = getNeighbourCostSum(node);
            node.setAttribute("ui.label", format("Neighbour Cost Sum: %d", neighbourCostSum));
            System.out.printf("Node id: %s - Neighbour Cost Sum=%d%n", node.getId(), neighbourCostSum);
            if (neighbourCostSum > REFERENCE_COST) {
                node.setAttribute("ui.style", EXCEEDED_COST_STYLE);
            }
        }
        System.out.printf("Average graph degree: %f%n", averageDegree(graph));
        graph.display(true);
    }

    private static double averageDegree(Graph graph) {
        return graph.getNodeSet().stream().mapToInt(Node::getDegree).average().orElse(0);
    }

    private static int getNeighbourCostSum(Node node) {
        return node.getEdgeSet().stream().mapToInt(edge -> edge.getAttribute("cost", Integer.class)).sum();
    }
}
