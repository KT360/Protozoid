package Mechanics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;

import javax.swing.JComponent;

public class Bullet{
	
	int x;
	int y;
	int xDir;
	int yDir;
	public Bullet(int x, int y, int xVelocity, int yVelocity)
	{
		
		this.x = x;
		this.y = y;
		this.xDir = xVelocity;
		this.yDir = yVelocity;
	
	}
	
	public void drawBullet(Graphics2D g)
	{
		
		g.setColor(Color.black);
		g.fill(new Ellipse2D.Double(x,y,10,10));
		
	}
	public void tick()
	{
	this.x+= this.xDir;
	this.y+= this.yDir;
		
	}
	
	public void setxDir(int value)
	{
		
		this.xDir = value;
		
	}
	
	public void setyDir(int value)
	{
		this.yDir = value;
		
		
	}
	
	public int getX()
	{
		
		return x;
		
	}
	
	public int getY()
	{
		
		return y;
		
	}


}
