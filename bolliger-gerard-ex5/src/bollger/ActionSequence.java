package bollger;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.*;

class ActionSequence {
    private final static double DISTANCE_NOT_CACHED = -1;

    private final LinkedList<TaskAction> actions;
    private final Vehicle vehicle;
    private double distance = DISTANCE_NOT_CACHED;

    public ActionSequence(Vehicle vehicle) {
        this(vehicle, new LinkedList<>());
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
        PickupAction pickup = new PickupAction(task);
        newSeq.actions.add(pickup);
        newSeq.actions.add(delivery);

        return newSeq;
    }

    public double getDistance() {
        if (distance == DISTANCE_NOT_CACHED) {
            distance = 0;
            Topology.City current = vehicle.getCurrentCity();

            for (TaskAction taskAction : actions) {
                Topology.City next = taskAction.isPickup() ?
                        taskAction.getTask().pickupCity :
                        taskAction.getTask().deliveryCity;

                if (!current.equals(next)) {
                    distance += current.distanceTo(next);
                    current = next;
                }
            }
        }
        return distance;
    }

    double estimatedMaxGain(Map<Integer, Double> expectedLoadOnEdge) {
        double estimatedMaxGain = 0;
        City currentCity = vehicle.getCurrentCity();

        for (TaskAction action: actions) {
            Topology.City next = action.isPickup() ?
                    action.getTask().pickupCity :
                    action.getTask().deliveryCity;

            if (!currentCity.equals(next)) {
                for (City city : currentCity.pathTo(next)) {

                    int edgeHash = currentCity.hashCode() + city.hashCode();

                    estimatedMaxGain += currentCity.distanceTo(city)
                            * vehicle.costPerKm()
                            * expectedLoadOnEdge.get(edgeHash);

                    currentCity = city;
                }
            }
        }

        return estimatedMaxGain;
    }

    public Plan getPlan(Vehicle vehicle) {
        City current = vehicle.getCurrentCity();
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

    public Task getRandomTask() {
        return actions.get(new Random().nextInt(actions.size())).getTask();
    }

    public ActionSequence removeTask(Task task) {
        ActionSequence newSeq = copy();

        newSeq.actions.removeIf(action -> action.getTask().equals(task));
        return newSeq;
    }

    public ActionSequence insertAt(TaskAction action, int position) {
        ActionSequence newSeq = copy();

        newSeq.actions.add(position, action);
        return newSeq;
    }

    public boolean checkCapacityIsRespected() {
        int vehicleLoad = 0;
        for (TaskAction action : actions) {
            if (action.isPickup()){
                vehicleLoad += action.getTask().weight;
            }
            else {
                vehicleLoad -= action.getTask().weight;
            }

            if (vehicleLoad > vehicle.capacity()) {
                return false;
            }
        }
        return true;
    }


    public List<ActionSequence> reorderTasks() {
        List<ActionSequence> possibleCombinations = new LinkedList<>();

        for (TaskAction action : actions) {
            if (action.isPickup()) {

                ActionSequence seqWithoutTask = removeTask(action.getTask());
                DeliveryAction delivery = new DeliveryAction(action.getTask());

                for (int posPickup = 0;
                     posPickup <= seqWithoutTask.actions.size(); posPickup++) {

                    ActionSequence seqWithOnlyPickup =
                            seqWithoutTask.insertAt(action, posPickup);

                    for (int posDelivery = posPickup + 1;
                         posDelivery <= seqWithOnlyPickup.actions.size();
                         posDelivery++) {

                        ActionSequence reorderedSeq =
                                seqWithOnlyPickup.insertAt(delivery, posDelivery);

                        if (reorderedSeq.checkCapacityIsRespected()) {
                            possibleCombinations.add(reorderedSeq);
                        }
                    }
                }
            }
        }
        return possibleCombinations;
    }

    private ActionSequence copy() {
        return new ActionSequence(vehicle, actions);
    }

    @Override
    /**
     * Returns true if the other object is also a ActionSequence and represents
     * essentially the same sequence. This is compared by the total distance
     * that the vehicle has to drive for this sequence. In that manner we
     * avoid to consider two sequences where only two pickups in the same city
     * change order but yield the same cost as two different sequences.
     *
     * This is important in order to avoid being stuck in local minima with
     * the local stochastic search.
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActionSequence)) return false;
        ActionSequence that = (ActionSequence) o;

        // compare distance and vehicle (not actual sequence)
        return Objects.equals(getDistance(), that.getDistance()) &&
                Objects.equals(vehicle, that.vehicle);
    }

    @Override
    public int hashCode() {
        // compare distance and vehicle (not actual sequence)
        return Objects.hash(getDistance(), vehicle);
    }
}
