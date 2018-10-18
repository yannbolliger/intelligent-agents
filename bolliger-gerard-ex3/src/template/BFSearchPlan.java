package template;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;

import java.util.*;

public class BFSearchPlan {

    private final Vehicle vehicle;
    private final TaskSet tasks;

    public BFSearchPlan(Vehicle vehicle, TaskSet tasks) {
        this.vehicle = vehicle;
        this.tasks = tasks;
    }

    public Plan plan() {

        LinkedList<StatePlanPair> q = new LinkedList();
        Map<State, Double> cyleSet = new HashMap<>();
        List<Plan> goalPlanSet = new ArrayList<>();

        State startState = new State(
                vehicle.getCurrentCity(), vehicle.getCurrentTasks(), tasks);

        q.add(new StatePlanPair(startState, new LinkedList<>()));
        cyleSet.put(startState, 0d);

        while (!q.isEmpty()) {
            StatePlanPair statePlanPair = q.poll();

            if (statePlanPair.getState().isGoal()) {
                goalPlanSet.add(
                        new Plan(vehicle.getCurrentCity(),
                                statePlanPair.getActions())
                );
            }

            else {
                Map<State, Map.Entry<Action, Double>> children =
                        statePlanPair.getState().nextStates(vehicle.capacity());

                for (Map.Entry<State, Map.Entry<Action, Double>> child :
                        children.entrySet()) {

                    LinkedList<Action> childPlan = statePlanPair.getActions();
                    childPlan.add(child.getValue().getKey());
                    State childState = child.getKey();

                    boolean visited = cyleSet.containsKey(childState);
                    boolean visitedWithHigherCost =
                            cyleSet.get(childState) > new PlchildPlan


                    if (!visited || visitedWithHigherCost) {
                        cyleSet.add(child.getKey());
                        q.addLast(new StatePlanPair(child.getKey(), childPlan));
                    }


                }
            }
        }

        double minDistance = Double.POSITIVE_INFINITY;
        Plan bestPlan = null;
        for (Plan p : goalPlanSet) {
            double planDistance = p.totalDistance();
            if (planDistance < minDistance) {
                minDistance = planDistance;
                bestPlan = p;
            }
        }
        return bestPlan;
    }

    private class StatePlanPair {
        private State state;
        private LinkedList<Action> plan;

        public StatePlanPair(State state, LinkedList<Action> plan) {
            this.state = state;
            this.plan = plan;
        }

        public State getState() {
            return state;
        }

        public  LinkedList<Action> getActions() {
            return (LinkedList) plan.clone();
        }
    }
}
