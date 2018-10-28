package template;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.*;

public class Solution {


    private final Map<Vehicle, List<TaskAction>> taskAssignment;
    private final List<Vehicle> vehicles;

    private Solution(Map<Vehicle,List<TaskAction>> taskAssignment,
                     List<Vehicle> vehicles) {

        this.taskAssignment = taskAssignment;
        this.vehicles = vehicles;
    }

    public static Solution initial(List<Vehicle> vehicles, TaskSet tasks) {
        Map<Vehicle, List<TaskAction>> taskAssignments = new HashMap();
        for(Vehicle vehicle: vehicles) {
            taskAssignments.put(vehicle, new LinkedList<>());
        }

        for(Task task : tasks){
            double min_cost = Double.POSITIVE_INFINITY;
            Vehicle best_vehicle = null;
            for(Vehicle vehicle: vehicles) {
                List<TaskAction> existingAssignement =  taskAssignments.get(vehicle);
                City vehicleNextPosition = vehicle.getCurrentCity();
                if (!existingAssignement.isEmpty()){
                    vehicleNextPosition = existingAssignement.get(existingAssignement.size() - 1).task.deliveryCity;
                }
                double cost_to_pickup = vehicle.costPerKm() * vehicleNextPosition.distanceTo(task.pickupCity);

                if(cost_to_pickup < min_cost && task.weight < vehicle.capacity()) {
                    min_cost = cost_to_pickup;
                    best_vehicle = vehicle;
                }
            }

            List<TaskAction> existingVehicleAssignment = taskAssignments.get(best_vehicle);
            existingVehicleAssignment.add(new TaskAction(task, true));
            existingVehicleAssignment.add(new TaskAction(task, false));

        }
        return new Solution(taskAssignments, vehicles);
    }

    public double getCost() {
        // TODO: Yann
        return 0;
    }

    public List<Plan> getPlans() {
        List<Plan> plans = new ArrayList<>();
        for(Vehicle vehicle: vehicles) {
            List<TaskAction> assignment = taskAssignment.get(vehicle);
            City current = vehicle.getCurrentCity();
            Plan plan = new Plan(current);
            for(TaskAction taskAction: assignment) {
                City next = taskAction.isPickup ? taskAction.task.pickupCity : taskAction.task.deliveryCity;

                if (current != next) {
                    for (City city :  current.pathTo(next)) {
                        plan.appendMove(city);
                    }
                    current = next;
                }

                if (taskAction.isPickup){
                    plan.appendPickup(taskAction.getTask());
                }
                else {
                    plan.appendDelivery(taskAction.getTask());
                }

            }
            plans.add(plan);

        }
        return plans;
    }

    public List<Solution> localNeighbors() {
        // TODO: Yann
        return null;
    }


}
