package template;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.*;
import java.util.Map.Entry;

public class AStarSearchPlan {

    private final Vehicle vehicle;
    private final TaskSet tasks;

    public AStarSearchPlan(Vehicle vehicle, TaskSet tasks) {
        this.vehicle = vehicle;
        this.tasks = tasks;
    }

    public Plan plan() {
        // calculate initial state
        State initialState = new State(
                vehicle.getCurrentCity(),
                vehicle.getCurrentTasks(),
                tasks
        );

        Node initialNode = new Node(initialState, 0, new ArrayList<>());

        // automatically sorts all the entries by their f() value
        // see compareTo of Node class below
        Queue<Node> agenda = new PriorityQueue<>();
        agenda.add(initialNode);

        // keeps track of visited states and the cost of their path
        Map<State, Double> visitedNodes = new HashMap<>();

        while (!agenda.isEmpty()) {
            Node next = agenda.poll();

            if (next.state.isGoal()) {
                return new Plan(vehicle.getCurrentCity(), next.actions);
            }

            if (!visitedNodes.containsKey(next.state)) {
                visitedNodes.put(next.state, next.cost);

                List<Node> successors = next.successors();
                agenda.addAll(successors);
            }
        }

        // failure
        return Plan.EMPTY;
    }

    private class Node implements Comparable<Node> {
        private final State state;
        private final double cost;
        private final double heuristic;
        private final List<Action> actions;

        public Node(State state, double cost, List<Action> actions) {
            this.state = state;
            this.cost = cost;
            this.actions = actions;
            this.heuristic = calculateHeuristic();
        }

        private double calculateHeuristic() {
            double maxSingleCost = 0;
            Topology.City pos = state.getPosition();

            for (Task task: state.getCarriedTasks()) {
                double cost = pos.distanceTo(task.deliveryCity);

                if (cost > maxSingleCost) {
                    maxSingleCost = cost;
                }
            }

            for (Task task : state.getPendingTasks()) {
                double cost =
                        pos.distanceTo(task.pickupCity) + task.pathLength();

                if (cost > maxSingleCost) {
                    maxSingleCost = cost;
                }
            }
            return maxSingleCost;
        }

        public double f() {
            return cost + heuristic;
        }

        @Override
        public int compareTo(Node o) {
            return new Double(this.f()).compareTo(o.f());
        }

        public List<Node> successors() {
            Map<State, Entry<Action, Double>> nextStates =
                    state.nextStates(vehicle.capacity());

            List<Node> successors = new ArrayList<>();

            for (Entry<State, Entry<Action, Double>> entry :
                    nextStates.entrySet())
            {
                State  nextState    = entry.getKey();
                Action actionTaken  = entry.getValue().getKey();
                double costOfAction = entry.getValue().getValue();

                double newCost = cost + costOfAction;

                List<Action> newActions = new ArrayList<>(actions);
                newActions.add(actionTaken);

                Node node = new Node(nextState, newCost, newActions);
                successors.add(node);
            }

            return successors;
        }
    }
}
