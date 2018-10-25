package template;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import java.util.List;

public class Solution {


    private final List<List<TaskAction>> taskAssignment;
    private final List<Vehicle> vehicles;

    private Solution(List<List<TaskAction>> taskAssignment,
                     List<Vehicle> vehicles) {

        this.taskAssignment = taskAssignment;
        this.vehicles = vehicles;
    }

    public static Solution initial(List<Vehicle> vehicles, TaskSet tasks) {
        // TODO: Kyle
        return null;
    }

    public double getCost() {
        // TODO: Yann
        return 0;
    }

    public List<Plan> getPlans() {
        // TODO: Kyle
        return null;
    }

    public List<Solution> localNeighbors() {
        // TODO: Yann
        return null;
    }

    private class TaskAction {
        Task task;
        boolean isPickup;

        public TaskAction(Task task, boolean isPickup) {
            this.task = task;
            this.isPickup = isPickup;
        }

        public Task getTask() {
            return task;
        }

        public boolean isPickup() {
            return isPickup;
        }

        public boolean isDelivery() {
            return !isPickup;
        }
    }
}
