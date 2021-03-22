package com.example.pacman2;

public class Camera {
	private int y;
	private Pacman pacman;
	private PacBoard pacBoard;
	
	public Camera(Pacman pacman,PacBoard pacBoard) {
		this.pacman = pacman;
		this.pacBoard = pacBoard;
		y=0;
	}
	
	public void update() {
		if(pacman.pixelPosition.y>=300&&pacman.pixelPosition.y<=450)
			{
			y= -pacman.pixelPosition.y+300;
			pacBoard.setDraw(y);
			pacBoard.repaint();
			}
	}
}
