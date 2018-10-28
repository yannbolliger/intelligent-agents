package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import logist.LogistSettings;

import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

import javax.xml.transform.SourceLocator;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */

public class CentralizedTemplate implements CentralizedBehavior {
    private static final double p = 0.6;
    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeoutSetup;
    private long timeout_plan;
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeoutSetup milliseconds
        timeoutSetup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long timeStart = System.currentTimeMillis();
        long delta = 10;

        Solution solution = Solution.initial(vehicles, tasks);

        List<Solution> neighbors = new ArrayList<>();
        while (!runningOutOfTime(timeStart, delta)) {
            List<Solution> newNeighbors = solution.localNeighbors();
            neighbors.addAll(neighbors);

            Solution newSolution = localChoice(neighbors);

            if (newSolution != null) {
                solution = newSolution;
                neighbors = new ArrayList<>();
            }
        }

        // log used time
        long time_end = System.currentTimeMillis();
        long duration = time_end - timeStart;
        System.out.println(
                "The plan was generated in " +
                        duration + " milliseconds."
        );
        return solution.getPlans();
    }

    private boolean runningOutOfTime(long timeStart, long delta) {
        long elapsedTime = System.currentTimeMillis() - timeStart;

        return elapsedTime >= timeout_plan - 2 * delta;
    }

    private Solution localChoice(List<Solution> solutions) {
        if (Math.random() > p) {
            return null;
        }
        double minCost = Double.POSITIVE_INFINITY;
        Solution bestSolution = null;
        for(Solution solution : solutions) {
            Double solutionCost = solution.getCost();
            if (solutionCost < minCost){
                bestSolution = solution;
                minCost = solutionCost;
            }
        }
        return bestSolution;
    }
/*
    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
    */
}
