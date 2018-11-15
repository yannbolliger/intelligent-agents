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

    private static final int LOSS_ROUNDS = 3;
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
	private TaskSet wonTasks;
	private Solution currentAssignment;
	private Solution assignmentWithBiddedTask;

	private int round = 0;
	private double gains = 0;


	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {
        long timeStart = System.nanoTime();

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings(
                    "config" + File.separator + "settings_default.xml"
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


		this.wonTasks = TaskSet.create(new Task[0]);
		this.currentAssignment = Solution.initial(agent.vehicles(), wonTasks);

		this.planner = new CentralizedPlanner(agent, timeoutBid - BID_DELTA);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {

	    // do bookkeeping if task was won
	    if (winner == agent.id()) {
            wonTasks.add(previous);
            gains = wonTasks.rewardSum() - assignmentWithBiddedTask.getCost();
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

        if (round < LOSS_ROUNDS) {
            return costOnlyDeliveryFirstTask(task);
        }
        else if (gains < 0) {
            return marginalCost + -gains/Math.max(1, ROUNDS_TO_PROFIT - round);
        }

        return marginalCost + 1;
    }

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		return new CentralizedPlanner(agent, timeoutPlan).plan(tasks);
	}

	private double costOnlyDeliveryFirstTask(Task task) {
	    double minCost = Double.POSITIVE_INFINITY;
	    Vehicle cheapestVehicle = null;

	    for (Vehicle v : agent.vehicles()) {
	        if (v.costPerKm() < minCost) {
	            minCost = v.costPerKm();
	            cheapestVehicle = v;
            }
        }

        return task.pathLength() * cheapestVehicle.costPerKm();
    }

	private int getEdgeHash(City from, City to) {
	    return from.hashCode() + to.hashCode();
    }
}
