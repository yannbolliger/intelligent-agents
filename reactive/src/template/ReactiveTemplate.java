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

    private static double EPSILON = 0.1;

	private int numActions;
	private Agent myAgent;
	private Map<State, Action> strategy;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		final double discount = agent
                .readProperty("discount-factor", Double.class, 0.95);

		this.numActions = 0;
		this.myAgent = agent;

		// spaces
        List<State> stateSpace = stateSpace(topology);
        List<Action> actionSpace = actionSpace(topology);

        // "tables"
        Map<State, Double> v = new HashMap<>();
        Map<State, Double> newV = new HashMap<>();
        Map<State, Action> best = new HashMap<>();

        do {
            for (State s : stateSpace) {
                double max = Double.MIN_VALUE;
                Action bestAction = null;

                for (Action a: actionSpace) {

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
                best.put(s, bestAction);
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

	private double transition(State s, ActionSpaceElem a, State sPrime) {
	    if (!s.getTo().equals(sPrime.getCurrent())) return 0;

	    // pickup action
	    if (a.isPickupAction()) {
	        if (s.getTo() == null) return 0;
	        else return td.probability(s.getCurrent(), s.getTo());
        }
        // move action
	    else {
	        if (!s.getCurrent().hasNeighbor(a.moveToCity())) return 0;
	        else return 1;
        }
    }

    private double reward(State s, Action a) {
        return 0.0;
    }

	private List<State> stateSpace(Topology topology) {
        List<State> stateSpace = new ArrayList<>();
        for (City c : topology.cities()) {
            stateSpace.add(new State(c, null));

            for (City to : topology.cities()) {
                stateSpace.add(new State(c, to));
            }
        }
        return stateSpace;
    }

    private List<Action> actionSpace(Topology topology) {
        List<Action> actionSpace = new ArrayList<>();
        actionSpace.add(new Pickup(null));

        for (City c : topology.cities()) {
            actionSpace.add(new Move(c));
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
