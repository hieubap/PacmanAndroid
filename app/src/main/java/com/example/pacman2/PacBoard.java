package com.example.pacman2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.wifi.p2p.WifiP2pManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.w3c.dom.Text;
import java.util.ArrayList;

public class PacBoard extends SurfaceView implements SurfaceHolder.Callback {
    public final int WIDTH = 1024;
    public final int HEIGHT = 1920;


//    Timer redrawTimer;
//    ActionListener redrawAL;

    int[][] map;
    Bitmap[] mapSegments;

    Bitmap foodImage;
    Bitmap[] pfoodImage;

    Bitmap goImage;
    Bitmap vicImage;

    Pacman pacman;
    ArrayList<Food> foods;
    ArrayList<PowerUpFood> pufoods;
    ArrayList<Ghost> ghosts;
    ArrayList<TeleportTunnel> teleports;

    boolean isCustom = false;
    boolean isGameOver = false;
    boolean isWin = false;
    boolean drawScore = false;
    boolean clearScore = false;
    int scoreToAdd = 0;

    int score,ycamera;
    Text scoreboard;

    LoopPlayer siren;
    boolean mustReactivateSiren = false;
    LoopPlayer pac6;

    public Point ghostBase;

    public int m_x;
    public int m_y;

    MapData md_backup;
    PacWindow windowParent;


    public PacBoard(Context context,Text scoreboard,MapData md,PacWindow pw){
        super(context);

        setFocusable(true);

        this.getHolder().addCallback(this);

        this.scoreboard = scoreboard;
//        this.setDoubleBuffered(true);
        md_backup = md;
        windowParent = pw;
        
        m_x = md.getX();
        m_y = md.getY();
        this.map = md.getMap();

        this.isCustom = md.isCustom();
        this.ghostBase = md.getGhostBasePosition();

        //loadMap();

        pacman = new Pacman(md.getPacmanPosition().x,md.getPacmanPosition().y,this);
//        addKeyListener(pacman);

//        camera = new Camera(pacman);
        ycamera = -pacman.pixelPosition.y+480;
        
        foods = new ArrayList<>();
        pufoods = new ArrayList<>();
        ghosts = new ArrayList<>();
        teleports = new ArrayList<>();

        //TODO : read food from mapData (Map 1)

        if(!isCustom) {
            for (int i = 0; i < m_x; i++) {
                for (int j = 0; j < m_y; j++) {
                    if (map[i][j] == 0)
                        foods.add(new Food(i, j));
                }
            }
        }else{
            foods = md.getFoodPositions();
        }



        pufoods = md.getPufoodPositions();

//        ghosts = new ArrayList<>();
        for(GhostData gd : md.getGhostsData()){
            switch(gd.getType()) {
                case RED:
                    ghosts.add(new RedGhost(gd.getX(), gd.getY(), this));
                    break;
                case PINK:
                    ghosts.add(new RedGhost(gd.getX(), gd.getY(), this));
                    break;
                case CYAN:
                    ghosts.add(new RedGhost(gd.getX(), gd.getY(), this));
                    break;
                case ORANGE:
                	ghosts.add(new RedGhost(gd.getX(), gd.getY(), this));
                	break;
            }
        }

        teleports = md.getTeleports();

//        setLayout(null);
//        setSize(20*m_x,20*m_y);
//        setBackground(Color.black);
        
//        try {
//        background = ImageIO.read(this.getClass().getResource("resources/images/background.png"));
//        }
//        catch(IOException e) {
//        	
//        }

        mapSegments = new Bitmap[28];
        mapSegments[0] = null;
        for(int ms=1;ms<28;ms++){
            try {
                mapSegments[ms] = BitmapFactory.decodeResource(this.getResources(),R.drawable.pac0);//("resources/images/map segments/"+ms+".png"));
            }catch(Exception e){}
        }


        pfoodImage = new Bitmap[5];
        for(int ms=0 ;ms<5;ms++){
            try {
                pfoodImage[ms] = BitmapFactory.decodeResource(this.getResources(),R.drawable.ghost3);//("resources/images/food/"+ms+".png"));
            }catch(Exception e){}
        }
        try{
            foodImage = BitmapFactory.decodeResource(this.getResources(),R.drawable.ghost3);;
            goImage = BitmapFactory.decodeResource(this.getResources(),R.drawable.ghost3);//ImageIO.read(this.getClass().getResource("resources/images/gameover.png"));
            vicImage = BitmapFactory.decodeResource(this.getResources(),R.drawable.ghost3);//ImageIO.read(this.getClass().getResource("resources/images/victory.png"));
            //pfoodImage = ImageIO.read(this.getClass().getResource("/images/pfood.png"));
        }catch(Exception e){}


//        redrawAL = new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                //Draw Board
//                repaint();
//            }
//        };
//        redrawTimer = new Timer(16){
//            public void actionPerformed(){
//                canvas = lock
//                draw(canvas);
//            }
//        };
//        redrawTimer .start();

        //SoundPlayer.play("pacman_start.wav");
        siren = new LoopPlayer("siren.wav");
        pac6 = new LoopPlayer("pac6.wav");
        siren.start();
    }

