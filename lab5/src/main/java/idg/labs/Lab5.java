package idg.labs;

import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;

import static idg.labs.Generators.torus;
import static idg.labs.Individual.State.INFECTED;
import static idg.labs.Individual.State.RECOVERED;
import static idg.labs.Individual.State.SUSCEPTIBLE;

public class Lab5 {
    private static final int SEED = 3;
    private static final long GUI_DELAY_MILLIS = 100;
    private static final int TORUS_DIM = 60;
    private static final String STYLESHEET = "graph { fill-color:white;padding:20px; }\n"
            + "node { size:6px;shape:circle; }\n"
            + "node." + SUSCEPTIBLE + " { fill-color:#9cf; }\n"
            + "node." + INFECTED + " { fill-color:#e30; }\n"
            + "node." + RECOVERED + " { fill-color:#0b3; }\n"
            + "edge { size:1px;fill-color:#ddd;}\n";

    static {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        EpidemicAutomaton automaton1 = new EpidemicAutomaton(0.05, 0.15, 30);
        // this automaton seems to get extinct after 218 iteration in von Neumann version
        run(automaton1, false, 500);
        hitAKey();
        // but infection stays active but stable in Moore version
        run(automaton1, true, 500);
        hitAKey();

        EpidemicAutomaton automaton2 = new EpidemicAutomaton(0.02, 0.2, 40);
        // infection stays active but stable with waves of stronger and weaker infection volume observed
        // in von Neumann version
        run(automaton2, false, 500);
        hitAKey();
        // in Moore version, almost the whole population gets infected, but then the epidemic gets extinct.
        run(automaton2, true, 500);
        hitAKey();

        // Simulations were done with seed=3, the results should be stable across multiple runs.
    }

    private static void run(EpidemicAutomaton automaton, boolean moore, int maxIterations) throws InterruptedException {
        System.out.printf("Automaton settings: %s%n", automaton.settingsInfo());
        System.out.printf("Graph: torus with %s neighbourhood%n", moore ? "Moore" : "von Neumann");

        SingleGraph graph = torus("Simulation of Epidemics", TORUS_DIM, TORUS_DIM, moore);
        graph.addAttribute("ui.stylesheet", STYLESHEET);
        graph.addAttribute("ui.antialias");
        graph.display(true);

        automaton.init(graph, SEED);

        int i = 0;
        while (++i < maxIterations) {
            Thread.sleep(GUI_DELAY_MILLIS);
            if (automaton.step()) {
                System.out.printf("Infection extinct after %d iterations%n", i);
                return;
            }
            if (i % 50 == 0) {
                System.out.printf("Population split after %d iterations: %s%n", i, automaton.populationSplitInfo());
            }
        }
        System.out.println("Stopped simulation after iterations limit met");
    }

    private static void hitAKey() throws IOException {
        System.out.println("Press enter to continue...");
        System.in.read();
    }
}
