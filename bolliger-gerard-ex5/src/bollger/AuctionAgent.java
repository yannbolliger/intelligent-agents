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
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */

public class AuctionAgent implements AuctionBehavior {

    private static final int LOSS_ROUNDS = 6;
    private static final int ROUNDS_TO_PROFIT = 10;
    private static final long BID_DELTA = 1_000;

    private long timeoutSetup;
    private long timeoutPlan;
    private long timeoutBid;

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;

	private Map<Integer, Double> expectedLoadOnEdge;

	private CentralizedPlanner planner;
	private Solution currentAssignment;
	private double currentEstimatedMaxGain = 0;
	private Solution assignmentWithBiddedTask;

	private int round = 0;
	private double gains = 0;
	private int numberWonTasks = 0;
	private long rewardSum = 0;


	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {
        long timeStart = System.nanoTime();

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings(
                    "config" + File.separator + "settings_auction.xml"
            );
        }
        catch (Exception e) {
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
		this.expectedLoadOnEdge = new HashMap<>();


		for (City pickupCity : topology.cities()) {
			for (City deliveryCity : topology.cities()) {
				Iterator<City> pathEdges = pickupCity.pathTo(deliveryCity).iterator();
				City previousCity = null;
				while (pathEdges.hasNext()) {
				    City currentCity = pathEdges.next();
					if (previousCity != null) {
					    int edgeHash = getEdgeHash(previousCity, currentCity);
						double expectedLoad = expectedLoadOnEdge.getOrDefault(edgeHash, 0.);

						expectedLoad +=
                                distribution.probability(previousCity, currentCity)
                                * distribution.weight(previousCity, currentCity);

						expectedLoadOnEdge.put(edgeHash, expectedLoad);
					}
					previousCity = currentCity;
				}
			}
		}


		TaskSet emptySet = TaskSet.create(new Task[0]);
		this.currentAssignment = Solution.initial(agent.vehicles(), emptySet);

		this.planner = new CentralizedPlanner(
		        agent.vehicles(), timeoutBid - BID_DELTA
        );
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {

	    // do bookkeeping if task was won
	    if (winner == agent.id()) {
	        ++numberWonTasks;
	        rewardSum += previous.reward;
            gains = rewardSum - assignmentWithBiddedTask.getCost();
            currentAssignment = assignmentWithBiddedTask;
        }

        ++round;
        assignmentWithBiddedTask = null;
	}

	@Override
	public Long askPrice(Task task) {
	    Double price = askPriceDouble(task);
        return price == null ? null : (long) Math.ceil(price);
	}

	private Double askPriceDouble(Task task) {
        assignmentWithBiddedTask = planner.plan(task, currentAssignment);

        double marginalCost = assignmentWithBiddedTask.getCost() -
                currentAssignment.getCost();

        double marginalEstimatedMaxGain = currentEstimatedMaxGain
                - assignmentWithBiddedTask.estimatedMaxGain(expectedLoadOnEdge, round);

        if (round + numberWonTasks < LOSS_ROUNDS) {
            return marginalCost - marginalEstimatedMaxGain * 0.4;
        }
        else if (gains < 0) {
            return marginalCost + -gains/Math.max(1, ROUNDS_TO_PROFIT - round);
        }

        return marginalCost + 1;
    }

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		return new CentralizedPlanner(agent.vehicles(), timeoutPlan).plan(tasks);
	}

	private int getEdgeHash(City from, City to) {
	    return from.hashCode() + to.hashCode();
    }
}
