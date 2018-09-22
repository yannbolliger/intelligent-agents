import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {

    public static final int EMPTY = 0;
    public static final int GRASS = 1;

    private Object2DGrid grassSpace;
    private Object2DGrid agentSpace;

    private int grassEnergy;

    public RabbitsGrassSimulationSpace(int gridSize, int grassEnergy) {
        this.grassSpace = new Object2DGrid(gridSize, gridSize);
        this.grassEnergy = grassEnergy;
        agentSpace = new Object2DGrid(gridSize, gridSize);

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                this.grassSpace.putObjectAt(i, j, EMPTY);
            }
        }
    }

    public void growGrass(int growthRate) {
        final int surface = grassSpace.getSizeX() * grassSpace.getSizeY();
        final int grassAmount = surface * growthRate/100;

        for (int i = 0; i < grassAmount; i++) {
            int x = (int) (Math.random() * grassSpace.getSizeX());
            int y = (int) (Math.random() * grassSpace.getSizeY());

            grassSpace.putObjectAt(x, y, GRASS);
        }
    }

    public int takeGrassEnergyAt(int x, int y){
        if ((int) grassSpace.getObjectAt(x,y) == GRASS) {
            this.grassSpace.putObjectAt(x, y, EMPTY);
            return grassEnergy;
        }

        return 0;
    }

    public Object2DGrid getGrassSpace() {
        return grassSpace;
    }
    public Object2DGrid getAgentSpace() { return agentSpace; }

    public boolean isCellOccupied(int x, int y){
        return agentSpace.getObjectAt(x, y) != null;
    }

    public boolean addAgent(RabbitsGrassSimulationAgent agent) {
        int countLimit = 10 * agentSpace.getSizeX() * agentSpace.getSizeY();

        for (int count = 0; count < countLimit; count++) {
            int x = (int)(Math.random()*(agentSpace.getSizeX()));
            int y = (int)(Math.random()*(agentSpace.getSizeY()));

            if (!isCellOccupied(x,y)){
                putAgentAt(agent, x, y);
                return true;
            }
        }
        return false;
    }

    public boolean moveAgentAt(int x, int y, int newX, int newY){

        newX = Math.floorMod(newX, agentSpace.getSizeX());
        newY = Math.floorMod(newY, agentSpace.getSizeY());


        if (!isCellOccupied(newX, newY)){
            RabbitsGrassSimulationAgent agent =
                    (RabbitsGrassSimulationAgent) agentSpace.getObjectAt(x, y);
            removeAgentAt(x, y);

            putAgentAt(agent, newX, newY);
            return true;
        }
        return false;
    }

    public void removeAgentAt(int x, int y){
        agentSpace.putObjectAt(x, y, null);
    }

    private void putAgentAt(RabbitsGrassSimulationAgent agent, int x, int y) {
        agent.setXY(x, y);
        agentSpace.putObjectAt(x, y, agent);
    }
}
