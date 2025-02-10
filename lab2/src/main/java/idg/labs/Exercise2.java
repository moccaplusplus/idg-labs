package idg.labs;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Exercise2 implements Runnable {
    private static final String VISITED_NODE_STYLE = "shape:box;fill-color:black;size:10;";

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String... args) {
        new Exercise2(50).run();
    }

    public static void bfs(Node startNode, Consumer<Node> visitor) {
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

    private final long delayMillis;

    public Exercise2(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    @Override
    public void run() {
        traverseWithHighlighting("dgs/completegrid_10.dgs", Exercise2::bfs);
    }

    protected void traverseWithHighlighting(String dgsFile, BiConsumer<Node, Consumer<Node>> traversalAlgorithm) {
        Graph graph = Tools.read(dgsFile);
        graph.display();
        Node startNode = graph.getNode(Tools.RANDOM.nextInt(graph.getNodeCount()));
        traversalAlgorithm.accept(startNode, node -> {
            node.setAttribute("ui.style", getVisitedNodeStyle());
            Tools.sleep(delayMillis);
        });
    }

    protected String getVisitedNodeStyle() {
        return VISITED_NODE_STYLE;
    }
}
