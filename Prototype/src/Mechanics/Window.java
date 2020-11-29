package Mechanics;

import javax.swing.JFrame;

public class Window extends JFrame {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3570390636310826205L;

	public Window()
	{
		//change the name approprietly!
		setTitle("ZOMBIE MANIA!");
		
		Field field = new Field();
		setSize(800,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		getContentPane().add(field);
	}
	
	public static void main (String []args)
	{
		
		
		Window w = new Window();
		
		
	}
}
