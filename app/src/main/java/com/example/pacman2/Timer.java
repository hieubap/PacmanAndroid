package com.example.pacman2;

public abstract class Timer extends Thread{
    int delay;
    public Timer(int delay){
       this.delay = delay;

    }
    public void run(){
        while(true){
            actionPerformed();
            try{
                this.sleep(delay);
            }
            catch (InterruptedException e){

            }
        }
    }

    public abstract void actionPerformed();
}
