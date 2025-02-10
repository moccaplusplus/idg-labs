package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import static java.util.Collections.emptySet;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

public class Exercise6 implements Runnable {
    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String... args) {
        new Exercise6().run();
    }

    public static Set<Edge> primSpanningTree(Graph graph, ToIntFunction<Edge> costFunction, Consumer<Edge> collector) {
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
            collector.accept(edge);
            if (mst.size() == nodeCount - 1) break;
            cutSet.removeIf(e -> e.getNode0().getId().equals(node.getId()) || e.getNode1().getId().equals(node.getId()));
            cutSet.addAll(node.getEdgeSet().stream()
                    .filter(e -> !tree.contains(e.getOpposite(node).getId()))
                    .collect(toList()));
        }
        return mst;
    }

    @Override
    public void run() {
        run("dgs/gridvaluated_10_12.dgs");
        run("dgs/gridvaluated_10_220.dgs");
    }

    private void run(String dgsFile) {
        System.out.printf("Running Prim Min Spanning Tree for graph: %s%n", dgsFile);
        Graph graph = Tools.read(dgsFile);
        graph.display(true);
        primSpanningTree(
                graph,
                edge -> edge.getAttribute("distance"),
                edge -> {
                    edge.setAttribute("ui.style", "fill-color:black;size:5;");
                    Tools.sleep(50);
                });
        Tools.sleep(1000);
    }
}
