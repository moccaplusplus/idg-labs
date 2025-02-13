package idg.labs;

import org.graphstream.graph.Node;
import org.graphstream.stream.GraphParseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import static idg.labs.Exercise2.traverseWithHighlighting;

public class Exercise3 {
    private static final int DELAY_MILLIS = 20;
    private static final String VISITED_NODE_STYLE = "shape:box;fill-color:black;size:7;";

    public static void main(String... args) throws IOException, GraphParseException {
        Consumer<Node> traversalAlgorithm = startNode -> dfs(startNode, node -> {
            node.setAttribute("ui.style", VISITED_NODE_STYLE);
            Tools.sleep(DELAY_MILLIS);
        });
        traverseWithHighlighting("dgs/completegrid_30.dgs", traversalAlgorithm);
        Tools.hitAKey("Hit a key to continue");
        traverseWithHighlighting("dgs/uncompletegrid_50-0.12.dgs", traversalAlgorithm);
    }

    private static void dfs(Node startNode, Consumer<Node> visitor) {
        Stack<Node> stack = new Stack<>();
        Set<Node> visited = new HashSet<>();
        stack.add(startNode);
        visited.add(startNode);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            visitor.accept(node);
            Iterator<Node> it = node.getNeighborNodeIterator();
            while (it.hasNext()) {
                Node targetNode = it.next();
                if (!visited.contains(targetNode)) {
                    stack.add(targetNode);
                    visited.add(targetNode);
                }
            }
        }
    }
}
