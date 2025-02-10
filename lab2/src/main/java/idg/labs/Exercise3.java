package idg.labs;

import org.graphstream.graph.Node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

public class Exercise3 extends Exercise2 {
    private static final String VISITED_NODE_STYLE = "shape:box;fill-color:black;size:7;";

    public static void main(String... args) {
        new Exercise3(20).run();
    }

    public static void dfs(Node startNode, Consumer<Node> visitor) {
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

    public Exercise3(long delayMillis) {
        super(delayMillis);
    }

    @Override
    public void run() {
        traverseWithHighlighting("dgs/completegrid_30.dgs", Exercise3::dfs);
        traverseWithHighlighting("dgs/uncompletegrid_50-0.12.dgs", Exercise3::dfs);
    }

    @Override
    protected String getVisitedNodeStyle() {
        return VISITED_NODE_STYLE;
    }
}
