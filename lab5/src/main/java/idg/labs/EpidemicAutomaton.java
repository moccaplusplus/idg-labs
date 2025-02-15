package idg.labs;

import idg.labs.Individual.State;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static idg.labs.Individual.State.INFECTED;
import static idg.labs.Individual.State.RECOVERED;
import static idg.labs.Individual.State.SUSCEPTIBLE;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

public class EpidemicAutomaton {
    private final Random random = new Random();
    private final double recoveryProbability;
    private final double infectionProbability;
    private final int immunityTimeSpan;
    private List<Individual> population;

    public EpidemicAutomaton(
            double recoveryProbability,
            double infectionProbability,
            int immunityTimeSpan
    ) {
        this.recoveryProbability = recoveryProbability;
        this.infectionProbability = infectionProbability;
        this.immunityTimeSpan = immunityTimeSpan;
    }

    public void init(SingleGraph populationGraph, long seed) {
        random.setSeed(seed);
        population = populationGraph.getNodeSet().stream()
                .sorted(Comparator.comparingInt(Node::getIndex))
                .map(Individual::new)
                .collect(toList());
        Individual patientZero = population.get(random.nextInt(population.size()));
        patientZero.setState(INFECTED);
    }

    public boolean step() {
        Map<State, List<Individual>> compartments = population.stream()
                .collect(groupingBy(Individual::getState));

        if (population.size() == compartments.getOrDefault(SUSCEPTIBLE, emptyList()).size()) {
            return true;
        }

        compartments.getOrDefault(RECOVERED, emptyList()).stream()
                .peek(Individual::tick)
                .filter(individual -> individual.getTimer() > immunityTimeSpan)
                .forEach(individual -> individual.setState(SUSCEPTIBLE));

        Map<Boolean, List<Individual>> recoveringOrNot = compartments.getOrDefault(INFECTED, emptyList()).stream()
                .peek(Individual::tick)
                .collect(partitioningBy(this::isRecovering));

        recoveringOrNot.getOrDefault(true, emptyList()).forEach(individual -> individual.setState(RECOVERED));

        recoveringOrNot.getOrDefault(false, emptyList()).stream()
                .flatMap(this::neighbours)
                .distinct()
                .filter(neighbour -> neighbour.getState() == SUSCEPTIBLE)
                .filter(susceptible -> exposeToInfection())
                .forEach(exposed -> exposed.setState(INFECTED));

        return false;
    }

    private Stream<Individual> neighbours(Individual individual) {
        return individual.getNode().getEdgeSet().stream()
                .map(edge -> (Node) edge.getOpposite(individual.getNode()))
                .mapToInt(Node::getIndex)
                .mapToObj(population::get);
    }

    private boolean exposeToInfection() {
        double r = random.nextDouble();
        return r < infectionProbability;
    }

    private boolean isRecovering(Individual infected) {
        double p = 1 - Math.pow(1 - recoveryProbability, infected.getTimer());
        double r = random.nextDouble();
        return r < p;
    }

    public String populationSplitInfo() {
        return population.stream()
                .collect(groupingBy(Individual::getState, counting()))
                .entrySet().stream()
                .map(entry -> format("%s=%d", entry.getKey(), entry.getValue()))
                .collect(joining(", "));
    }

    public String settingsInfo() {
        return "recoveryProbability=" + recoveryProbability
                + ", infectionProbability=" + infectionProbability
                + ", immunityTimeSpan=" + immunityTimeSpan;
    }
}
