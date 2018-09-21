import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	private int x;
	private int y;
	private int energy;
	private RabbitsGrassSimulationSpace space;
    private ArrayList<LegalMoves> moves;

	public static final int DEFAULT_BIRTH_ENERGY = 15;

	public enum LegalMoves {
        UP, DOWN, RIGHT, LEFT
    }

    public RabbitsGrassSimulationAgent(RabbitsGrassSimulationSpace space){
		this.x = -1;
		this.y = -1;
		this.energy = DEFAULT_BIRTH_ENERGY;
		this.space = space;
        this.moves = new ArrayList<LegalMoves>(Arrays.asList(LegalMoves.values()));
	}

	public void step() {
        moveRandomly();
        decreaseEnergy();
        takeGrass();
	}

	public void draw(SimGraphics graphics) {
		graphics.drawFastRect(Color.white);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setXY(int newX, int newY){
		x = newX;
		y = newY;
	}

	private void decreaseEnergy(){
        energy--;
    }

    private void moveRandomly(){

        Collections.shuffle(moves);

        boolean agentHasMoved = false;
        Iterator<LegalMoves> moveIterator = moves.iterator();

        while(moveIterator.hasNext() && !agentHasMoved){
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

}
