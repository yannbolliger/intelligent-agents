import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;

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
    private int gridSize = DEFAULT_GRID_SIZE;
    private int birthThreshold = DEFAULT_BIRTH_THRESHOLD;
    private int numberOfRabbits = DEFAULT_NUMBER_RABBITS;
    private int grassGrowth = DEFAULT_GROWTH_RATE;
    private int grassEnergy = DEFAULT_GRASS_ENERGY;

    public void begin(){
		buildModel();
		buildSchedule();
		buildDisplay();
	}

	public void buildModel(){
	}

	public void buildSchedule(){
	}

	public void buildDisplay(){
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
        // TODO Auto-generated method stub

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
}
