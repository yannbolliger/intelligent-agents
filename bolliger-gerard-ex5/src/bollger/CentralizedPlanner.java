package bollger;


import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.CentralizedBehavior;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.*;


/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */

public class CentralizedPlanner {
    private static final double P = 0.4;
    private static final long DELTA = 3_000;
    private static final int MAX_NEIGHBORS_SIZE = 250_000;

    private Agent agent;
    private long timeoutPlan;

    public CentralizedPlanner(Agent agent, long timeoutPlan) {
        // the plan method cannot execute more than timeoutPlan milliseconds
        this.timeoutPlan = timeoutPlan;
        this.agent = agent;
    }


    public Solution plan(Task task, Solution previousSolution) {
        Solution initialSolution = previousSolution.addTask(task);
        return findBestSolution(initialSolution);
    }

    public List<Plan> plan(TaskSet tasks) {
        long timeStart = System.currentTimeMillis();
        List<Vehicle> vehicles = agent.vehicles();
        Solution initialSolution = Solution.initial(vehicles, tasks);
        Solution bestSolution = findBestSolution(initialSolution);
        List<Plan> plans = bestSolution.getPlans();
        return plans;
    }

    private Solution findBestSolution(Solution initialSolution) {
        long timeStart = System.currentTimeMillis();
        List<Vehicle> vehicles = agent.vehicles();
        Solution nextSolution = initialSolution;
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
        return bestSolution;
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
