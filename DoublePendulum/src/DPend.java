
import java.util.Random;


public class DPend implements MatrixConstants{
	
	
	
	/*----- FIELDS -----*/
	/* The DPRKN object stores the angles, the angular velocity, and the angular acceleration with the lengths of the objects. The mass ratio is currently going to be held constant
	 * Every step, the angular parameters will be updated via the RKN-8 method in the step() method. The acceleration will be the last elements of the stored array of acceleration calculations
	 * Whenever there is an adjustment to any of the lengths, the time probably needs to be wiped. Fortunately the acceleration itself is autonomous and does not depend on the time.
	 */
	public static int PPI = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
	
	
	//GENERAL
	private double time;
	private double gravity = 386.0885826772 * java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
	private double massrat = 1;
	private int massratpower = 5; //1 - 10
	private int gravpower = 4; //1 - 8
	private double[] aArray = new double[18]; //The array of accelerations
	
	//PRIMARY BOB
	private double ang1; 
	private double vel1;
	private double acc1;
	private double len1;
	
	//SECONDARY BOB
	private double ang2;
	private double vel2;
	private double acc2;
	private double len2;
	
	
	
	
	
	/*----- CONSTRUCTORS -----*/
	public DPend() {
		System.out.println(gravity); //Print out the current state
		restart(200); //Start the pendulum.
	}
	
	
	
	/*----- GETTERS -----*/
	
	//Just returns the double for the "time" it has been running. Not accurate at all. Why do I bother.
	public double getTime() {return time;}
	
	//These methods will return the x or y coordinates relative to the center of the screen as a double. The container objects will handle how it is used.
	public double getPrimeX() {return len1 * Math.sin(ang1);}
	public double getPrimeY() {return len1 * Math.cos(ang1);}
	public double getSecondX() {return len2 * Math.sin(ang2) + getPrimeX();}
	public double getSecondY() {return len2 * Math.cos(ang2) + getPrimeY();}
	
	//Returns the angles. I don't know if these are even used to be honest. I am too scared to check.
	public double getPrimeAng() {return ang1;}
	public double getSecondAng() {return ang2;}	
	
	//Used for debug info.
	public int getGrav() {return gravpower;}
	public int getMassRatio() {return massratpower;}
	
	
	
	/*----- SETTERS -----*/
	
	//Allows the position of the bob to be set by an x, y coordinate, and will update the parameters accordingly. 
	public void setPosition(int selection, int x, int y) {
		if (selection < 1 || selection > 2) {throw new IllegalArgumentException("The setPosition method was given illegal selection " + selection);} //Checks if I let an illegal argument go in

		double newang = Math.atan2(x,y), newlen = Math.hypot(x, y); //Gets the polar coordinates of the new position relative to the origin.
		
		if (selection == 1) {ang1 = newang; len1 = newlen;} //Adjust the angle and length of bob 1
		else {ang2 = newang; len2 = newlen;} //Adjust the angle and length of bob 2
		
		//There's mild error here, since the input is an integer.
		
		resetDerivatives();
	}
	
	//Sets the argument to reflect the current state of the system
	public void setGhost(PendGhost ghost) {
		ghost.setGhost(time, (int)getPrimeX(),(int)getPrimeY(),(int)getSecondX(),(int)getSecondY(), vel2 - vel1, gravpower, massratpower);
	}
	
	//Sets the system to reflect the ghost state
	public void fromGhost(PendGhost ghost) {
		setPosition(1, ghost.getx1(), ghost.gety1());
		setPosition(2, ghost.getx2() - ghost.getx1(), ghost.gety2() - ghost.gety1());
	}
	
	
	//Adjust the mass power, the boolean being if it's incremented or decremented
	public void changeMassRatio(boolean positive) {
		if (positive && massratpower != 10) {
			massratpower++;
			massrat /= 2.0;
		}
		else if (!positive && massratpower != 1) {
			massratpower--;
			massrat *= 2.0;
		}
	}
	//Adjust the gravity power, the boolean being if it's incremented or decremented
	public void changeGravity(boolean positive) {
		if (positive && gravpower != 8) {
			gravpower++;
			gravity *= 2.0;
		}
		else if (!positive && gravpower != 1) {
			gravpower--;
			gravity /= 2.0;
		}
	}
	
	
	
	
	
	
	/*----- INDUCTIVE METHODS -----*/
	
