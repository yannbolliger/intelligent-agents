package template;


import java.util.*;

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
    private static final double P = 0.3;
    private static final long DELTA = 3_000;
    private static final int MAX_NEIGHBORS_SIZE = 250_000;

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

        Solution nextSolution = Solution.initial(vehicles, tasks);
        Solution bestSolution = nextSolution;
        List<Solution> neighbors = new LinkedList<>();
        Set<Solution> formerSolutions = new HashSet<>();
        formerSolutions.add(bestSolution);

        while (!runningOutOfTime(timeStart)) {
            List<Solution> newNeighbors = nextSolution.localNeighbors();
            neighbors.addAll(newNeighbors);
            neighbors.removeAll(formerSolutions);

            Solution newSolution = localChoice(neighbors);

            if (newSolution != null) {
                formerSolutions.add(newSolution);
                nextSolution = newSolution;
                neighbors = new LinkedList<>();

                if (nextSolution.getCost() < bestSolution.getCost()) {
                    bestSolution = nextSolution;
                }
            }
        }
        List<Plan> plans = bestSolution.getPlans();

        // log used time
        long timeEnd = System.currentTimeMillis();
        long duration = timeEnd - timeStart;
        System.out.println(
                "The plan was generated in " + duration +
                        " milliseconds with cost: " + bestSolution.getCost()
        );
        return plans;
    }

    private boolean runningOutOfTime(long timeStart) {
        long elapsedTime = System.currentTimeMillis() - timeStart;

        return elapsedTime >= timeoutPlan - DELTA;
    }

    private Solution localChoice(List<Solution> solutions) {
        if (Math.random() > 1 - P && solutions.size() < MAX_NEIGHBORS_SIZE) {
            return null;
        }

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
