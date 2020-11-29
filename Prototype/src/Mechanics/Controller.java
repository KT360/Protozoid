package Mechanics;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.LinkedList;

public class Controller {
	
	LinkedList <Bullet> ll = new LinkedList<Bullet>();
	LinkedList <Point>  hitBox = new LinkedList<Point>();
	
	Bullet tempBullet;
	Point tempPoint;
	public void renderBullet(Graphics2D g)
	{
		
		for (int i=0; i<ll.size(); i++)
		{
			tempBullet = ll.get(i);
			
			tempBullet.drawBullet(g);
			
			
		}
		
		
	}
	
	public void moveBullet()
	{
		for (int i=0; i<ll.size(); i++)
		{
			
		tempBullet = ll.get(i);
		tempPoint = hitBox.get(i);	
		tempPoint.move(tempBullet.getX(), tempBullet.getY());
		tempBullet.tick();
		
		
		if (tempBullet.getY()<=0)
		{
			
			removeBullet(tempBullet);
			hitBox.remove(i);
		}
		else if (tempBullet.getX()<=0)
		{
			
			removeBullet(tempBullet);
			hitBox.remove(i);
		}
		else if (tempBullet.getX()>=800)
		{
			
			removeBullet(tempBullet);
			hitBox.remove(i);
		}
		else if (tempBullet.getY()>= 600)
		{
			
			removeBullet(tempBullet);
			hitBox.remove(i);
		}
		
		
		}
		
	}
	
	public void addBullet(Bullet bullet)
	{
		ll.add(bullet);
		hitBox.add(new Point (bullet.getX(),bullet.getY()));
	}
	

	
	
	public void removeBullet(Bullet bullet)
	{
		ll.remove(bullet);
		
	}
	

}
