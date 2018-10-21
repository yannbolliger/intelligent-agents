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
		Long startTime = System.nanoTime();
		switch (algorithm) {
            case ASTAR:
                plan = new AStarSearchPlan(vehicle, tasks).plan();
                break;
            case BFS:
                plan = new BFSearchPlan(vehicle, tasks).plan();
                break;
            default:
                throw new AssertionError("Should not happen.");
		}
		Long elapsedTime = System.nanoTime() - startTime;
		System.out.println("time to compute Plan with " + algorithm + " algo and " + tasks.size() + " tasks is: " + elapsedTime + " reward: " + plan.totalDistance());
		return plan;
	}


	@Override
	public void planCancelled(TaskSet carriedTasks) {
        /**
         * We don't need to do anything here, because our planning classes
         * always check for vehicle.getCurrentTasks() and therefore already
         * consider the carriedTasks.
         */
	}
}
