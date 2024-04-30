import java.awt.Color;
import java.awt.Graphics;

public class PendPaint {
	//The button radii
	public static int BUT_RAD = 10;
	public static int PRI_RAD = 10;
	public static int SEC_RAD = 10;
	
	//The Pendulum ghost references, and the tail objects
	PendGhost pg = null;
	private int index = 0;
	public static final int TAIL_LENGTH = 512;
	private int[] xtail = new int[TAIL_LENGTH];
	private int[] ytail = new int[TAIL_LENGTH];
	private float[] htail = new float[TAIL_LENGTH];
	
	
	
	public PendPaint(PendGhost pg) {
		this.pg = pg; //Just passes a reference
	}
	
	
	//The main draw method
	public void draw(Graphics g, int xorigin, int yorigin, boolean status) {
		int x1 = pg.getx1(), x2 = pg.getx2(), y1 = pg.gety1(), y2 = pg.gety2();
		
		//System.out.print(status);
		if (status) {paintTail(g, x2 + xorigin,y2 + yorigin);}
		
		g.setColor(Color.black);
		g.drawLine(xorigin, yorigin, xorigin + x1, yorigin + y1);
		g.drawLine(xorigin + x1, yorigin + y1, xorigin + x2, yorigin + y2);
		
		//drawing the bobs
		g.setColor((status) ? Color.blue : Color.red);
		g.fillOval(xorigin - BUT_RAD, yorigin - BUT_RAD, BUT_RAD << 1, BUT_RAD << 1);
		g.setColor(Color.DARK_GRAY);
		g.fillOval(xorigin - PRI_RAD + x1, yorigin - PRI_RAD + y1, PRI_RAD << 1, PRI_RAD << 1);
		g.fillOval(xorigin - SEC_RAD + x2, yorigin - SEC_RAD + y2, SEC_RAD << 1, SEC_RAD << 1);
		
		g.drawString("Press \'H\' for Help & Info!", 10, 10);
		g.drawString(String.format("Gravity Level %3d | Mass Level: %3d", pg.getGrav(), pg.getMass()), 10, 22);
		g.drawString(String.format("(%3d, %3d)", x2,y2), 10, 34);
		g.drawString(String.format("%1.7f seconds", pg.getTime()), 10, 46);
		
	}
	
	//Method to paint the tail
	public void paintTail(Graphics g, int x, int y) {
		g.setColor(Color.BLUE);
		
		
		
		g.drawLine(xtail[index], ytail[index], x, y);
		index = (index == 0) ? TAIL_LENGTH - 1 : index - 1; //Increase the index to store the next time a point is read
		xtail[index] = x; //Set a new tail element at the index
		ytail[index] = y; //Set a new tail element at the index
		htail[index] = pg.getHue();	
		
		int cycind1, cycind2 = cycind1 = index; //Stores the cycle indicies, which initially start at the stored index. Cycle
		
		for (int i = 0; i < TAIL_LENGTH - 1; i++) { //Loop length - 1 times
			float scalar = 1.0f - (float)i/TAIL_LENGTH; //Scalar interpolation for the tail
			cycind2 = (cycind2 == TAIL_LENGTH - 1) ? 0 : cycind2 + 1; //Step one backwards
			g.setColor(Color.getHSBColor(htail[cycind2], scalar, .75f));
			g.drawLine(xtail[cycind1], ytail[cycind1], xtail[cycind2], ytail[cycind2]); //Draws the lines between the points
			cycind1 = cycind2; //Matches the cycle indicies
		}
	
	}
	
	
	
	//Method to start the tail, putting the center x and y coordinates.
	public void startTail(int xorigin, int yorigin) {
		int x = pg.getx2() + xorigin;
		int y = pg.gety2() + yorigin;
		for (int i = 0; i < TAIL_LENGTH; i++) {
			xtail[i] = x;
			ytail[i] = y;
			htail[i] = 0;
		}
	}
	
	
	
}
