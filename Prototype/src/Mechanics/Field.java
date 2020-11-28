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
		
		swarm = new Zombie[100];
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

}
