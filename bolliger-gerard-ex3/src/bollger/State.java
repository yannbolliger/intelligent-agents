package bollger;

import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import logist.plan.Action.Move;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.*;
import java.util.Map.Entry;

public class State {

    private final City position;
    private final TaskSet carriedTasks;
    private final TaskSet pendingTasks;


    public State(City position, TaskSet carriedTasks, TaskSet pendingTasks) {
        this.position = position;
        this.carriedTasks = carriedTasks;
        this.pendingTasks = pendingTasks;
    }

    public Map<State, Entry<Action, Double>> nextStates(int capacity) {

        Map<State,  Entry<Action, Double>> nextStates = new HashMap<>();
        final int carriedSum = carriedTasks.weightSum();

        // check whether agent can deliver a task
        for (Task task: carriedTasks) {
            if (task.deliveryCity.equals(position)) {

                TaskSet newCarriedTasks = carriedTasks.clone();
                newCarriedTasks.remove(task);

                State next = new State(position, newCarriedTasks, pendingTasks);
                nextStates.put(next, deliveryWithCost(task));
            }
        }

        // check whether agent can pick up a task
        for (Task task: pendingTasks) {
            if (task.pickupCity.equals(position) &&
                    capacity > task.weight + carriedSum) {

                TaskSet newCarriedTasks = carriedTasks.clone();
                newCarriedTasks.add(task);

                TaskSet newPendingTasks = pendingTasks.clone();
                newPendingTasks.remove(task);

                State next = new State(position, newCarriedTasks,
                        newPendingTasks);

                nextStates.put(next, pickUpWithCost(task));
            }
        }

        // can always move to neighbors
        for (City neighbor : position.neighbors()) {
            State next = new State(neighbor, carriedTasks, pendingTasks);
            
            nextStates.put(next, moveWithCost(neighbor));
        }

        return Collections.unmodifiableMap(nextStates);
    }

    public boolean isGoal() {
        return carriedTasks.isEmpty() && pendingTasks.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return Objects.equals(position, state.position) &&
                Objects.equals(carriedTasks, state.carriedTasks) &&
                Objects.equals(pendingTasks, state.pendingTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, carriedTasks, pendingTasks);
    }

    public City getPosition() {
        return position;
    }

    public TaskSet getCarriedTasks() {
        return carriedTasks;
    }

    public TaskSet getPendingTasks() {
        return pendingTasks;
    }

    private Entry<Action, Double> deliveryWithCost(Task t) {
        double costOfDelivery = 0;

        return new AbstractMap.SimpleImmutableEntry<>(
                new Delivery(t),
                costOfDelivery
        );
    }

    private Entry<Action, Double> pickUpWithCost(Task t) {
        double costOfPickup = 0;

        return new AbstractMap.SimpleImmutableEntry<>(
                new Pickup(t),
                costOfPickup
        );
    }

    private Entry<Action, Double> moveWithCost(City neighbor) {
        double cost = position.distanceTo(neighbor);

        return new AbstractMap.SimpleImmutableEntry<>(
                new Move(neighbor),
                cost
        );
    }
}

