package com.example.pixelgame;

/** A release before the threshold is a tap; a completed hold can never become a tap later. */
final class DownGesture {
    private boolean active,lastHeld,awaitRelease;
    private float seconds;
    private Object startSupport;
    boolean update(float dt,boolean pressed,boolean held,boolean allowed,Object support){
        if(awaitRelease){lastHeld=held;if(!held)awaitRelease=false;return false;}
        if(!allowed){cancel(held);return false;}
        if(!lastHeld&&(pressed||held)){active=true;seconds=0;startSupport=support;}
        lastHeld=held;
        if(!active)return false;
        if(held){seconds=Math.min(CameraLook.holdSeconds(),seconds+Math.max(0,dt));return false;}
        boolean tap=seconds+.00001f<CameraLook.holdSeconds()&&support!=null&&support==startSupport;
        active=false;seconds=0;startSupport=null;
        return tap;
    }
    boolean preparing(){return active&&lastHeld;}
    boolean looking(){return preparing()&&seconds+.00001f>=CameraLook.holdSeconds();}
    boolean releasingTap(boolean held,Object support){
        return active&&!held&&seconds+.00001f<CameraLook.holdSeconds()&&support!=null&&support==startSupport;
    }
    void cancel(boolean held){active=false;seconds=0;startSupport=null;lastHeld=held;awaitRelease=held;}
}
