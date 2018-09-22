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
	private RabbitsGrassSimulationSpace space;

	public static final int DEFAULT_BIRTH_ENERGY = 15;

	public enum LegalMoves {
        UP, DOWN, RIGHT, LEFT
    }

    public static final List<LegalMoves> MOVES_LIST =
            Arrays.asList(LegalMoves.values());

    public RabbitsGrassSimulationAgent(RabbitsGrassSimulationSpace space){
		this.x = -1;
		this.y = -1;
		this.energy = DEFAULT_BIRTH_ENERGY;
		this.space = space;
	}

	public void step() {
        moveRandomly();
        decreaseEnergy();
        takeGrass();
        eventuallyDie();
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

	public void setXY(int newX, int newY){
		x = newX;
		y = newY;
	}

	private void decreaseEnergy(){
        energy--;
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

    private void takeGrass(){
	    energy += space.getGrassEnergyAt(getX(),getY());
    }

    private void eventuallyDie() {
        if (!isAlive()) space.removeAgentAt(getX(), getY());
    }
}
