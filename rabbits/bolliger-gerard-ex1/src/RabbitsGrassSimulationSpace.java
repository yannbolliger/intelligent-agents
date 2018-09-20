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

    public RabbitsGrassSimulationSpace(int gridSize) {
        this.grassSpace = new Object2DGrid(gridSize, gridSize);
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

    public Object2DGrid getGrassSpace() {
        return grassSpace;
    }
    public Object2DGrid getAgentSpace() {
        return agentSpace;
    }

    public boolean isCellOccupied(int x, int y){
        return (agentSpace.getObjectAt(x, y)!=null);
    }

    public boolean addAgent(RabbitsGrassSimulationAgent agent){
        boolean retVal = false;
        int count = 0;
        int countLimit = 10 * agentSpace.getSizeX() * agentSpace.getSizeY();

        while((retVal==false) && (count < countLimit)){
            int x = (int)(Math.random()*(agentSpace.getSizeX()));
            int y = (int)(Math.random()*(agentSpace.getSizeY()));
            if(isCellOccupied(x,y) == false){
                agentSpace.putObjectAt(x,y,agent);
                agent.setXY(x,y);
                retVal = true;
            }
            count++;
        }

        return retVal;
    }
}
