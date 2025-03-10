package bollger;


import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskSet;

import java.util.*;


/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */

public class CentralizedPlanner {
    private static final double P = 0.8;
    private static final long DELTA = 200;
    private static final int MAX_NEIGHBORS_SIZE = 250_000;

    private List<Vehicle> vehicles;
    private long timeoutPlan;

    public CentralizedPlanner(List<Vehicle> vehicles, long timeoutPlan) {
        // the plan method cannot execute more than timeoutPlan milliseconds
        this.timeoutPlan = timeoutPlan;
        this.vehicles = vehicles;
    }


    public Solution plan(Task task, Solution previousSolution) {
        long timeStart = System.currentTimeMillis();

        return findBestSolution(previousSolution.addTask(task), timeStart);
    }

    public List<Plan> plan(TaskSet tasks, Solution previousSolution) {
        long timeStart = System.currentTimeMillis();

        // we don't have to search for a solution if there are no tasks
        if (tasks.isEmpty()) return previousSolution.getPlans();

        // translate previous solution to new task set
        Solution initialSolution = previousSolution.translateTo(tasks);

        Solution bestSolution = findBestSolution(initialSolution, timeStart);

        List<Plan> plans = bestSolution.getPlans();
        System.out.println("Total cost is " + bestSolution.getCost());
        return plans;
    }

    private Solution findBestSolution(Solution initialSolution, long timeStart) {

        Solution nextSolution = initialSolution;
        Solution bestSolution = nextSolution;

        List<Solution> neighbors = new LinkedList<>();

        while (!runningOutOfTime(timeStart)) {
            List<Solution> newNeighbors = nextSolution.localNeighbors();
            neighbors.addAll(newNeighbors);

            Solution newSolution = localChoice(neighbors);

            if (newSolution != null) {
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
