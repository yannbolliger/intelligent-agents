import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author Kyle Gerard, Yann Bolliger
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		

    public static final int DEFAULT_GRID_SIZE = 20;
    public static final int DEFAULT_NUMBER_RABBITS = 100;
    public static final int DEFAULT_BIRTH_THRESHOLD = 15;
    public static final int DEFAULT_BIRTH_ENERGY = 5;
    public static final int DEFAULT_GROWTH_RATE = 15;
    public static final int DEFAULT_GRASS_ENERGY = 5;

    private Schedule schedule;
    private DisplaySurface displaySurface;
    private RabbitsGrassSimulationSpace space;
    private ArrayList<RabbitsGrassSimulationAgent> agentList;
    private OpenSequenceGraph populationGraph;

    private int gridSize = DEFAULT_GRID_SIZE;
    private int birthThreshold = DEFAULT_BIRTH_THRESHOLD;
    private int numberOfRabbits = DEFAULT_NUMBER_RABBITS;
    private int grassGrowth = DEFAULT_GROWTH_RATE;
    private int grassEnergy = DEFAULT_GRASS_ENERGY;


    public void setup() {
        space = null;

        setupDisplaySurface();
        setupPopulationGraph();

        agentList = new ArrayList();
        schedule = new Schedule(1);
    }

    public void begin(){
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurface.display();
        populationGraph.display();
	}

	public void buildModel() {
        space = new RabbitsGrassSimulationSpace(gridSize, getGrassEnergy());
        space.growGrass(grassGrowth);

        for (int i = 0; i < numberOfRabbits; i++) {
            addNewAgent(DEFAULT_BIRTH_ENERGY);
        }
	}

	public void buildSchedule(){

        schedule.scheduleActionBeginning(0, new BasicAction() {

            @Override
            public void execute() {

                SimUtilities.shuffle(agentList);
                Iterator<RabbitsGrassSimulationAgent> iterator = agentList.iterator();

                List<Integer> newAgentEnergies = new ArrayList();
                while (iterator.hasNext()) {
                    RabbitsGrassSimulationAgent agent = iterator.next();
                    agent.step();

                    if (!agent.isAlive()) {
                        iterator.remove();
                        space.removeAgentAt(agent.getX(), agent.getY());
                    }
                    else if (agent.canReproduce()) {
                        newAgentEnergies.add(agent.reproduce());
                    }
                }
                for (int i : newAgentEnergies) addNewAgent(i);

                space.growGrass(getGrassGrowthRate());

                displaySurface.updateDisplay();
            }
        });

        schedule.scheduleActionAtInterval(5, new BasicAction() {
            @Override
            public void execute() {
                populationGraph.step();
            }
        });

	}

	public void buildDisplay(){
        ColorMap map = new ColorMap();
        map.mapColor(RabbitsGrassSimulationSpace.EMPTY, Color.gray);
        map.mapColor(RabbitsGrassSimulationSpace.GRASS, Color.green);

        Value2DDisplay grassDisplay =
                new Value2DDisplay(space.getGrassSpace(), map);
        displaySurface.addDisplayableProbeable(grassDisplay, "Grass");

        Object2DDisplay displayAgents =
                new Object2DDisplay(space.getAgentSpace());
        displayAgents.setObjectList(agentList);
        displaySurface.addDisplayableProbeable(displayAgents, "Agents");

        populationGraph.addSequence("Rabbit population", new RabbitPopulation());
        populationGraph.addSequence("Grass amount", new GrassPopulation());
	}

    private void addNewAgent(int energyAtBirth){
        RabbitsGrassSimulationAgent a =
                new RabbitsGrassSimulationAgent(space, birthThreshold, energyAtBirth);

        // add agent to the list only if it could be placed on the space
        if (space.addAgent(a)) agentList.add(a);
    }

    private void setupDisplaySurface() {
        if (displaySurface != null) displaySurface.dispose();

        final String windowName = getName() + " Window";
        displaySurface = new DisplaySurface(this, windowName);
        registerDisplaySurface(windowName, displaySurface);
    }

    private void setupPopulationGraph() {
        if (populationGraph != null) populationGraph.dispose();

        populationGraph = new OpenSequenceGraph("Rabbit Population Count", this);
        this.registerMediaProducer("Plot", populationGraph);
    }

    public String[] getInitParam() {
        String[] params = {
                "GridSize",
                "BirthThreshold",
                "GrassGrowthRate",
                "Number",
                "GrassEnergy"
        };

        return params;
    }

    public String getName() {
        return "GerBol Rabbit Simulation";
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public int getBirthThreshold() {
        return birthThreshold;
    }

    public void setBirthThreshold(int birthThreshold) {
        this.birthThreshold = birthThreshold;
    }

    public int getNumber() {
        return numberOfRabbits;
    }

    public void setNumber(int numberOfRabbits) {
        this.numberOfRabbits = numberOfRabbits;
    }

    public int getGrassGrowthRate() {
        return grassGrowth;
    }

    public void setGrassGrowthRate(int grassGrowth) {
        this.grassGrowth = grassGrowth;
    }

    public int getGrassEnergy() {
        return grassEnergy;
    }

    public void setGrassEnergy(int grassEnergy) {
        this.grassEnergy = grassEnergy;
    }

    public static void main(String[] args) {
        SimInit init = new SimInit();
        RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
        init.loadModel(model, "", false);
    }

    private class RabbitPopulation extends Population {

        @Override
        public double getSValue(){
            return agentList.stream().filter(a -> a.isAlive()).count();
        }
    }

    private class GrassPopulation extends Population {

        @Override
        public double getSValue() {
            return space.countGrass();
        }
    }
}
