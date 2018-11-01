package template;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.*;

public class Solution {


    private final Map<Vehicle, ActionSequence> assignments;
    private final List<Vehicle> vehicles;

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

            taskAssignments.put(
                    bestVehicle,
                    taskAssignments.get(bestVehicle).append(task)
            );
        }
        return new Solution(taskAssignments, vehicles);
    }

    public double getCost() {
        double cost = 0;

        for (Vehicle vehicle: vehicles) {
            Plan plan = assignments.get(vehicle).getPlan(vehicle);
            cost += vehicle.costPerKm() * plan.totalDistance();
        }
        return cost;
    }

    public List<Plan> getPlans() {
        List<Plan> plans = new ArrayList<>();

        for (Vehicle vehicle: vehicles) {
            plans.add(assignments.get(vehicle).getPlan(vehicle));
        }
        return plans;
    }

    public List<Solution> localNeighbors() {
        List<Solution> solutions = new ArrayList<>();

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

        return null;
    }

    private List<Solution> reassignFirstTask(
            ActionSequence assignmentA, ActionSequence assignmentB) {

        Task firstTaskA = assignmentA.getFirstTask();
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
}
