package bollger;

import logist.topology.Topology.City;

import java.util.Objects;

public final class State {

    private final City current;
    private final City to;

    public State(City current, City to) {
        this.current = current;
        this.to = to;
    }

    public City getCurrent() {
        return current;
    }

    public City getTo() {
        return to;
    }

    public boolean hasDestination() {
        return to != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return Objects.equals(current, state.current) &&
                Objects.equals(to, state.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(current, to);
    }
}
