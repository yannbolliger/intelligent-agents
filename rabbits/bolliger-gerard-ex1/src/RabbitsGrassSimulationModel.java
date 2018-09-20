import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Value2DDisplay;

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
    private ArrayList agentList;

    private int gridSize = DEFAULT_GRID_SIZE;
    private int birthThreshold = DEFAULT_BIRTH_THRESHOLD;
    private int numberOfRabbits = DEFAULT_NUMBER_RABBITS;
    private int grassGrowth = DEFAULT_GROWTH_RATE;
    private int grassEnergy = DEFAULT_GRASS_ENERGY;


    public void begin(){
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurface.display();
	}

	public void buildModel() {
        this.space = new RabbitsGrassSimulationSpace(gridSize);
        space.growGrass(grassGrowth);

        for(int i = 0; i < numAgents; i++){
            addNewAgent();
        }
	}

	public void buildSchedule(){
	}

	public void buildDisplay(){
        ColorMap map = new ColorMap();
        map.mapColor(RabbitsGrassSimulationSpace.EMPTY, Color.lightGray);
        map.mapColor(RabbitsGrassSimulationSpace.GRASS, Color.green);

        Value2DDisplay display = new Value2DDisplay(space.getSpace(), map);
        displaySurface.addDisplayable(display, "Grass");
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

    public void setup() {
        this.space = null;

        if (displaySurface != null) displaySurface.dispose();

        final String windowName = getName() + "Window";
        displaySurface = new DisplaySurface(this, windowName);
        registerDisplaySurface(windowName, displaySurface);

        agentList = new ArrayList();
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
        RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent();
        agentList.add(a);
    }
}
