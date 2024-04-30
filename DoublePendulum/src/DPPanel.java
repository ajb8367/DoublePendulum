import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//This is the main panel class that has basically everything and has the starter method. 
public class DPPanel extends JPanel implements Runnable{
	private static final long serialVersionUID = 1L;
	public static final int INFO_LINES = 7;
	
	
	//The lock for the threads. I have barely done any Async before and it's been like two years. Excuse the awfulness
	Object lock = new Object();
	
	/*----- CLASS OBJECTS -----*/
	Thread thread = null; //Thread to run the RKN in the background. Horrifying.
	PendGhost ghost = new PendGhost(); //The Ghost reference
	DPend dpend = null; //The double pendulum object
	PendPaint painter = null; //The painter object
	MouseHandler mh = new MouseHandler(); 
	KeyHandler kh = new KeyHandler(); 
	JFrame frame = null; //The reference to the frame. Horrible practice I think. I don't care :3
	
	
	/*----- CLASS FIELDS -----*/
	boolean beingdragged = false; 
	boolean active = false;
	byte selected = -1; //Which "button" is being selected
	int xcent = 600; //Center of the frame in x and y
	int ycent = 600;
	long evaltime = 0; //How long it's been since the last evaluation.
	
	
	/*----- CONSTRUCTORS -----*/
	public DPPanel(JFrame frame) {
		
		//Add the listeners
		addMouseListener(mh);
		addMouseMotionListener(mh);
		frame.addKeyListener(kh);
		
		//Make a new double pendulum, set the ghost
		dpend = new DPend(); 
		dpend.setGhost(ghost);
		
		//Make a new painter object, and pass the ghost by reference
		painter = new PendPaint(ghost);
		
		
		//Set a default size.
		this.setPreferredSize(new Dimension(xcent << 1, ycent << 1));
		
		//Set the frame reference
		this.frame = frame;
		
		
	}
	
	
	
	/*----- STANDARD METHODS -----*/
	
	//Starts the thread and the object
	private void start() {
		restart();
		if (thread == null) {thread = new Thread(this); thread.start();}
		System.out.println("Started");
		popupHelp(); //Gives the help popup
	}
	
	//Restarts the panel
	private void restart() {
		//Get the center coordinates
		xcent = this.getWidth() >> 1;
		ycent = this.getHeight() >> 1;
		
		//Restart the double pendulum object
		dpend.restart(Math.min(xcent, xcent) >> 1);
		
		//Sets the ghost from within the double pendulum object
		dpend.setGhost(ghost);
		
		//Restarts the tail within the painter
		painter.startTail(xcent, ycent);
		
		//Requests to repaint the Swing panel
		repaint();
	}
	
	
	//Just gives a small popup describing the project and describing what to do.
	public void popupHelp() {
		JOptionPane.showMessageDialog(frame,
				"This is an honors project by Aidan Block for PHYS-211 at Penn State York, Spring 2024." + "\nThe underlying algorithm uses a Runge-Kutta-Nystrom method provided by Erwin Felhberg in a public domain NASA technical report" 
						+ "\nHere's a link to the technical report: https://ntrs.nasa.gov/citations/19740026877" + "\n\nTo pause the simulation, press the button at the center, or press \'H\'"
						+ "\nWhile paused:"
						+ "\n - The left/right arrow keys change the bob-mass ratio by a power of two"
						+ "\n - The up/down arrow keys change the gravity by a power of two"
						+ "\n - The bobs can be dragged around when paused, and will assume the released position upon releasing the cursor"
						+ "\n - To reset the bob, press \'R\'"
				, "Double Pendulum - Info & Help " , JOptionPane.INFORMATION_MESSAGE);
	}
	
