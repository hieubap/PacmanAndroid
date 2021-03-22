package com.example.pacman2;

public class Finder {
	private boolean[][] map;
	
	public Finder(PacBoard pb) {
		map = new boolean[27][29];
		
		for(int x=0;x<27;x++)
			for(int y=0;y<29;y++)
			{
				if(pb.map[x][y]>0 && pb.map[x][y]<26){
                    map[x][y] = false;
                }else{
                    map[x][y] = true;
                }
			}
		
	}
	
	public moveType getMove(int a,int b,int x,int y) {
		if(x==a && y==b){
            return moveType.NONE;
        }
		
		boolean[][] valid = new boolean[27][29];
		
		for(int i=0;i<27;i++)
			for(int j=0;j<29;j++)
				valid[i][j]= false;
		valid[a][b] = true;
		
		Find[] Maze = new Find[500];
		
		int size = 1;
		int done=0;
		Find start = new Find(a, b, -1);
		Maze[0] = start;
		
		for(int i=0;i< size ; i++)			
		{
			if(Maze[i].x==x&&Maze[i].y==y)
			{
			done=i;
			break;
			}
			if(Maze[i].x>0&&map[Maze[i].x-1][Maze[i].y]&&!valid[Maze[i].x-1][Maze[i].y])
			{
				valid[Maze[i].x-1][Maze[i].y]=true;
				Find m = new Find(Maze[i].x-1, Maze[i].y, i);
				Maze[size] = m;
				size++;
			}
			if(Maze[i].y>0&&map[Maze[i].x][Maze[i].y-1]&&!valid[Maze[i].x][Maze[i].y-1])
			{
				valid[Maze[i].x][Maze[i].y-1]=true;
				Find m = new Find(Maze[i].x, Maze[i].y-1, i);
				Maze[size] = m;
				size++;
			}
			if(Maze[i].x<27&&map[Maze[i].x+1][Maze[i].y]&&!valid[Maze[i].x+1][Maze[i].y])
			{
				valid[Maze[i].x+1][Maze[i].y]=true;
				Find m = new Find(Maze[i].x+1, Maze[i].y, i);
				Maze[size] = m;
				size++;
			}
			if(Maze[i].y<29&&map[Maze[i].x][Maze[i].y+1]&&!valid[Maze[i].x][Maze[i].y+1])
			{
				valid[Maze[i].x][Maze[i].y+1]=true;
				Find m = new Find(Maze[i].x, Maze[i].y+1, i);
				Maze[size] = m;
				size++;
			}
			
		}
		int xp = -1;
		int yp = -1;
		
		while(Maze[done].parent!=0) {
			done = Maze[done].parent;
		}
			xp = Maze[done].x;
			yp = Maze[done].y;
		
		if(xp == a - 1 && yp == b)
			return moveType.LEFT;
		if(xp == a + 1 && yp == b)
			return moveType.RIGHT;
		if(xp == a && yp == b - 1)
			return moveType.UP;
		if(xp == a && yp == b + 1)
			return moveType.DOWN;
		return moveType.NONE;
		
	}

	private class Find{
		public int x,y,parent;
		
		public Find(int a,int b,int c) {
			x=a;
			y=b;
			parent=c;
		}
	}
}
