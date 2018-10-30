package template;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.*;

public class Solution {


    private final Map<Vehicle, LinkedList<TaskAction>> taskAssignment;
    private final List<Vehicle> vehicles;

    private Solution(Map<Vehicle, LinkedList<TaskAction>> taskAssignment,
                     List<Vehicle> vehicles) {

        this.taskAssignment = taskAssignment;
        this.vehicles = vehicles;
    }

    public static Solution initial(List<Vehicle> vehicles, TaskSet tasks) {
        Map<Vehicle, LinkedList<TaskAction>> taskAssignments = new HashMap();
        for (Vehicle vehicle: vehicles) {
            taskAssignments.put(vehicle, new LinkedList<>());
        }

        for (Task task : tasks){
            double minCost = Double.POSITIVE_INFINITY;
            Vehicle bestVehicle = null;

            for (Vehicle vehicle: vehicles) {
                LinkedList<TaskAction> existingAssignment =
                        taskAssignments.get(vehicle);

                City vehicleNextPosition = vehicle.getCurrentCity();
                if (!existingAssignment.isEmpty()){
                    vehicleNextPosition =
                            existingAssignment.getLast().getTask().deliveryCity;
                }

                double costToPickup = vehicle.costPerKm()
                        * vehicleNextPosition.distanceTo(task.pickupCity);

                // TODO: vehicle can take more than one task !
                if (costToPickup < minCost && task.weight < vehicle.capacity()) {
                    minCost = costToPickup;
                    bestVehicle = vehicle;
                }
            }

            List<TaskAction> existingVehicleAssignment = taskAssignments.get(bestVehicle);
            existingVehicleAssignment.add(new TaskAction(task, true));
            existingVehicleAssignment.add(new TaskAction(task, false));

        }
        return new Solution(taskAssignments, vehicles);
    }

    public double getCost() {
        double cost = 0;

        for (Vehicle vehicle: vehicles) {
            Plan plan = getPlan(vehicle, taskAssignment.get(vehicle));
            cost += vehicle.costPerKm() * plan.totalDistance();
        }
        return cost;
    }

    public List<Plan> getPlans() {
        List<Plan> plans = new ArrayList<>();

        for (Vehicle vehicle: vehicles) {
            plans.add(getPlan(vehicle, taskAssignment.get(vehicle)));
        }
        return plans;
    }

    private Plan getPlan(Vehicle vehicle, List<TaskAction> assignment) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (TaskAction taskAction: assignment) {
            City next = taskAction.isPickup() ?
                    taskAction.getTask().pickupCity :
                    taskAction.getTask().deliveryCity;

            if (!current.equals(next)) {
                for (City city : current.pathTo(next)) {
                    plan.appendMove(city);
                }
                current = next;
            }

            if (taskAction.isPickup()){
                plan.appendPickup(taskAction.getTask());
            }
            else {
                plan.appendDelivery(taskAction.getTask());
            }

        }
        return plan;
    }

    public List<Solution> localNeighbors() {
        // TODO: Yann
        return null;
    }


}
