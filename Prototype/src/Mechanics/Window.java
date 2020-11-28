package Mechanics;

import javax.swing.JFrame;

public class Window extends JFrame {

	
	
	public Window()
	{
		
		Field field = new Field();
		setSize(800,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		add(field);
	}
	
	public static void main (String []args)
	{
		
		
		Window w = new Window();
		
		
	}
}
