package bollger;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.*;

public class Solution {
    private final static double COST_NOT_CACHED = -1;

    private final Map<Vehicle, ActionSequence> assignments;
    private final List<Vehicle> vehicles;
    private double cost = COST_NOT_CACHED;

    private Solution(Map<Vehicle, ActionSequence> taskAssignment,
                     List<Vehicle> vehicles) {

        this.assignments = taskAssignment;
        this.vehicles = vehicles;
    }

    public static Solution initial(List<Vehicle> vehicles, TaskSet tasks) {
        Map<Vehicle, ActionSequence> taskAssignments = new HashMap();
        for (Vehicle vehicle: vehicles) {
            taskAssignments.put(vehicle, new ActionSequence(vehicle));
        }

        for (Task task : tasks){

            Vehicle bestVehicle = findBestVehicle(task, vehicles, taskAssignments);

            taskAssignments.put(
                    bestVehicle,
                    taskAssignments.get(bestVehicle).append(task)
            );
        }
        return new Solution(taskAssignments, vehicles);
    }

    private static Vehicle findBestVehicle(Task task, List<Vehicle> vehicles, Map<Vehicle, ActionSequence> taskAssignments) {
        double minCost = Double.POSITIVE_INFINITY;
        Vehicle bestVehicle = null;
        for (Vehicle vehicle: vehicles) {
            ActionSequence actionSequence = taskAssignments.get(vehicle);

            City vehicleNextPosition = vehicle.getCurrentCity();
            if (!actionSequence.isEmpty()){
                vehicleNextPosition = actionSequence.getEndPosition();
            }

            double costToPickup = vehicle.costPerKm()
                    * vehicleNextPosition.distanceTo(task.pickupCity);

            if (costToPickup < minCost && task.weight < vehicle.capacity()) {
                minCost = costToPickup;
                bestVehicle = vehicle;
            }
        }
        return bestVehicle;
    }

    private Solution copy () {
        Solution copy = new Solution(assignments, vehicles);
        return copy;
    }

    public Solution addTask(Task task) {
        Solution copy = copy();

        Map<Vehicle, ActionSequence> taskAssignments = copy.assignments;

        Vehicle bestVehicle = findBestVehicle(task, vehicles, assignments);

        assignments.put(
                bestVehicle,
                assignments.get(bestVehicle).append(task)
        );

        return copy;
    }

    public double getCost() {
        if (cost == COST_NOT_CACHED) {
            cost = 0;

            for (Vehicle vehicle : vehicles) {
                cost += vehicle.costPerKm() * assignments.get(vehicle).getDistance();
            }
        }
        return this.cost;
    }

    public List<Plan> getPlans() {
        List<Plan> plans = new ArrayList<>();

        for (Vehicle vehicle: vehicles) {
            plans.add(assignments.get(vehicle).getPlan(vehicle));
        }
        return plans;
    }

    public List<Solution> localNeighbors() {
        List<Solution> solutions = new LinkedList<>();

        // Choose random vehicle with at least one task
        Vehicle vehicle = null;
        do {
            vehicle = vehicles.get(new Random().nextInt(vehicles.size()));
        } while (assignments.get(vehicle).isEmpty());

        ActionSequence assignment = assignments.get(vehicle);

        // Generate all new solutions from giving the first task to another
        // vehicle
        for (Vehicle other: vehicles) {
            if (other.equals(vehicle)) continue;

            solutions.addAll(
                    reassignFirstTask(assignment, assignments.get(other))
            );
        }

        // Generate all solutions by reordering the actions in this vehicle's
        // ActionSequence
        for (ActionSequence newAssignment: assignment.reorderTasks()) {
            Map<Vehicle, ActionSequence> newAssignments =
                    new HashMap<>(assignments);

            newAssignments.put(vehicle, newAssignment);
            solutions.add(new Solution(newAssignments, vehicles));
        }
        return solutions;
    }

    private List<Solution> reassignFirstTask(
            ActionSequence assignmentA, ActionSequence assignmentB) {

        Task firstTaskA = assignmentA.getRandomTask();
        if (firstTaskA.weight > assignmentB.getVehicle().capacity()) {
            return Collections.EMPTY_LIST;
        }

        ActionSequence removedTaskA = assignmentA.removeTask(firstTaskA);
        ActionSequence insertsInB = assignmentB.append(firstTaskA);

        List<Solution> solutions = new LinkedList<>();
        for (ActionSequence insertInB: insertsInB.reorderTasks()) {

            Map<Vehicle, ActionSequence> newAssignments =
                    new HashMap<>(assignments);

            newAssignments.put(removedTaskA.getVehicle(), removedTaskA);
            newAssignments.put(insertInB.getVehicle(), insertInB);
            solutions.add(new Solution(newAssignments, vehicles));
        }
        return solutions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Solution)) return false;
        Solution solution = (Solution) o;
        return Objects.equals(assignments, solution.assignments) &&
                Objects.equals(vehicles, solution.vehicles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignments, vehicles);
    }
}