    private void collisionTest(){
        Rect pr = new Rect(pacman.pixelPosition.x+13,pacman.pixelPosition.y+13,2,2);
        Ghost ghostToRemove = null;
        for(Ghost g : ghosts){
            Rect gr = new Rect(g.pixelPosition.x,g.pixelPosition.y,28,28);

            if(pr.intersect(gr)){
                if(!g.isDead()) {
                    if (!g.isWeak()) {
                        //Game Over
                        siren.stop();
                        SoundPlayer.play("pacman_lose.wav");
                        pacman.moveTimer.stop();
                        pacman.animTimer.stop();
                        g.moveTimer.stop();
                        isGameOver = true;
                        scoreboard.setData("    Press R to try again !");
                        //scoreboard.setForeground(Color.red);
                        break;
                    } else {
                        //Eat Ghost
                        SoundPlayer.play("pacman_eatghost.wav");
                        //getGraphics().setFont(new Font("Arial",Font.BOLD,20));
                        drawScore = true;
                        scoreToAdd++;
                        if(ghostBase!=null)
                            g.die();
                        else
                            ghostToRemove = g;
                    }
                }
            }
        }

        if(ghostToRemove!= null){
            ghosts.remove(ghostToRemove);
        }
    }

    public void update(){
    	
        Food foodToEat = null;
        //Check food eat
        for(Food f : foods){
            if(pacman.logicalPosition.x == f.position.x && pacman.logicalPosition.y == f.position.y)
                foodToEat = f;
        }
        if(foodToEat!=null) {
            SoundPlayer.play("pacman_eat.wav");
            foods.remove(foodToEat);
            score ++;
            scoreboard.setData("    Score : "+score);

            if(foods.size() == 0){
                siren.stop();
                pac6.stop();
                SoundPlayer.play("pacman_intermission.wav");
                isWin = true;
                pacman.moveTimer.stop();
                for(Ghost g : ghosts){
                    g.moveTimer.stop();
                }
            }
        }

        PowerUpFood puFoodToEat = null;
        //Check pu food eat
        for(PowerUpFood puf : pufoods){
            if(pacman.logicalPosition.x == puf.position.x && pacman.logicalPosition.y == puf.position.y)
                puFoodToEat = puf;
        }
        if(puFoodToEat!=null) {
            //SoundPlayer.play("pacman_eat.wav");
            switch(puFoodToEat.type) {
                case 0:
                    //PACMAN 6
                    pufoods.remove(puFoodToEat);
                    siren.stop();
                    mustReactivateSiren = true;
                    pac6.start();
                    for (Ghost g : ghosts) {
                        g.weaken();
                    }
                    scoreToAdd = 0;
                    break;
                default:
                    SoundPlayer.play("pacman_eatfruit.wav");
                    pufoods.remove(puFoodToEat);
                    scoreToAdd = 1;
                    drawScore = true;
            }
            //score ++;
            //scoreboard.setText("    Score : "+score);
        }

        //Check Ghost Undie
        for(Ghost g:ghosts){
            if(g.isDead() && g.logicalPosition.x == ghostBase.x && g.logicalPosition.y == ghostBase.y){
                g.undie();
            }
        }

        //Check Teleport
        for(TeleportTunnel tp : teleports) {
            if (pacman.logicalPosition.x == tp.getFrom().x && pacman.logicalPosition.y == tp.getFrom().y && pacman.activeMove == tp.getReqMove()) {
                //System.out.println("TELE !");
                pacman.logicalPosition = tp.getTo();
                pacman.pixelPosition.x = pacman.logicalPosition.x * 28;
                pacman.pixelPosition.y = pacman.logicalPosition.y * 28;
            }
        }

        //Check isSiren
        boolean isSiren = true;
        for(Ghost g:ghosts){
            if(g.isWeak()){
                isSiren = false;
            }
        }
        if(isSiren){
            pac6.stop();
            if(mustReactivateSiren){
                mustReactivateSiren = false;
                siren.start();
            }

        }



    }

