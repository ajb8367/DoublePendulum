
public class PendGhost {
	//Fields parallel to the DPend object
	private double time;
	private int x1;
	private int y1;
	private int x2;
	private int y2;
	private double v;
	private int gravlevel;
	private int masslevel;
	
	
	
	//----- Ghost Setting and Overwriting-----/
	public void setGhost(double time, int x1, int y1, int x2, int y2, double v2, int gravlevel, int masslevel) {
		this.time = time;
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.v = v2;
		this.gravlevel = gravlevel;
		this.masslevel = masslevel;
	}
	
	public void overwriteGhost(PendGhost pg) {
		this.time = pg.time;
		this.x1 = pg.x1;
		this.x2 = pg.x2;
		this.y1 = pg.y1;
		this.y2 = pg.y2;
		this.v = pg.v;
		this.gravlevel = pg.gravlevel;
		this.masslevel = pg.masslevel;
	
		
	}
	
	//Set the positions
	public void setPosition(int selection, int x, int y) {
		if (selection < 1 || selection > 2) {throw new IllegalArgumentException("The setPosition method was given illegal selection " + selection);} //Checks if my dumbass made an illegal argument go in
		
		if (selection == 1) {x1 = x; y1 = y;} //Adjust the angle and length of bob 1
		else {x2 = x; y2 = y;} //Adjust the angle and length of bob 2
	}
	
	//Changes the gravity level for the ghost, which will then be forwarded to the pendulum
		public void changeMassRatio(boolean positive) {
			if (positive && masslevel != 10) {
				masslevel++;
			}
			else if (!positive && masslevel != 1) {
				masslevel--;
			}
		}
		
		//Changes the gravity level for the ghost, which will then be forwarded to the pendulum
		public void changeGravity(boolean positive) {
			if (positive && gravlevel != 10) {
				gravlevel++;
			}
			else if (!positive && gravlevel != 1) {
				gravlevel--;
			}
		}
		
	
	
	//System getters
	public int getx1() {return x1;}
	public int getx2() {return x2;}
	public int gety1() {return y1;}
	public int gety2() {return y2;}
	public float getHue() {return (float)(0.33333333333333F/(1 + v * v / (gravlevel * 50)));}
	public int getGrav() {return gravlevel;}
	public int getMass() {return masslevel;}
	public double getTime() {return time;}
	

	
	//Returns which body the point lies in
		public byte inBob(int inx, int iny) {
			if ((inx * inx + iny * iny) <= PendPaint.BUT_RAD * PendPaint.BUT_RAD) {
				return 0;
			}
			double delx = (x1 - inx), dely = (y1 - iny);
			if ((delx * delx + dely * dely) <= PendPaint.PRI_RAD * PendPaint.PRI_RAD){
				return 1;
			}
			delx = (x2 - inx);
			dely = (y2 - iny);
			if ((delx * delx + dely * dely) <= PendPaint.PRI_RAD * PendPaint.PRI_RAD){
				return 2;
			}
			return -1;
		}
		
		
}
