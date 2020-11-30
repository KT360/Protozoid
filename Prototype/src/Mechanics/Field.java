package Mechanics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

public class Field extends JPanel implements ActionListener, KeyListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5932129097751782616L;
	Timer t = new Timer(5, this);
	Player player = new Player();
	//Zombie Zombie = new Zombie();
	Controller controller = new Controller();
	Zombie[] swarm;


	int bulletxPos;
	int bulletyPos;
	int bulletxVel;
	int bulletyVel;
	
	
	public Field()
	{
		
		t.start();
		setFocusable(true);
		addKeyListener(this);
		initZombies();
		//initZombiesPos();
		
	}
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		player.drawPlayer(g2d);
		//Zombie.drawZombie(g2d);
		controller.renderBullet(g2d);
		drawZombies(g2d);
	}
	public void initZombies()
	{
		
		swarm = new Zombie[1];
		for (int i=0; i<swarm.length; i++)
		{
			swarm[i] = new Zombie();

		}
		
		
		
	}
	
	public void initZombiesPos() {
		
		Random r = new Random();
			
		
			
		for (int i=0; i<swarm.length; i++)
		{
			
			swarm[i].setX (r.nextInt(800));
			swarm[i].setY(r.nextInt(300));

		}
	}
	
	public void drawZombies(Graphics2D g)
	{
		
		for (int i =0; i<swarm.length; i++)
		{
			
			
			swarm[i].drawZombie(g);
		}
		
		
		
	}
	Point tempPoint;
	@Override
	public void actionPerformed(ActionEvent e) {

		repaint();
		player.move();
		controller.moveBullet();
		//Zombie.chase(player);
		
		for (int i= 0; i<swarm.length; i++)
		{
			
			swarm[i].chase(player);
			
		}
		
		for (int i =0 ; i< swarm.length; i++)
		{
		for (int j =0; j<controller.hitBox.size(); j++)
		{
			tempPoint = controller.hitBox.get(j);
		
		if (swarm[i].zombie.contains(tempPoint))
		{
			
			
			swarm[i].Health -= 5;
			
			if (swarm[i].Health <=0)
			{
			swarm[i].alive = false;
			}
		}
		
		
		}
//		else 
//		{
//			
//			Zombie.alive = true;
//			
//		}
		}
		
		
		//after the zombies move or whatever
		//check for collision
		//feel free to move the function elsewhere
		
		checkForColision();
		
		
		
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		
		if (e.getKeyCode() == KeyEvent.VK_UP)
		{
			
			player.yDir =-10;
		
			bulletxVel = 0;
			bulletyVel = -5;
		}
		
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			
			player.yDir = 10;
		
			bulletxVel = 0;
			bulletyVel = 5;
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			
			player.xDir = 10;
			
			bulletxVel = 5;
			bulletyVel =0;
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT)
		{
			
			player.xDir =-10;
			
			bulletxVel = -5;
			bulletyVel = 0;
			
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP)
		{
			
			player.yDir =0;
			
		}
		
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			
			player.yDir = 0;
			
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			
			player.xDir = 0;
			
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT)
		{
			
			player.xDir =0;
			
		}
		else if (e.getKeyCode() == KeyEvent.VK_C)
		{
			
			controller.addBullet(new Bullet(player.x, player.y, bulletxVel, bulletyVel));
			
			
		}
		
	}

	//this is a toy collision detection system!!
	//shall a player surpass a zombie within one frame
	//this will not catch the error,although with the specified speed
	//no such problem will arise
	//also this aproximates the square as a circle
	//being a little forgiving over the edges
	//but is more forgiving to the eye implementing this with pythagoreum theorem
	//rather than another algorithm
	
	void checkForColision(){
		
		for(Zombie z : swarm) {
			double distance = Math.sqrt (Math.pow((double) player.xCenter - z.xCenter ,2) +  Math.pow((double) player.yCenter - z.yCenter ,2)  ); //pythagoreum theorem for distance between two points
			double totalRadius = ((double) player.width) / 2 + ((double) z.width) / 2;
			
			if(distance < totalRadius) {
				/*
				 * Collision resolution goes here
				 */
			}
		}
	}
}
