package idg.labs;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import java.io.IOException;
import java.util.Random;

public class Tools {
    public static final Random RANDOM = new Random();

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static SingleGraph read(String file) throws IOException, GraphParseException {
        SingleGraph graph = new SingleGraph("Graph from file: " + file);
        graph.read(file);
        graph.addAttribute("ui.antialias");
        return graph;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void hitAKey(String msg) throws IOException {
        System.out.println(msg);
        System.in.read();
    }
}
