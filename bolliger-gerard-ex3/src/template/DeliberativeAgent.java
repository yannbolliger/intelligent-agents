package template;

/* import table */
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.Map;

/**
 * An optimal planner for one vehicle.
 */
public class DeliberativeAgent implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		capacity = agent.vehicles().get(0).capacity();

		// Throws IllegalArgumentException if algorithm is unknown
        String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// ...
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		switch (algorithm) {
            case ASTAR:
                plan = new AStarSearchPlan(vehicle, tasks).plan();
                break;
            case BFS:
                plan = bfsPlan(vehicle, tasks);
                break;
            default:
                throw new AssertionError("Should not happen.");
		}		
		return plan;
	}
	
	private Plan bfsPlan(Vehicle vehicle, TaskSet tasks) {

        LinkedList<StatePlanPair> q = new LinkedList();
        List<State> cyleSet = new ArrayList<>();

        State startState = new State(vehicle.getCurrentCity(), vehicle.getCurrentTasks(), tasks);

        q.add(new StatePlanPair(startState, new LinkedList<Action>()));
        cyleSet.add(startState);

	    while (!q.isEmpty()) {
            StatePlanPair statePlanPair = q.poll();

            if (statePlanPair.getState().isGoal()) {
                return new Plan(vehicle.getCurrentCity(), statePlanPair.getActions());
            }

            Map<State, Entry<Action, Double>> children = statePlanPair.getState().nextStates(capacity);

            for(Entry<State, Entry<Action, Double>> child : children.entrySet()) {
                List<Action> childPlan = statePlanPair.getActions();
                childPlan.add(child.getValue().getKey());

                if (!cyleSet.contains(child.getKey())) {
                    cyleSet.add(child.getKey());
                    q.addLast(new StatePlanPair(child.getKey(), childPlan));
                }
            }
        }
        return null;
    }

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
