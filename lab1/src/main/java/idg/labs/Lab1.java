package idg.labs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;

import java.io.IOException;
import java.util.Random;

public class Lab1 {
    private static final Random RANDOM = new Random();

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String[] args) {
        exercise1();
        hitAKey("Hit a key to continue");
        exercise2();
        hitAKey("Hit a key to continue");
        exercise3();
        hitAKey("Hit a key to continue");
        exercise4();
    }

    private static void setYX(Node node, int y, int x) {
        node.setAttribute("y", y);
        node.setAttribute("x", x);
    }

    private static void addIdLabel(Element... elements) {
        for (Element element : elements) {
            element.setAttribute("ui.label", element.getId());
        }
    }

    private static void exercise1() {
        SingleGraph myGraph = new SingleGraph("first graph");
        Node a = myGraph.addNode("a");
        Node b = myGraph.addNode("b");
        Node c = myGraph.addNode("c");
        Node d = myGraph.addNode("d");
        myGraph.addEdge("a--b", a, b);
        myGraph.addEdge("b--c", b, c);
        myGraph.addEdge("c--d", c, d);
        myGraph.addEdge("d--a", d, a);
        setYX(a, 0, 1);
        setYX(b, 1, 0);
        setYX(c, 2, 1);
        setYX(d, 1, 2);
        myGraph.display(false);
    }

    private static void exercise2() {
        SingleGraph myGraph = new SingleGraph("first graph");
        Node a = myGraph.addNode("a");
        Node b = myGraph.addNode("b");
        Node c = myGraph.addNode("c");
        Node d = myGraph.addNode("d");
        myGraph.addEdge("a--b", a, b);
        myGraph.addEdge("b--c", b, c);
        myGraph.addEdge("c--d", c, d);
        myGraph.addEdge("d--a", d, a);
        setYX(a, 0, 1);
        setYX(b, 1, 0);
        setYX(c, 2, 1);
        setYX(d, 1, 2);
        myGraph.display(false);

        String stylesheet = "graph { fill-color: lightblue; }";
        myGraph.addAttribute("ui.stylesheet", stylesheet);

        int randomNodeIndex = RANDOM.nextInt(myGraph.getNodeCount());
        myGraph.getNode(randomNodeIndex).setAttribute("ui.style", "shape:cross;size:30px;fill-color:#7733aa;");
    }

    private static void exercise3() {
        try {
            // 1
            SingleGraph dgsGraph = new SingleGraph("DGS graph");
            dgsGraph.read("dgs/savedgraph.dgs");
            dgsGraph.display(true);

            // 2
            FileSinkImages fsi = new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.SVGA);
            fsi.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
            fsi.writeAll(dgsGraph, "pictures/graphbeforemodifications.png");

            // 3
            int nodeCountBefore = dgsGraph.getNodeCount();
            Node n1 = dgsGraph.addNode("Added node 1");
            Node n2 = dgsGraph.addNode("Added node 2");
            Edge e1 = dgsGraph.addEdge("Added vert 1", n1, dgsGraph.getNode(0));
            Edge e2 = dgsGraph.addEdge("Added vert 2", n2, dgsGraph.getNode(nodeCountBefore - 1));
            addIdLabel(n1, n2, e1, e2);

            String stylesheet = dgsGraph.getAttribute("ui.stylesheet");
            stylesheet = stylesheet.replace("lightblue", "cyan");
            dgsGraph.setAttribute("ui.stylesheet", stylesheet);

            // 4
            fsi.writeAll(dgsGraph, "pictures/graphaftermodifications.png");

            // 5
            dgsGraph.write("dgs/modifiedgraph.dgs");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void exercise4() {
        // 1
        SingleGraph dgsGraph = Tools.read("dgs/savedgraph.dgs");
        dgsGraph.display(true);

        // 2
        Tools.screenshot(dgsGraph, "pictures/graphbeforemodifications.png");

        // 3
        int nodeCountBefore = dgsGraph.getNodeCount();
        Node n1 = dgsGraph.addNode("Added node 1");
        Node n2 = dgsGraph.addNode("Added node 2");
        Edge e1 = dgsGraph.addEdge("Added vert 1", n1, dgsGraph.getNode(0));
        Edge e2 = dgsGraph.addEdge("Added vert 2", n2, dgsGraph.getNode(nodeCountBefore - 1));
        addIdLabel(n1, n2, e1, e2);

        String stylesheet = dgsGraph.getAttribute("ui.stylesheet");
        stylesheet = stylesheet.replace("lightblue", "cyan");
        dgsGraph.setAttribute("ui.stylesheet", stylesheet);

        // 4
        Tools.screenshot(dgsGraph, "pictures/graphaftermodifications.png");

        // 5
        Tools.write(dgsGraph, "dgs/modifiedgraph.dgs");
    }

    private static void hitAKey(String msg) {
        System.out.println(msg);
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
