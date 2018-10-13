package template;

import logist.plan.Action;
import logist.plan.Plan;

import java.util.LinkedList;
import java.util.List;

public class StatePlanPair {
    private State state;
    private LinkedList<Action> plan;

    public StatePlanPair(State state, List<Action> plan) {
        this.state = state;
        this.plan = (LinkedList) plan;
    }

    public State getState() {
        return state;
    }

    public  List<Action> getActions() {
        return (LinkedList) plan.clone();
    }
}
