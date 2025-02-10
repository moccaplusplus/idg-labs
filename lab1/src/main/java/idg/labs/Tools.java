package idg.labs;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.stream.file.FileSinkImages;

import java.io.IOException;

public class Tools {
    private static final FileSinkImages FILE_SINK_IMAGES = new FileSinkImages(
            FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.SVGA);
    static {
        FILE_SINK_IMAGES.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
    }

    public static SingleGraph read(String file) {
        SingleGraph graph = new SingleGraph("Graph from file: " + file);
        try {
            graph.read(file);
        } catch (IOException | GraphParseException e) {
            throw new IllegalStateException(e);
        }
        return graph;
    }

    public static void write(SingleGraph graph, String file) {
        try {
            graph.write(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void screenshot(SingleGraph graph, String file) {
        try {
            FILE_SINK_IMAGES.writeAll(graph, file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
