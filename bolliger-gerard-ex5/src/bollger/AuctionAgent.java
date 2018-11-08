package bollger;

//the list of imports
import java.io.File;
import java.util.*;

import logist.LogistSettings;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */

public class AuctionAgent implements AuctionBehavior {

    private long timeoutSetup;
    private long timeoutPlan;
    private long timeoutBid;

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;

	private CentralizedPlanner planner;
	private TaskSet wonTasks;
	private Solution currentAssignment;
	private Solution assignmentWithBiddedTask;

	private int round = 0;


	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator +
                    "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println(
                    "There was a problem loading the configuration file."
            );
        }

        this.timeoutSetup = ls.get(LogistSettings.TimeoutKey.SETUP);
        this.timeoutBid = ls.get(LogistSettings.TimeoutKey.BID);
        this.timeoutPlan = ls.get(LogistSettings.TimeoutKey.PLAN);

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;

		this.wonTasks = TaskSet.create(new Task[0]);
		this.currentAssignment = Solution.initial(agent.vehicles(), wonTasks);

		this.planner = new CentralizedPlanner();
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {

	    // do bookkeeping if task was won
	    if (winner == agent.id()) {
            wonTasks.add(previous);
            currentAssignment = assignmentWithBiddedTask;
        }

        ++round;
        assignmentWithBiddedTask = null;

	}

	@Override
	public Long askPrice(Task task) {
        assignmentWithBiddedTask = planner.plan();

        double marginalCost = assignmentWithBiddedTask.getCost() -
                currentAssignment.getCost();

        distribution.
		return null;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		assert(vehicles.equals(agent.vehicles()));
		assert(tasks.equals(wonTasks));

		Solution finalSolution = planner.plan();

	    return finalSolution.getPlan();
	}
}
