package idg.labs;

import org.graphstream.graph.implementations.SingleGraph;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum Topology {
    gridVonNeumann(() -> Generators.grid(10, 10, false)),
    gridMoore(() -> Generators.grid(10, 10, true)),
    torusVonNeumann(() -> Generators.torus(10, 10, false)),
    torusMoore(() -> Generators.torus(10, 10, true)),
    erdosRenyi(() -> Generators.erdosRenyi(100, 0.2)),
    planar(() -> Generators.planar(100)),
    fullyConnected(() -> Generators.fullyConnected(100)),
    ring(() -> Generators.ring(100)),
    preferentialAttachment(() -> Generators.preferentialAttachment(100, 5));

    public static Stream<Topology> stream() {
        return Arrays.stream(values());
    }

    public static void forEach(BiConsumer<Topology, SingleGraph> consumer) {
        stream().forEach(topology -> consumer.accept(topology, topology.graph.get()));
    }

    public final Supplier<SingleGraph> graph;

    Topology(Supplier<SingleGraph> graph) {
        this.graph = graph;
    }
}
