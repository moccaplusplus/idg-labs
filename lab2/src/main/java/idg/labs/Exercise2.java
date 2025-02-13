package idg.labs;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.GraphParseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

public class Exercise2 {
    private static final long DELAY_MILLIS = 50;
    private static final String VISITED_NODE_STYLE = "shape:box;fill-color:black;size:10;";

    public static void main(String... args) throws IOException, GraphParseException {
        traverseWithHighlighting("dgs/completegrid_10.dgs", startNode -> bfs(startNode, node -> {
            node.setAttribute("ui.style", VISITED_NODE_STYLE);
            Tools.sleep(DELAY_MILLIS);
        }));
    }

    public static void traverseWithHighlighting(String dgsFile, Consumer<Node> traversalAlgorithm)
            throws IOException, GraphParseException {
        Graph graph = Tools.read(dgsFile);
        graph.display(true);
        Node startNode = graph.getNode(Tools.RANDOM.nextInt(graph.getNodeCount()));
        traversalAlgorithm.accept(startNode);
    }

    private static void bfs(Node startNode, Consumer<Node> visitor) {
        LinkedList<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        queue.add(startNode);
        visited.add(startNode);
        while (!queue.isEmpty()) {
            Node node = queue.removeFirst();
            visitor.accept(node);
            Iterator<Node> it = node.getNeighborNodeIterator();
            while (it.hasNext()) {
                Node targetNode = it.next();
                if (!visited.contains(targetNode)) {
                    queue.addLast(targetNode);
                    visited.add(targetNode);
                }
            }
        }
    }
}