	//Changes the state of the panel (activity)
	public void changeState() {
		if (active) { //If the state is being changed FROM active
			synchronized(ghost) {dpend.setGhost(ghost);} //Synchronize over the ghost object (to prevent an unreasonably frequent race condition)
			active = false; //Set the state to be inactive
		}
		
		else {  //If the state is being changed FROM inactive
			dpend.fromGhost(ghost); //Grab the current position from the ghost to update the double pendulum			
			
			synchronized(lock) { lock.notify(); } //Tell the thread to wake up by releasing the lock
			
			painter.startTail(this.getWidth() >> 1, this.getHeight() >> 1); //Start the tail
			
			active = true; //set the state to be active
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*----- RUNNABLE INTERFACE ----*/
	/* In the background there is a thread that will update the Pendulum, and request the panel to be repainted.
	 * However I want to maintain some semblance of time-accuracy, therefore I want the pendulum to continue updating WHILE the SWING THREAD draws stuff out.
	 * Therefore, I made it so that this thread will continue running and past a certain time interval will update the "ghost" of the pendulum and repaint the structure
	 * Otherwise the pendulum will continue being updated by the frame-rule.
	 */
	
	public static final int MAX_FPS = 2048; //Max fps, not accurate
	public static final long FRAME_INTERVAL = 1000000000L/MAX_FPS; //min interval between frames, not accurate.
	
	@Override
	public void run() { //The method that should be ran in our new thread.
		long dt = FRAME_INTERVAL; //Set the first step to be the frame interval, because why not

		
		while (thread != null) { //While the thread is active
			
				if (active) { //If the system is active
					
					long timetaken = 0; //The time taken since last calculation in the system in nanos
					
					do {
						dpend.step(dt/1000000000.0); //Call for a step in the double pendulum.
						dt = System.nanoTime() - timetaken; //Calculate the change in time
						timetaken += dt; //Add that time to the time taken
					} while (timetaken <= FRAME_INTERVAL); //Loop until time taken exceeds frame interval.
					
				
					dpend.setGhost(ghost); //Update the ghost
					
					
					repaint(); //Repaint
					
					dt = (System.nanoTime() - timetaken); //Set the time
				}
				
				else { //If the system is inactive, become inactive, lock 
					synchronized(lock) {
						try {lock.wait();}
						catch (InterruptedException e) {e.printStackTrace();}
					}
				}
			}
		
			thread = null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*----- JPANEL EXTENSION -----*/
	public void update(Graphics g) { //An update without clearing the panel. Basically nothing lol
		paint(g);
	}
	
	
	@Override
	public void paintComponent(Graphics g) { //What do do when painting the component
		super.paintComponent(g);
		xcent = this.getWidth() >> 1;
		ycent = this.getHeight() >> 1;
		
		synchronized(ghost) { //Wait til the updater thread is done updating the ghost lol
			painter.draw(g, xcent, ycent, active);	
		}
	}

	


	
	
	
	/*----- MOUSEHANDLER SUBCLASS -----*/
	private class MouseHandler extends MouseAdapter implements MouseMotionListener {
		

		/*----- When the mouse button is pressed down -----*/
		@Override
		public void mousePressed(MouseEvent me) {
			int relx = me.getX() - xcent;
			int rely = me.getY() - ycent;
			byte preselect = ghost.inBob(relx, rely); //Get what was selected
			
			
			if (!active && (preselect == 1 || preselect == 2)) { //If the system is inactive and either of the pendulum bobs were clicked.
				beingdragged = true; //Set the drag status
				selected = preselect; //Change the selector
				ghost.setPosition(selected, relx, rely); //Set the ghost position
			}
			
			
			if (preselect == 0) { //If the preselect is 0
				changeState(); //Change the state
			}
		}

		@Override
		public void mouseDragged(MouseEvent me) {
			if (beingdragged) {
				int relx = me.getX() - xcent;
				int rely = me.getY() - ycent;
				ghost.setPosition(selected, relx, rely);
				repaint();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent me) {
			if (beingdragged) {
				int relx = me.getX() - xcent;
				int rely = me.getY() - ycent;
				ghost.setPosition(selected, relx, rely);
				repaint();
			}
			beingdragged = false;
		}
	}
	
	
	/*----- KEYHANDLER SUBCLASS -----*/
	private class KeyHandler implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			
			//First checks the popup or state change keys
			if (e.getKeyCode() == KeyEvent.VK_H) { popupHelp(); }
			else if (e.getKeyCode() == KeyEvent.VK_K) { changeState(); }
			
			if (!active) { //Checks the codes that are only for when the system is inactive
				//System.out.println("Key Pressed! " + e.getKeyCode());
				switch (e.getKeyCode()) { //
					case (KeyEvent.VK_LEFT):  {dpend.changeMassRatio(false); ghost.changeMassRatio(false); break;}
					case (KeyEvent.VK_RIGHT): {dpend.changeMassRatio(true); ghost.changeMassRatio(true); break;}
					case (KeyEvent.VK_UP):  {dpend.changeGravity(true); ghost.changeGravity(true); break;}
					case (KeyEvent.VK_DOWN): {dpend.changeGravity(false); ghost.changeGravity(false); break;}
					case (KeyEvent.VK_R): {restart();}
					default: {}
				}
				
				repaint();
			}
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
		}
		
	}
	
	
	
	
	/*----- STATIC METHODS -----*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DPPanel pc = new DPPanel(frame);
		frame.getContentPane().add(pc);
		pc.setPreferredSize(new Dimension(400,400));
		frame.setVisible(true);
		frame.pack();
		frame.setTitle("Double Pendulum");
		pc.start();
	}


	
}
