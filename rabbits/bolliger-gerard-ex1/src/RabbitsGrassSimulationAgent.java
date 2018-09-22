import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	private int x;
	private int y;
	private int energy;
	private final RabbitsGrassSimulationSpace space;
	private int birthThreshold;

	public static final int DEFAULT_BIRTH_ENERGY = 5;

	public enum LegalMoves { UP, DOWN, RIGHT, LEFT }

    public static final List<LegalMoves> MOVES_LIST =
            Arrays.asList(LegalMoves.values());

    public RabbitsGrassSimulationAgent(
            RabbitsGrassSimulationSpace space,
            int birthThreshold){
		this.x = -1;
		this.y = -1;
		this.energy = DEFAULT_BIRTH_ENERGY;
		this.space = space;
		setBirthThreshold(birthThreshold);
	}

	public void step() {
        moveRandomly();
        decreaseEnergy();
        takeGrass();

        // dying and reproducing is done by the model
	}

	public void draw(SimGraphics graphics) {
	    graphics.drawFastCircle(Color.white);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isAlive() {
        return energy > 0;
	}

	public boolean canReproduce() {
        return energy > birthThreshold;
    }

	public void setXY(int newX, int newY){
		x = newX;
		y = newY;
	}

	public void setBirthThreshold(int birthThreshold) {
        this.birthThreshold = birthThreshold;
    }

	private void decreaseEnergy(){
        energy -= RabbitsGrassSimulationModel.STEP_COST;
    }

    private void moveRandomly(){

        Collections.shuffle(MOVES_LIST);

        boolean agentHasMoved = false;
        Iterator<LegalMoves> moveIterator = MOVES_LIST.iterator();

        while (moveIterator.hasNext() && !agentHasMoved){
            switch (moveIterator.next()){
                case UP:
                    agentHasMoved = space.moveAgentAt(getX(), getY(), getX(), getY() + 1);
                    break;
                case DOWN:
                    agentHasMoved = space.moveAgentAt(getX(), getY(), getX(), getY() - 1);
                    break;
                case RIGHT:
                    agentHasMoved = space.moveAgentAt(getX(), getY(), getX() + 1, getY());
                    break;
                case LEFT:
                    agentHasMoved = space.moveAgentAt(getX(), getY(), getX() - 1, getY());
                    break;
            }
        }

        // TODO what happens if not able to move?
    }

    private void takeGrass() {
	    energy += space.takeGrassEnergyAt(getX(),getY());
    }
}
