import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;


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


	public void draw(SimGraphics arg0) {
		// TODO Auto-generated method stub
		
	}

	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setXY(int newX, int newY){
		x = newX;
		y = newY;
	}

}
