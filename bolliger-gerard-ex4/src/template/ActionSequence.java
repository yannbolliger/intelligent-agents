package template;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

import java.util.*;

public class ActionSequence {

    private final LinkedList<TaskAction> actions;
    private final Vehicle vehicle;

    public ActionSequence(Vehicle vehicle) {
        this(vehicle, Collections.EMPTY_LIST);
    }

    public ActionSequence(Vehicle vehicle, List<TaskAction> actions) {
        this.vehicle = vehicle;
        this.actions = new LinkedList<>(actions);
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }

    public Topology.City getEndPosition() {
        return actions.getLast().getTask().deliveryCity;
    }

    public ActionSequence append(Task task) {
        ActionSequence newSeq = copy();

        DeliveryAction delivery = new DeliveryAction(task);
        PickupAction pickup = new PickupAction(task, delivery);
        newSeq.actions.add(pickup);
        newSeq.actions.add(delivery);

        return newSeq;
    }

    public Plan getPlan(Vehicle vehicle) {
        Topology.City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (TaskAction taskAction: actions) {
            Topology.City next = taskAction.isPickup() ?
                    taskAction.getTask().pickupCity :
                    taskAction.getTask().deliveryCity;

            if (!current.equals(next)) {
                for (Topology.City city : current.pathTo(next)) {
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

    public Task getFirstTask() {
        return actions.getFirst().getTask();
    }

    public ActionSequence removeTask(Task task) {
        ActionSequence newSeq = copy();

        newSeq.actions.removeIf(action -> action.getTask().equals(task));
        return newSeq;
    }

    public List<ActionSequence> reorderTasks() {
        return null;
    }

    private ActionSequence copy() {
        return new ActionSequence(vehicle, actions);
    }
}
