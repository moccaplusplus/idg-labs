package idg.labs;

import org.graphstream.stream.GraphParseException;

import java.io.IOException;

public class Lab2 {
    public static void main(String[] args) throws IOException, GraphParseException {
        Exercise1.main();
        Tools.hitAKey("Hit a key to continue");
        Exercise2.main();
        Tools.hitAKey("Hit a key to continue");
        Exercise3.main();
        Tools.hitAKey("Hit a key to continue");
        Exercise4.main();
        Tools.hitAKey("Hit a key to continue");
        Exercise5.main();
        Tools.hitAKey("Hit a key to continue");
        Exercise6.main();
    }
}