package Mechanics;

import java.awt.Color;

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
	
	Timer damageDelay;
	
	Player player = new Player();
	
	Controller controller = new Controller();
	
	//Made the swarm into an array List so that they stop existing when their dead :)
	ArrayList<Zombie> swarm;
	
	Zombie testBuddy = new Zombie();

	int zombiesNumb = 10;

	int bulletxPos;
	int bulletyPos;
	int bulletxVel;
	int bulletyVel;
	
	int delay = 100;

	boolean isDashing = false;
	
	boolean isBeingEaten = false;
	
	int playerHealth =100;
	
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
		renderUI(g2d);
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
	
	public void renderUI(Graphics g)
	{
		
		
		g.setColor(Color.RED);
		g.fillRect(20, 20, 100, 30);
		
		g.setColor(Color.GREEN);
		g.fillRect(20, 20, playerHealth, 30);
		
	}
	
	Point tempPoint;
	@Override
	public void actionPerformed(ActionEvent e) {

		repaint();
		controller.moveBullet();
		
		if(isDashing && player.energy > 0)
		{
			
			player.dash();
			player.energy-= 5;
			
		}
		else
		{
			player.move(player.xDir,player.yDir);
			
		}
		
		checkForColision();
		
		for(Zombie z : swarm)
		{
			
			if(z.knockedBack)
			{
				
				if(z.knockBackForce > 0)
				{
					z.pullBack(z.knockBackDir);
					
					z.knockBackForce--;
					
					System.out.println(z.knockBackForce);
					
					if(z.knockBackForce == 0)
					{
						z.knockedBack = false;
						
						z.knockBackForce = 3;
						
					}
				}
			}
			
		}
		
		//here as well
		for(Zombie z : swarm)
		{
			
			if(z.shouldChase)
			{
				z.chase(player);
				
			}
			else if (isBeingEaten)
			{
				z.attack();
				
				//Delay the damage
				//Makes the game a bit more fair
				delay--;
				
				if(delay == 0)
				{
					playerHealth-=20;
					delay = 100;
					
				}
			
				
				
				
						 
			}
		}

		
		
		
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
		
		if (e.getKeyCode() == KeyEvent.VK_UP && !isDashing)
		{
			
			player.yDir =-5;
			
			player.dashDirY = -10;
			player.dashDirX = 0;
			
			bulletxVel = 0;
			bulletyVel = -5;
		}
		
		if (e.getKeyCode() == KeyEvent.VK_DOWN  && !isDashing)
		{
			
			player.yDir = 5;
			
			player.dashDirY = 10;
			player.dashDirX =0;
			
			bulletxVel = 0;
			bulletyVel = 5;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT  && !isDashing)
		{
			
			player.xDir = 5;
			
			player.dashDirX = 10;
			player.dashDirY = 0 ;
			
			bulletxVel = 5;
			bulletyVel =0;
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT  && !isDashing)
		{
			
			player.xDir =-5;
			
			player.dashDirX = -10;
			player.dashDirY = 0 ;
			
			bulletxVel = -5;
			bulletyVel = 0;
			
		}
		if (e.getKeyCode() == KeyEvent.VK_C)
		{
			
			controller.addBullet(new Bullet(player.xCenter, player.yCenter-5, bulletxVel, bulletyVel));
			
			
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			isDashing = true;
		
			
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP)
		{
			
			player.yDir =0;
			
		}
		
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			
			player.yDir = 0;
			
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			
			player.xDir = 0;
			
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
		{
			
			player.xDir =0;
			
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			isDashing = false;
			player.energy = 100;
		}
		
		
	}

	
	
	void checkForColision(){
		
		//Collision with the player
		
		for(Zombie z : swarm) {
			double distance = Math.sqrt (Math.pow((double) player.xCenter - z.xCenter ,2) +  Math.pow((double) player.yCenter - z.yCenter ,2)  ); //pythagoreum theorem for distance between two points
			
			double totalRadius = ((double) player.width) / 2 + ((double) z.width) / 2;
			
			
			//When they collide both they player and the zombies should stop moving
			
			if(distance < totalRadius) {
				/*
				 * Collision resolution goes here
				 */

				
				z.shouldChase =false;
				isBeingEaten = true;
				player.xDir =0;
				player.yDir = 0;
				
				
			}
			else
			{
				
				isBeingEaten = false;
			}

			
		}
		
		
		//Collision with other zombies
		
		for(int i =0; i<swarm.size(); i++) {
			
			for(int j =0; j<swarm.size(); j++)
			{
				
				if(j == i)
				{
					
					continue;
				}
			
				double distance = Math.sqrt (Math.pow((double) swarm.get(i).xCenter - swarm.get(j).xCenter ,2) +  Math.pow((double) swarm.get(i).yCenter - swarm.get(j).yCenter ,2)  ); //pythagoreum theorem for distance between two points
					
				double totalRadius = ((double) swarm.get(i).width) / 2 + ((double) swarm.get(j).width) / 2;
			
				
				if(distance < totalRadius)
				{
					
					
						int yDiff = swarm.get(i).yCenter - swarm.get(j).yCenter;
						
						int xDiff = swarm.get(i).xCenter - swarm.get(j).xCenter;
						
						int rangeCheck = Math.abs(swarm.get(i).xCenter - swarm.get(j).xCenter);
						
						int width = swarm.get(i).width;
						
						
						
						
						//Blocked top
						if(yDiff > 0 && ((width/2) - rangeCheck) > 0)
						{
					
							
								swarm.get(i).shouldChase = false;
								
									
									swarm.get(i).zombie.y += 10;
									swarm.get(i).yCenter += 10;
									swarm.get(i).knockedBack = true;
									swarm.get(i).knockBackDir = 3;
								
							System.out.println("blocked top");
						}
						//Right
						 if (xDiff <= (totalRadius - 5 )* -1)
						{
							 
							 swarm.get(i).shouldChase = false;
							 
							    
									swarm.get(i).zombie.x -= 10;
									swarm.get(i).xCenter -= 10;
									swarm.get(i).knockedBack = true;
									swarm.get(i).knockBackDir = 4;
									
							System.out.println("blocked right");
							
						}
						 
						//Bottom
						 if(yDiff < 0 && ((width/2) - rangeCheck) > 0)
						{
							
							 swarm.get(i).shouldChase = false;
							 
							        swarm.get(i).zombie.y -= 10;
									swarm.get(i).yCenter -= 10;
									swarm.get(i).knockedBack = true;
									swarm.get(i).knockBackDir = 1;
							System.out.println("blocke bottom");
						}
						//left
						else if (xDiff >= (totalRadius - 5 ))
						{
							swarm.get(i).shouldChase = false;
							
								swarm.get(i).zombie.x += 10;
								swarm.get(i).xCenter += 10;
								swarm.get(i).knockedBack = true;
								swarm.get(i).knockBackDir = 2;
							System.out.println("blocked left");
							
							
						}
						
					
				}
				else
				{
					
					swarm.get(i).shouldChase =true;
				}
				
			}
			
			
		}
	}
}