    @Override
    public void draw(Canvas g){
        super.draw(g);
        Paint p = new Paint();

        //DEBUG ONLY !
        /*for(int ii=0;ii<=m_x;ii++){
            g.drawLine(ii*28+10,10,ii*28+10,m_y*28+10);
        }
        for(int ii=0;ii<=m_y;ii++){
            g.drawLine(10,ii*28+10,m_x*28+10,ii*28+10);
        }*/

        //Draw Walls
//        g.setColor(Color.blue);
        for(int i=0;i<m_x;i++){
            for(int j=0;j<m_y;j++){
                if(map[i][j]>0){
                    //g.drawImage(10+i*28,10+j*28,28,28);
                    g.drawBitmap(mapSegments[map[i][j]],10+i*28,10+j*28+ycamera,null);
                }
            }
        }
//        g.drawImage(background, 6, ydraw, PacWindow.WIDTH-30, PacWindow.HEIGHT-55, null);
        

        
        //Draw Food
        p.setColor(Color.rgb(204, 122, 122));
        for(Food f : foods){
            //g.fillOval(f.position.x*28+22,f.position.y*28+22,4,4);
            g.drawBitmap(foodImage,10+f.position.x*28,10+f.position.y*28+ycamera,p);
        }

        //Draw PowerUpFoods
        p.setColor(Color.rgb(204, 174, 168));
        for(PowerUpFood f : pufoods){
            //g.fillOval(f.position.x*28+20,f.position.y*28+20,8,8);
            g.drawBitmap(pfoodImage[f.type],10+f.position.x*28,10+f.position.y*28+ycamera,null);
        }

        //Draw Pacman
        switch(pacman.activeMove){
            case NONE:
            case RIGHT:
                g.drawBitmap(pacman.getPacmanImage(),10+pacman.pixelPosition.x,10+pacman.pixelPosition.y+ycamera,null);
                break;
            case LEFT:
                g.drawBitmap(ImageHelper.flipHor(pacman.getPacmanImage()),10+pacman.pixelPosition.x,10+pacman.pixelPosition.y+ycamera,null);
                break;
            case DOWN:
                g.drawBitmap(ImageHelper.rotate90(pacman.getPacmanImage()),10+pacman.pixelPosition.x,10+pacman.pixelPosition.y+ycamera,null);
                break;
            case UP:
                g.drawBitmap(ImageHelper.flipVer(ImageHelper.rotate90(pacman.getPacmanImage())),10+pacman.pixelPosition.x,10+pacman.pixelPosition.y+ycamera,null);
                break;
        }
        //Draw Ghosts
        for(Ghost gh : ghosts){
            g.drawBitmap(gh.getGhostImage(),10+gh.pixelPosition.x,10+gh.pixelPosition.y+ycamera,null);
        }

        if(clearScore){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            drawScore = false;
            clearScore =false;
        }

        if(drawScore) {
            //System.out.println("must draw score !");
            //p.setFont(new Font("Arial",Font.BOLD,15));
            p.setColor(Color.YELLOW);
            Integer s = scoreToAdd*100;
            g.drawText(s.toString(), pacman.pixelPosition.x + 13, pacman.pixelPosition.y + 50,p);
            //drawScore = false;
            score += s;
            scoreboard.setData("    Score : "+score);
            clearScore = true;

        }

        if(isGameOver){
            g.drawBitmap(goImage,WIDTH/2-315,HEIGHT/2-75,null);
        }

        if(isWin){
            g.drawBitmap(vicImage,WIDTH/2-315,HEIGHT/2-75,null);
        }

        g.drawBitmap(mapSegments[map[0][14]],10,10+14*28+ycamera,null);
    	g.drawBitmap(mapSegments[map[26][14]],10+26*28,10+14*28+ycamera,null);
    	

    }

//    public void processEvent(AWTEvent ae){
//    	System.out.println("..............");
//
//        if(ae.getID()==Messeges.UPDATE) {
//            update();
//        }else if(ae.getID()==Messeges.COLTEST) {
//            if (!isGameOver) {
//                collisionTest();
//            }
//        }else if(ae.getID()==Messeges.RESET){
//            if(isGameOver)
//                restart();
//        }else {
//            super.processEvent(ae);
//        }
//    }
    
//    public void restart(){
//
//        siren.stop();
//
//        new PacWindow();
//        windowParent.dispose();
//
//        /*
//        removeKeyListener(pacman);
//
//        isGameOver = false;
//
//        pacman = new Pacman(md_backup.getPacmanPosition().x,md_backup.getPacmanPosition().y,this);
//        addKeyListener(pacman);
//
//        foods = new ArrayList<>();
//        pufoods = new ArrayList<>();
//        ghosts = new ArrayList<>();
//        teleports = new ArrayList<>();
//
//        //TODO : read food from mapData (Map 1)
//
//        if(!isCustom) {
//            for (int i = 0; i < m_x; i++) {
//                for (int j = 0; j < m_y; j++) {
//                    if (map[i][j] == 0)
//                        foods.add(new Food(i, j));
//                }
//            }
//        }else{
//            foods = md_backup.getFoodPositions();
//        }
//
//
//
//        pufoods = md_backup.getPufoodPositions();
//
//        ghosts = new ArrayList<>();
//        for(GhostData gd : md_backup.getGhostsData()){
//            switch(gd.getType()) {
//                case RED:
//                    ghosts.add(new RedGhost(gd.getX(), gd.getY(), this));
//                    break;
//                case PINK:
//                    ghosts.add(new PinkGhost(gd.getX(), gd.getY(), this));
//                    break;
//                case CYAN:
//                    ghosts.add(new CyanGhost(gd.getX(), gd.getY(), this));
//                    break;
//            }
//        }
//
//        teleports = md_backup.getTeleports();
//        */
//    }

    public void setDraw(int x) {
    	ycamera=x;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        GameThread thread = new GameThread(this,holder);
        thread.setRunning(true);
        thread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
