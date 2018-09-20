import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	private int x;
	private int y;
	private int energy;
	public static final int DEFAULT_BIRTH_ENERGY = 15;


	public RabbitsGrassSimulationAgent(){
		this.x = -1;
		this.y = -1;
		this.energy = DEFAULT_BIRTH_ENERGY;
	}

	public void step() {
		energy--;
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

}
