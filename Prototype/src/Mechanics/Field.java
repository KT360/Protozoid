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
import java.util.ArrayList;
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
	
	Controller controller = new Controller();
	
	//Made the swarm into an array List so that they stop existing when their dead :)
	ArrayList<Zombie> swarm;

	int zombiesNumb = 4;

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
		
		
	}
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		player.drawPlayer(g2d);
		
		controller.renderBullet(g2d);
		drawZombies(g2d);
	}
	public void initZombies()
	{
		
		//new Array
		swarm = new ArrayList<Zombie>();
		
		for(int i =0; i<zombiesNumb; i++)
		{
		swarm.add(new Zombie());
		}
		
		
	}

	public void drawZombies(Graphics2D g)
	{

		//Looks prettier with a for each loop
		for(Zombie z : swarm)
		{
			z.drawZombie(g);
			
		}
		
	}
	
	Point tempPoint;
	@Override
	public void actionPerformed(ActionEvent e) {

		repaint();
		player.move();
		controller.moveBullet();
		
		
		//here as well
		for(Zombie z : swarm)
		{
			
			if(z.shouldChase)
			{
				z.chase(player);
				
			}
			else
			{
				z.attack();
				
			}
		}

		
		checkForColision();
		
		for (int i =0 ; i< swarm.size(); i++)
		{
			for (int j =0; j<controller.hitBox.size(); j++)
			{
				tempPoint = controller.hitBox.get(j);
			
				if (swarm.get(i).zombie.contains(tempPoint))
				{
					
					
					swarm.get(i).Health -= 5;
					
					if (swarm.get(i).Health <=0)
					{
					swarm.get(i).alive = false;
					
					//remove zombie from map
					swarm.remove(i);
					
					}
				}
			
			
			}

		}
		
		
		
		
		
		
		
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
		else if (e.getKeyCode() == KeyEvent.VK_C)
		{
			
			controller.addBullet(new Bullet(player.x, player.y, bulletxVel, bulletyVel));
			
			
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
		
		
	}

	
	
	void checkForColision(){
		
		for(Zombie z : swarm) {
			double distance = Math.sqrt (Math.pow((double) player.xCenter - z.xCenter ,2) +  Math.pow((double) player.yCenter -        			z.yCenter ,2)  ); //pythagoreum theorem for distance between two points
			
			double totalRadius = ((double) player.width) / 2 + ((double) z.width) / 2;
			
			
			//When they collide both they player and the zombies should stop moving
			
			if(distance < totalRadius) {
				/*
				 * Collision resolution goes here
				 */
				
				
				z.shouldChase =false;
				
				player.xDir =0;
				player.yDir = 0;
				
				System.out.println("Collision!");
				
			}
			else
			{
				
				z.shouldChase = true;
			}
		}
	}
}
