package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.ToIntFunction;

public class Exercise4 implements Runnable {
    private static final String START_NODE_STYLE =
            "shape: box;fill-color: black;size: 10px, 10px;stroke-mode: plain;stroke-color:black;";

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String... args) {
        new Exercise4().run();
    }

    public static Map<Node, Integer> dijkstra(Node startNode, ToIntFunction<Edge> costFunction) {
        Map<Node, Integer> cost = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(cost::get));
        cost.put(startNode, 0);
        priorityQueue.add(startNode);
        while (!priorityQueue.isEmpty()) {
            Node prev = priorityQueue.poll();
            int prevCost = cost.get(prev);
            Iterator<Node> it = prev.getNeighborNodeIterator();
            while (it.hasNext()) {
                Node next = it.next();
                Edge edge = next.getEdgeBetween(prev);
                int edgeCost = costFunction.applyAsInt(edge);
                int newCost = prevCost + edgeCost;
                Integer oldCost = cost.get(next);
                if (oldCost == null || newCost < oldCost) {
                    cost.put(next, newCost);
                    priorityQueue.remove(next);
                    priorityQueue.add(next);
                }
            }
        }
        return cost;
    }

    @Override
    public void run() {
        run("dgs/gridvaluated_10_12.dgs");
        run("dgs/gridvaluated_10_220.dgs");
    }

    private void run(String dgsFile) {
        System.out.printf("Running Dijkstra for graph: %s%n", dgsFile);
        Graph graph = Tools.read(dgsFile);
        graph.display(true);

        Node startNode = graph.getNode(Tools.RANDOM.nextInt(graph.getNodeCount()));
        appendToLabel(startNode, ", START");
        startNode.setAttribute("ui.style", START_NODE_STYLE);

        Map<Node, Integer> costMap = dijkstra(startNode, edge -> edge.getAttribute("distance"));
        System.out.printf("Distances from Start Node %s to:%n", startNode.getId());
        costMap.entrySet().stream()
                .filter(entry -> !Objects.equals(startNode.getId(), entry.getKey().getId()))
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .forEach(entry -> {
                    Node node = entry.getKey();
                    int distance = entry.getValue();
                    appendToLabel(node, ", DISTANCE: " + distance);
                    System.out.printf("Node %s: %d%n", node.getId(), distance);
                });
    }

    private static void appendToLabel(Element element, String textToAppend) {
        element.setAttribute("ui.label", element.getAttribute("ui.label", String.class) + textToAppend);
    }
}
