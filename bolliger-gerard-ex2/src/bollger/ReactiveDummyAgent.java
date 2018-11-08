package bollger;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

import java.util.Random;

public class ReactiveDummyAgent implements ReactiveBehavior {

    private Random random;
    private double pPickup;
    private int numActions;
    private Agent myAgent;

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {

        // Reads the discount factor from the agents.xml file.
        // If the property is not present it defaults to 0.95
        Double discount = agent.readProperty("discount-factor", Double.class,
                0.95);

        this.random = new Random();
        this.pPickup = discount;
        this.numActions = 0;
        this.myAgent = agent;
    }

    @Override
    public Action act(Vehicle vehicle, Task availableTask) {
        Action action;

        if (availableTask == null) {
            Topology.City closestCity = null;
            double minDistance = Double.MAX_VALUE;
            Topology.City from = vehicle.getCurrentCity();
            for (Topology.City to : from.neighbors()) {

                if (from.distanceTo(to) < minDistance){
                    minDistance = from.distanceTo(to);
                    closestCity = to;
                }
            }
            action = new Action.Move(closestCity);
        } else {
            action = new Action.Pickup(availableTask);
        }

        if (numActions >= 1) {
            System.out.println("The total profit after " + numActions +
                    " actions is " + myAgent.getTotalProfit() +
                    " (average profit: " +
                    (myAgent.getTotalProfit() / (double)numActions)+")"
            );
        }
        numActions++;

        return action;
    }
}
