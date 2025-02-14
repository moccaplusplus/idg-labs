package idg.labs;

import org.graphstream.graph.Node;

public class Individual {
    public enum State {
        SUSCEPTIBLE,
        INFECTED,
        RECOVERED;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final Node node;
    private State state;
    private int timer;

    public Individual(Node node) {
        this.node = node;
        setState(State.SUSCEPTIBLE);
    }

    public void setState(State state) {
        this.state = state;
        node.setAttribute("ui.class", state.toString());
        timer = 0;
    }

    public State getState() {
        return state;
    }

    public int getTimer() {
        return timer;
    }

    public void tick() {
        timer++;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Individual) {
            return node.getIndex() == ((Individual) obj).node.getIndex();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return node.getIndex();
    }
}
