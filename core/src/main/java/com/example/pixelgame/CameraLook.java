package com.example.pixelgame;

/** Delayed vertical look input; independent of the player's movement and exploration state. */
final class CameraLook {
    static final float HOLD_SECONDS = .65f;
    static final float UP_DISTANCE = 72f, DOWN_DISTANCE = 148f;
    static float holdSeconds(){return GameSettings.current.lookDelay();}
    private int direction;
    private float heldSeconds, offset;

    float update(float delta, boolean up, boolean down, boolean enabled) {
        return update(delta,up,down,enabled,false);
    }
    float update(float delta,boolean up,boolean down,boolean enabled,boolean downReady){
        float dt = Math.max(0, delta);
        int requested = enabled && up != down ? (up ? 1 : -1) : 0;
        if (requested != direction) {
            direction = requested;
            heldSeconds = 0;
        }
        if(direction==-1&&downReady)heldSeconds=holdSeconds();
        else if (direction != 0) heldSeconds = Math.min(holdSeconds(), heldSeconds + dt);
        else heldSeconds = 0;
        float target = direction != 0 && heldSeconds + .00001f >= holdSeconds()
                ? (direction > 0 ? UP_DISTANCE : -DOWN_DISTANCE)*GameSettings.current.lookRange() : 0;
        if(GameSettings.current.on(GameSettings.Option.SMOOTH_CAMERA))offset += (target - offset) * (1 - (float)Math.exp(-8 * dt));
        else offset=target;
        if (Math.abs(target - offset) < .01f) offset = target;
        return offset;
    }

    void cancelHold() { direction = 0; heldSeconds = 0; }
    void reset() { cancelHold(); offset = 0; }
}
