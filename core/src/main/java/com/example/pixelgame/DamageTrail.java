package com.example.pixelgame;

/** Damage remains visible briefly, then drains at a fixed rate without changing real health. */
final class DamageTrail {
    private float value=1,target=1,delay;
    void reset(float ratio){value=target=clamp(ratio);delay=0;}
    void health(float ratio){
        ratio=clamp(ratio);
        if(ratio<target)delay=.5f;
        else if(ratio>target){value=ratio;delay=0;}
        target=ratio;
    }
    void update(float dt,float ratio){
        health(ratio);float remaining=Math.max(0,dt);
        float waiting=Math.min(delay,remaining);delay-=waiting;remaining-=waiting;
        value=Math.max(target,value-remaining*.32f);
    }
    float value(){return GameSettings.current.on(GameSettings.Option.DAMAGE_TRAIL)?value:target;}
    private float clamp(float ratio){return Math.max(0,Math.min(1,ratio));}
}
