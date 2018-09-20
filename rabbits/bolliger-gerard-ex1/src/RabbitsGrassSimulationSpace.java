import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {

    public static final int EMPTY = 0;
    public static final int GRASS = 1;

    private Object2DGrid space;

    public RabbitsGrassSimulationSpace(int gridSize) {
        this.space = new Object2DGrid(gridSize, gridSize);

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                this.space.putObjectAt(i, j, EMPTY);
            }
        }
    }

    public void growGrass(int growthRate) {
        final int surface = space.getSizeX() * space.getSizeY();
        final int grassAmount = surface * growthRate/100;

        for (int i = 0; i < grassAmount; i++) {
            int x = (int) (Math.random() * space.getSizeX());
            int y = (int) (Math.random() * space.getSizeY());

            space.putObjectAt(x, y, GRASS);
        }
    }

    public Object2DGrid getSpace() {
        return space;
    }
}
