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
            taskAssignments.put(vehicle, new ActionSequence());
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

        // Generate all new solutions from changing its first task with
        // another vehicle's first task
        for (Vehicle other: vehicles) {
            if (other.equals(vehicle)) continue;

            ActionSequence assignmentOther = assignments.get(other);
            if (assignmentOther.isEmpty()) continue;

            solutions.addAll(
                    swapFirstTask(assignment, vehicle, assignmentOther, other)
            );
        }

        // Generate all solutions by reordering the actions in vehicles
        // ActionSequence
        for (ActionSequence newAssignment: assignment.swapTasks()) {
            Map<Vehicle, ActionSequence> newAssignments =
                    new HashMap<>(assignments);

            newAssignments.put(vehicle, newAssignment);
            solutions.add(new Solution(newAssignments, vehicles));
        }

        return null;
    }

    private List<Solution> swapFirstTask(
            ActionSequence assignmentA, Vehicle vehicleA,
            ActionSequence assignmentB, Vehicle vehicleB) {

        Task firstTaskA = assignmentA.getFirstTask();
        Task firstTaskB = assignmentB.getFirstTask();

        List<ActionSequence> insertsA = assignmentA
                .removeTask(firstTaskA)
                .insertFirstTask(firstTaskB);

        List<ActionSequence> insertsB = assignmentB
                .removeTask(firstTaskB)
                .insertFirstTask(firstTaskA);

        List<Solution> solutions = new LinkedList<>();
        ListIterator<ActionSequence> iteratorA = insertsA.listIterator();

        while(iteratorA.hasNext()) {
            List<ActionSequence> insertsBUpwards =
                    insertsB.subList(iteratorA.nextIndex(), insertsB.size());

            for (ActionSequence sB: insertsBUpwards) {

                Map<Vehicle, ActionSequence> newAssignments =
                        new HashMap<>(assignments);

                newAssignments.put(vehicleA, iteratorA.next());
                newAssignments.put(vehicleB, sB);
                solutions.add(new Solution(newAssignments, vehicles));
            }
        }
        return solutions;
    }
}
