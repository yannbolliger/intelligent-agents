package template;


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


/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */

public class CentralizedAgent implements CentralizedBehavior {
    private static final double P = 0.6;
    private static final long DELTA = 1000;

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeoutSetup;
    private long timeoutPlan;
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println(
                    "There was a problem loading the configuration file."
            );
        }
        
        // the setup method cannot last more than timeoutSetup milliseconds
        timeoutSetup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeoutPlan milliseconds
        timeoutPlan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long timeStart = System.currentTimeMillis();

        Solution solution = Solution.initial(vehicles, tasks);
        List<Solution> neighbors = new ArrayList<>();

        while (!runningOutOfTime(timeStart)) {
            List<Solution> newNeighbors = solution.localNeighbors();
            neighbors.addAll(newNeighbors);

            Solution newSolution = localChoice(neighbors);

            if (newSolution != null) {
                solution = newSolution;
                neighbors = new ArrayList<>();
            }
        }
        List<Plan> plans = solution.getPlans();

        // log used time
        long timeEnd = System.currentTimeMillis();
        long duration = timeEnd - timeStart;
        System.out.println(
                "The plan was generated in " + duration + " milliseconds with cost: " + solution.getCost()
        );
        return plans;
    }

    private boolean runningOutOfTime(long timeStart) {
        long elapsedTime = System.currentTimeMillis() - timeStart;

        return elapsedTime >= timeoutPlan - 2 * DELTA;
    }

    private Solution localChoice(List<Solution> solutions) {
        if (Math.random() > P) return null;

        double minCost = Double.POSITIVE_INFINITY;
        Solution bestSolution = null;

        for (Solution solution : solutions) {
            double solutionCost = solution.getCost();
            if (solutionCost < minCost){
                bestSolution = solution;
                minCost = solutionCost;
            }
        }
        return bestSolution;
    }
}
