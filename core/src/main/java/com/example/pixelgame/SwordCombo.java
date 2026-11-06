package com.example.pixelgame;

/** Three committed swings, one buffered follow-up, and a frame-rate-independent finisher step. */
final class SwordCombo {
    private static final float[] DURATION={.24f,.26f,.36f},START={.055f,.065f,.105f},END={.155f,.18f,.235f};
    private boolean active,queued;
    private int step;
    private float elapsed,lunge;
    void start(){active=true;queued=false;step=1;elapsed=lunge=0;}
    void cancel(){active=queued=false;step=0;elapsed=lunge=0;}
    void clearQueue(){queued=false;}
    boolean active(){return active;}
    int step(){return active?step:0;}
    float elapsed(){return elapsed;}
    float duration(){return active?DURATION[step-1]:0;}
    boolean damaging(){return active&&elapsed>=START[step-1]&&elapsed<END[step-1];}
    boolean canCancel(){return !active||step<3||elapsed>=.30f;}
    float lunge(){return lunge;}
    float strikeProgress(){return damaging()?(elapsed-START[step-1])/(END[step-1]-START[step-1]):0;}
    int pose(){return active?(step-1)*3+(elapsed<START[step-1]?0:elapsed<END[step-1]?1:2):0;}
    int update(float dt,boolean press){
        lunge=0;if(!active)return 0;
        if(press&&step<3)queued=true;
        int started=0;
        for(float remaining=Math.max(0,dt);remaining>.000001f&&active;){
            float used=Math.min(remaining,DURATION[step-1]-elapsed),old=elapsed;
            elapsed+=used;remaining-=used;
            if(step==3)lunge+=20f*Math.max(0,Math.min(elapsed,.225f)-Math.max(old,.075f))/.15f;
            if(elapsed>=DURATION[step-1]-.000001f){
                if(queued&&step<3){step++;elapsed=0;queued=false;started++;}
                else{active=false;step=0;queued=false;}
            }
        }
        return started;
    }
}
