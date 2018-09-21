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

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		

    public static final int DEFAULT_GRID_SIZE = 20;
    public static final int DEFAULT_NUMBER_RABBITS = 150;
    public static final int DEFAULT_BIRTH_THRESHOLD = 15;
    public static final int DEFAULT_GROWTH_RATE = 15;
    public static final int DEFAULT_GRASS_ENERGY = 5;

    public static final int STEP_COST = 1;

    private Schedule schedule;
    private DisplaySurface displaySurface;
    private RabbitsGrassSimulationSpace space;
    private ArrayList<RabbitsGrassSimulationAgent> agentList;

    private int gridSize = DEFAULT_GRID_SIZE;
    private int birthThreshold = DEFAULT_BIRTH_THRESHOLD;
    private int numberOfRabbits = DEFAULT_NUMBER_RABBITS;
    private int grassGrowth = DEFAULT_GROWTH_RATE;
    private int grassEnergy = DEFAULT_GRASS_ENERGY;


    public void setup() {
        space = null;

        if (displaySurface != null) displaySurface.dispose();

        final String windowName = getName() + "Window";
        displaySurface = new DisplaySurface(this, windowName);
        registerDisplaySurface(windowName, displaySurface);

        agentList = new ArrayList();

        schedule = new Schedule(1);
    }

    public void begin(){
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurface.display();
	}

	public void buildModel() {
        this.space = new RabbitsGrassSimulationSpace(gridSize, getGrassEnergy());
        space.growGrass(grassGrowth);

        for (int i = 0; i < numberOfRabbits; i++){
            addNewAgent();
        }
	}

	public void buildSchedule(){

        schedule.scheduleActionBeginning(0, new BasicAction() {

            @Override
            public void execute() {
                SimUtilities.shuffle(agentList);
                for (RabbitsGrassSimulationAgent agent: agentList) agent.step();

                space.growGrass(getGrassGrowthRate());

                displaySurface.updateDisplay();
            }
        });
	}

	public void buildDisplay(){
        ColorMap map = new ColorMap();
        map.mapColor(RabbitsGrassSimulationSpace.EMPTY, Color.gray);
        map.mapColor(RabbitsGrassSimulationSpace.GRASS, Color.green);

        Value2DDisplay grassDisplay =
                new Value2DDisplay(space.getGrassSpace(), map);
        displaySurface.addDisplayable(grassDisplay, "Grass");

        Object2DDisplay displayAgents =
                new Object2DDisplay(space.getAgentSpace());
        displayAgents.setObjectList(agentList);
        displaySurface.addDisplayable(displayAgents, "Agents");
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

    private void addNewAgent(){
        RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(space);
        agentList.add(a);
        space.addAgent(a);
    }
}
