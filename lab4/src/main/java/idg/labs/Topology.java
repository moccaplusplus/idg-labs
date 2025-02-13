package idg.labs;

import org.graphstream.graph.implementations.SingleGraph;

import java.util.function.Function;

import static idg.labs.Generators.erdosRenyi;
import static idg.labs.Generators.fullyConnected;
import static idg.labs.Generators.grid;
import static idg.labs.Generators.planar;
import static idg.labs.Generators.preferentialAttachment;
import static idg.labs.Generators.ring;
import static idg.labs.Generators.torus;

public enum Topology {
    GRID_VON_NEUMANN("Grid (w=10, h=10, type=von Neumann)", id -> grid(id, 10, 10, false)),
    GRID_MOORE("Grid (w=10, h=10, type=Moore)", id -> grid(id, 10, 10, true)),
    TORUS_VON_NEUMANN("Torus (w=10, h=10, type=von Neumann)", id -> torus(id, 10, 10, false)),
    TORUS_MOORE("Torus (w=10, h=10, type=Moore)", id -> torus(id, 10, 10, true)),
    ERDOS_RENYI("Erdos-Renyi (n=100, p=2)", id -> erdosRenyi(id, 100, 0.2)),
    PLANAR("Planar (n=100)", id -> planar(id, 100)),
    FULLY_CONNECTED("Fully Connected (n=100)", id -> fullyConnected(id, 100)),
    RING("Ring (n=100)", id -> ring(id, 100)),
    PREFERENTIAL_ATTACHMENT("Preferential Attachment (n=100, k=5)", id -> preferentialAttachment(id, 100, 5));

    private final String displayName;
    private final Function<String, SingleGraph> generator;

    Topology(String displayName, Function<String, SingleGraph> generator) {
        this.displayName = displayName;
        this.generator = generator;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public SingleGraph graph() {
        return generator.apply(displayName);
    }
}