	//Increments the RKN System. I will possibly make it adaptive step eventually.
	public double step(double interval){
		//print(interval);
	
		
		//Temporary variables
		double tang1 = 0, tvel1 = 0;
		double tang2 = 0, tvel2 = 0;
		
		
		//Set first acceleration values
		aArray[0] = acc1;
		aArray[9] = acc2;
		
		
		//Outer loop
		for (int i = 1; i <= 8; i++) {
			
			tang1 = tvel1 = tang2 = tvel2 = 0; //Reset the temporary variables
			
			
			//Inner loop for the temporary time and velocity summations
			for (int j = 0; j < i; j++) {
				tang1 += POS_WEIGHTS[i - 1][j] * aArray[j];
				tang2 += POS_WEIGHTS[i - 1][j] * aArray[j + 9];
				tvel1 += VEL_WEIGHTS[i - 1][j] * aArray[j];
				tvel2 += VEL_WEIGHTS[i - 1][j] * aArray[j + 9];
			}
			
			//Full evaluation of temporary position and velocity terms via Horner's Rule
			tang1 = interval * ((interval * tang1) + (vel1 * TIME_WEIGHTS[i - 1])) + ang1;
			tang2 = interval * ((interval * tang2) + (vel2 * TIME_WEIGHTS[i - 1])) + ang2;
			tvel1 = (interval * tvel1) + vel1;
			tvel2 = (interval * tvel2) + vel2;
			
			
			//LOTS of calculated stuff for the differential equations
			double angd = tang2 - tang1, s = Math.sin(angd), c = Math.cos(angd), det = s/(massrat + s * s);
			double x = len1 * tvel1 * tvel1 + gravity * Math.cos(tang1), y = len2 * tvel2 * tvel2;
			
			//Acceleration calculations
			aArray[i] = (det * (c * x + y) - gravity * Math.sin(tang1)) / len1;
			aArray[i + 9] = -1 * det * ((1 + massrat) * x + c * y) / len2;
		}
		
		//Set the fields
		time += interval;
		ang1 = angleModulo(tang1);
		vel1 = tvel1;
		acc1 = aArray[8];
		ang2 = angleModulo(tang2);
		vel2 = tvel2;
		acc2 = aArray[17];
		
		//Return the time just in case it needs to be used.
		return time;
	}
	
	
	//Resets the derivatives and acceleration in case the system needs to be restarted.
	public void resetDerivatives() {
		time = vel1 = vel2 = 0;
		
		//LOTS of calculated stuff for the differential equations
		double angd = ang2 - ang1, s = Math.sin(angd), c = Math.cos(angd), det = s/(massrat + s * s);
		double x = gravity * Math.cos(ang1);
		
		//Acceleration calculations
		acc1 = (det * c * x - gravity * Math.sin(ang1)) / len1;
		acc2 = -1 * det * (1 + massrat) * x / len2;
	}
	
	
	public void restart(int rad) {
		Random rand = new Random();
		
		//Generate a random position to place the bobs
		len1 = (int)(rad*Math.sqrt(rand.nextDouble(0,1)));
		ang1 = (int)rand.nextDouble(-Math.PI,Math.PI);
		len2 = (int)(rad*Math.sqrt(rand.nextDouble(0,1)));
		ang2 = (int)rand.nextDouble(-Math.PI,Math.PI);
		
		resetDerivatives();
	}
	
	
	
	
	/*----- MISC METHODS -----*/
	
	//The standard toString method override for debugging purposes.
	@Override
	public String toString() {
		return String.format("Angles: (%1.7f, %1.7f) at %1.7fs", ang1, ang2, time, vel1, vel2, acc1, acc2);
		}
	
	
	
	
	
	
	
	/*----- STATIC METHODS -----*/
	
	//Returns the angle mod 2pi in order to make sure the angle double doesn't explode
	public static double angleModulo(double angle) {
		double scalar = angle / (Math.PI); //Scalar to check to see if angle is in (-pi, pi]
		if (scalar <= -1 || 1 < scalar) {
			angle -= 2*Math.PI*(1.0 + Math.floor((scalar - 1)/2)); //Finds the modulus centered about 0
		}
		return angle;
	}
}
	








