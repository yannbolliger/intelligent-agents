package template;

import java.util.*;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

    private static double EPSILON = 0.001;

	private int numActions;
	private Agent myAgent;
	private Map<State, Action> strategy;
	private TaskDistribution td;
    private double costPerKm;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		final double discount = agent
                .readProperty("discount-factor", Double.class, 0.95);

		this.numActions = 0;
		this.myAgent = agent;
		this.td = td;
		this.costPerKm = myAgent.vehicles().get(0).costPerKm();

		// spaces
        List<State> stateSpace = stateSpace(topology);
        List<ActionSpaceElem> actionSpace = actionSpace(topology);

        // "tables"
        Map<State, Double> v = new HashMap<>();
        Map<State, Double> newV = new HashMap<>();
        Map<State, Action> best = new HashMap<>();

        do {
            v = newV;
            newV = new HashMap<>();

            for (State s : stateSpace) {
                double max = Double.NEGATIVE_INFINITY;
                ActionSpaceElem bestAction = null;

                for (ActionSpaceElem a: actionSpace) {

                    double sum = 0;
                    for (State sPrime : stateSpace) {
                        sum += transition(s, a, sPrime) *
                                v.getOrDefault(sPrime, 0.0);
                    }

                    double value = reward(s, a) + discount * sum;

                    if (value > max) {
                        max = value;
                        bestAction = a;
                    }
                }
                newV.put(s, max);
                best.put(s, bestAction.getAction());
            }
        } while (!goodEnough(v, newV));

        strategy = Collections.unmodifiableMap(best);
	}

	private boolean goodEnough(Map<State, Double> v, Map<State, Double> vPrime) {
        double squares = 0;

        for (State s : v.keySet()) {
            squares += Math.pow(v.get(s) - vPrime.get(s), 2);
        }
        return squares/v.size() < EPSILON;
    }

	private double transition(State current, ActionSpaceElem a, State next) {
	    // pickup action
	    if (a.isPickupAction()) {
	        // there must be a task to pick up
	        if (!current.hasDestination()) return 0;

	        // the next state must start in the destination city
            if (!next.getCurrent().equals(current.getTo())) return 0;

	        return td.probability(next.getCurrent(), next.getTo());
        }
        // move action
	    else {
	        City moveCity = a.getMoveToCity();

	        boolean moveCityIsNeighbor =
                    current.getCurrent().hasNeighbor(moveCity);

	        boolean nextStateIsMoveCity = next.getCurrent().equals(moveCity);

	        if (moveCityIsNeighbor && nextStateIsMoveCity) {
                return td.probability(next.getCurrent(), next.getTo());
            }
	        else return 0;
        }
    }

    private double reward(State s, ActionSpaceElem a) {
        // illegal combinations give 0 reward
        if (a.isPickupAction() && !s.hasDestination()) {
            return Double.NEGATIVE_INFINITY;
        }

        if (a.isMoveAction()) {
            return -costPerKm * s.getCurrent().distanceTo(a.getMoveToCity());
        }

        return td.reward(s.getCurrent(), s.getTo()) -
                costPerKm * s.getCurrent().distanceTo(s.getTo());
    }

	private List<State> stateSpace(Topology topology) {
        List<State> stateSpace = new ArrayList<>();
        for (City c : topology.cities()) {
            stateSpace.add(new State(c, null));

            for (City to : topology.cities()) {
                if (!c.equals(to)) stateSpace.add(new State(c, to));
            }
        }
        return stateSpace;
    }

    private List<ActionSpaceElem> actionSpace(Topology topology) {
        List<ActionSpaceElem> actionSpace = new ArrayList<>();
        actionSpace.add(new ActionSpaceElem(new Pickup(null)));

        for (City c : topology.cities()) {
            actionSpace.add(new ActionSpaceElem(new Move(c), c));
        }
        return actionSpace;
    }

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {

	    final City destination = (availableTask != null) ?
                availableTask.deliveryCity : null;

	    final State state = new State(vehicle.getCurrentCity(), destination);

	    Action action = strategy.get(state);

	    if (action instanceof Pickup) {
	        action = new Pickup(availableTask);
        }
		
		if (numActions >= 1) {
			System.out.println("The total profit after " +
                    numActions+" actions is " + myAgent.getTotalProfit() +
                    " (average profit: " +
                    (myAgent.getTotalProfit() / (double)numActions) + ")"
            );
		}
		numActions++;
		
		return action;
	}
}
