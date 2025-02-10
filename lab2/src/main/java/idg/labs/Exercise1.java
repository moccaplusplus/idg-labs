package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import static java.lang.String.format;

public class Exercise1 implements Runnable {
    private static final String EXCEEDED_COST_STYLE = "fill-color:red;size:30;";

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String... args) {
        new Exercise1(30).run();
    }

    private final int referenceCost;

    public Exercise1(int referenceCost) {
        this.referenceCost = referenceCost;
    }

    @Override
    public void run() {
        Graph graph = Tools.read("dgs/firstgraphlab2.dgs");
        for (Node node : graph) {
            int neighbourCostSum = getNeighbourCostSum(node);
            node.setAttribute("ui.label", format("Neighbour Cost Sum: %d", neighbourCostSum));
            if (neighbourCostSum > referenceCost) {
                node.setAttribute("ui.style", EXCEEDED_COST_STYLE);
            }
        }
        System.out.printf("Average graph degree: %f%n", averageDegree(graph));
        graph.display(true);
    }

    private static double averageDegree(Graph graph) {
        int sumDegree = 0;
        for (Node n : graph) {
            sumDegree += n.getDegree();
        }
        return (double) sumDegree / graph.getNodeCount();
    }

    private static int getNeighbourCostSum(Node node) {
        int totalCost = 0;
        for (Edge edge : node.getEdgeSet()) {
            totalCost += edge.getAttribute("cost", Integer.class);
        }
        node.setAttribute("totalCost", totalCost);
        return totalCost;
    }
}
