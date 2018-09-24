import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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
	private Image rabbitImage;

	public static final int DEFAULT_BIRTH_ENERGY = 5;

	public enum LegalMoves { UP, DOWN, RIGHT, LEFT }

    public static final List<LegalMoves> MOVES_LIST =
            Arrays.asList(LegalMoves.values());

    public RabbitsGrassSimulationAgent(
            RabbitsGrassSimulationSpace space,
            int birthThreshold, int energyAtBirth){
		this.x = -1;
		this.y = -1;
		this.energy = energyAtBirth;
		this.space = space;
		setBirthThreshold(birthThreshold);

        try {
            File rabbitImageFile = new File("bolliger-gerard-ex1/images/rabbit.png");
            System.out.print(rabbitImageFile.getAbsolutePath());
            this.rabbitImage = ImageIO.read(rabbitImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void step() {
        moveRandomly();
        decreaseEnergy();
        takeGrass();

        // dying and reproducing is done by the model
	}

	public int reproduce(){
        this.energy = this.energy / 2;
        return this.energy;
    }

	public void draw(SimGraphics graphics) {
        if (rabbitImage == null){
            graphics.drawFastCircle(Color.WHITE);
        }
        else {
            graphics.drawImageToFit(rabbitImage);
        }
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
