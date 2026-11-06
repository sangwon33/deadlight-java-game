package com.example.pixelgame;

import java.util.ArrayList;
import java.util.IdentityHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    private float walkDistance;
    private java.util.List<Rectangle> worldSolids;
    private Rectangle support,ignoredPlatform;
    private float dropTimer,worldBottom=-Float.MAX_VALUE,worldTop=Float.MAX_VALUE,safeX,safeY;
    private boolean crouching,screenDown,pendingDown;
    private int downCount;
    public boolean isCrouching(){return crouching;}
    public boolean isGrounded(){return onGround;}
    public int getDownCount(){return downCount;}
    public Rectangle getSupport(){return support;}
    public float getVelocityY(){return velocityY;}
    public void configureWorld(java.util.List<Rectangle> solids,float bottom,float top){worldSolids=solids;worldBottom=bottom;worldTop=top;safeX=x;safeY=y;}
    public void setScreenDown(boolean held,boolean pressed){screenDown=held;pendingDown|=pressed;}
    public boolean wantsDown(){return KeyBindings.justPressed(KeyBindings.Action.DOWN)||pendingDown;}
    public void carry(float dx,float dy){x+=dx;y+=dy;}
    public void launch(float speed){if(!alive)return;velocityY=speed;onGround=false;support=null;crouching=false;jumpWasHeld=false;airDashUsed=false;coyoteTimer=0;}
    private float bodyHeight(){return crouching?32f:height;}
    private boolean canStand(){if(worldSolids==null)return true;Rectangle standing=new Rectangle(x,y,width,height);for(Rectangle r:worldSolids)if(r.width>0&&standing.overlaps(r))return false;return true;}

    private int jumpCount, attackCount, dashCount;
    public float getWalkDistance() { return walkDistance; }
    public int getJumpCount() { return jumpCount; }
    public int getAttackCount() { return attackCount; }
    public int getDashCount() { return dashCount; }
    private final IdentityHashMap<TextureRegion, Float> footRatios = new IdentityHashMap<>();
    private boolean screenLeft, screenRight;
    private boolean pendingJump, pendingAttack, pendingDash;
    private boolean pendingSkill, skillInput, usingSkill;
    private boolean chargingSkill, screenSkillHeld;
    private float skillChargeTime;
    private int skillChargeStage = 1, completedFullChargeSkills;
    private ThrustSprites thrustSprites;
    private float skillSweepStartX;
    private boolean skillRushThisFrame;
    private float skillElapsed, skillCooldown;
    private int skillCount, completedSkillCount, interruptedSkillCount;

    public float getSkillCooldown() { return skillCooldown; }
    public float getSkillElapsed() { return skillElapsed; }
    public int getSkillCount() { return skillCount; }
    public int getCompletedSkillCount() { return completedSkillCount; }
    public int getInterruptedSkillCount() { return interruptedSkillCount; }
    public boolean isUsingSkill() { return usingSkill; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isStunned() { return hitStunTimer > 0; }
    public boolean canUseSkill() { return alive && onGround && !crouching && !usingSkill && !attacking && !dashing && hitStunTimer <= 0 && skillCooldown <= 0; }
    public boolean isChargingSkill() { return usingSkill && chargingSkill; }
    public float getSkillChargeProgress() { return Math.min(1, skillChargeTime / GameBalance.SKILL_FULL_CHARGE); }
    public int getSkillChargeStage() { return skillChargeStage; }
    public int getCompletedFullChargeSkillCount() { return completedFullChargeSkills; }
    public boolean isSkillActive() { return usingSkill && !chargingSkill && skillElapsed >= GameBalance.SKILL_WINDUP
            && (skillElapsed <= GameBalance.SKILL_STRIKE_END + .0001f || skillRushThisFrame); }
    public int getSkillDamage() { return GameBalance.SKILL_DAMAGE; }
    public String getSkillPhase() { return chargingSkill ? "CHARGING" : skillElapsed < GameBalance.SKILL_WINDUP ? "WINDUP" : isSkillActive() ? "STRIKE" : "RECOVER"; }
    public float getSkillSweepStartX() { return skillSweepStartX; }
    public Rectangle getSkillBounds() {
        // Sweep the blade from the previous to current position; fast movement must not skip targets.
        float left = Math.min(skillSweepStartX, getCenterX());
        float travel = Math.abs(getCenterX() - skillSweepStartX);
        return new Rectangle(facingRight ? left : left - GameBalance.SKILL_REACH,
                y + GameBalance.character(30), travel + GameBalance.SKILL_REACH, GameBalance.character(24));
    }

    public void setScreenInput(boolean left, boolean right, boolean jump, boolean attack, boolean dash) {
        setScreenInput(left, right, jump, attack, dash, false);
    }
    public void setScreenInput(boolean left, boolean right, boolean jump, boolean attack, boolean dash, boolean skill) {
        setScreenInput(left, right, jump, attack, dash, skill, skill);
    }
    public void setScreenInput(boolean left, boolean right, boolean jump, boolean attack, boolean dash, boolean skill, boolean skillHeld) {
        screenLeft = left; screenRight = right;
        screenJumpHeld = false; screenDown=false;
        pendingJump |= jump; pendingAttack |= attack; pendingDash |= dash;
        pendingSkill |= skill;
        screenSkillHeld = skillHeld;
    }
    public float getDashCooldownTimer() { return dashCooldownTimer; }
    public void clearActionInput() {
        screenLeft = screenRight = screenDown = pendingDown = false;
        pendingJump = pendingAttack = pendingDash = false;
        jumpInput = attackInput = dashInput = false;
        pendingSkill = skillInput = false;
        screenSkillHeld = screenJumpHeld = jumpWasHeld = false;
        jumpBufferTimer = attackBufferTimer = coyoteTimer = 0;
        if(swordCombo!=null)swordCombo.clearQueue();
        // A menu or focus change must not release a stored attack when play resumes.
        if (chargingSkill) { chargingSkill = usingSkill = false; skillChargeTime = skillElapsed = 0; }
    }
    private boolean jumpInput, attackInput, dashInput;
    private boolean screenJumpHeld, jumpWasHeld, airDashUsed;
    private float coyoteTimer, jumpBufferTimer, attackBufferTimer;
    public void setScreenJumpHeld(boolean held) { screenJumpHeld = held; }
    public boolean canDash() {
        return alive && !crouching && hitStunTimer <= 0 && !usingSkill && !dashing && dashCooldownTimer <= 0
                && (onGround || !airDashUsed) && (!groundCombo||swordCombo==null||swordCombo.canCancel());
    }

    public void captureActionInput() {
        pendingJump |= KeyBindings.justPressed(KeyBindings.Action.JUMP);
        pendingAttack |= KeyBindings.justPressed(KeyBindings.Action.ATTACK);
        pendingDash |= KeyBindings.justPressed(KeyBindings.Action.DASH);
        pendingSkill |= KeyBindings.justPressed(KeyBindings.Action.SKILL);
    }


    // 기본 이미지
    private MoonAtlas moonArt;
    private float moonTime;
    private Texture idleTexture;
    private TextureRegion idleRegion;

    // 달리기 애니메이션
    private Texture runTexture;
    private TextureRegion[] runFrames;
    private Animation<TextureRegion> runAnimation;
    private float runStateTime = 0;

    // 점프 이미지
    private Texture jumpTexture;
    private TextureRegion[] jumpFrames;

    // 공격 애니메이션
    private Texture attackTexture;
    private TextureRegion[] attackFrames;
    private Animation<TextureRegion> attackAnimation;
    private float attackStateTime = 0;
    private SwordCombo swordCombo;
    private SwordComboSprites comboSprites;
    private boolean groundCombo;
    public int getComboStep(){return groundCombo&&swordCombo!=null?swordCombo.step():0;}
    public boolean isComboFinisher(){return getComboStep()==3;}

    // 대시 애니메이션
    private Texture dashTexture;
    private TextureRegion[] dashFrames;
    private Animation<TextureRegion> dashAnimation;
    private float dashStateTime = 0;

    // 캐릭터 위치
    private float x;
    private float y;

    // 실제 충돌 판정 크기
    private final float width = GameBalance.character(64);
    private final float height = GameBalance.character(64);

    // 평상시 이미지 출력
    private final float idleDrawWidth = 130;
    private final float idleDrawHeight = 130;
    private final float idleFootOffset = 21;

    // 달리기 이미지 출력
    private final float runDrawWidth = 120;
    private final float runDrawHeight = 80;
    private final float runFootOffset = 2;

    // 점프 이미지 출력
    private final float jumpDrawWidth = 120;
    private final float jumpDrawHeight = 80;
    private final float jumpFootOffset = 2;

    // 공격 이미지 출력
    private final float attackDrawWidth = 216.0f;
    private final float attackDrawHeight = 144.0f;
    private final float attackFootOffset = 39.75f;

    // 대시 이미지 출력
    private final float dashDrawWidth = 120;
    private final float dashDrawHeight = 80;
    private final float dashFootOffset = 2;

    // 이동
    private final float moveSpeed = GameBalance.PLAYER_MOVE_SPEED;
    private final float jumpPower = GameBalance.JUMP_SPEED;
    private final float gravity = GameBalance.GRAVITY;
    private final float groundY = 60;

    private float velocityY;
    private boolean onGround = true;
    private boolean moving = false;
    private boolean facingRight = true;

    // 공격
    private boolean attacking = false;
    private float attackTimer = 0;
    private final float attackDuration = 0.24f;

    private final float attackWidth = GameBalance.character(45);
    private final float attackHeight = GameBalance.character(38);
    private final float attackHitboxInset = GameBalance.character(12);

    // 대시
    private boolean dashing = false;
    private float dashTimer = 0;
    private float dashCooldownTimer = 0;
    private float dashDirection = 1;

    private final float dashSpeed = GameBalance.PLAYER_DASH_SPEED;
    private final float dashDuration = 0.14f;
    private final float dashCooldown = GameBalance.DASH_COOLDOWN;

    // 체력
    private int maxHealth = GameBalance.BASE_HEALTH;
    private int health = maxHealth;
    private boolean alive = true;
    private int attackUpgradeLevel = 0;
    private float attackPotionTimer = 0f;

    // 피격 후 무적 시간
    private float invincibleTimer = 0;
    private final float invincibleDuration = 0.8f;

    // 피격 시 붉은색 효과
    private float hitFlashTimer = 0;
    private final float hitFlashDuration = 0.15f;

    // 피격 중 조작 제한 시간
    private float hitStunTimer = 0;
    private final float hitStunDuration = 0.18f;

    // 피격 넉백
    private float knockbackVelocityX = 0;
    private final float knockbackSpeed = 320;
    private final float knockbackFriction = 1000;

    public Player(float startX, float startY) {

        x = startX;
        y = startY;

        moonArt = MoonAtlas.acquire("art/hero.png",4,4);
        loadIdleTexture();
        loadRunAnimation();
        loadJumpFrames();
        loadAttackAnimation();
        loadDashAnimation();
        thrustSprites = new ThrustSprites();
        for (TextureRegion frame : thrustSprites.frames) footRatios.put(frame,ThrustSprites.FOOT_RATIO);
        registerFootAnchors("player/player_idle.png", idleRegion);
        registerFootAnchors("player/player_run.png", runFrames);
        registerFootAnchors("player/player_jump.png", jumpFrames);
        registerFootAnchors("player/player_attack.png", attackFrames);
        registerFootAnchors("player/player_dash.png", dashFrames);
    }

    private void registerFootAnchors(String asset, TextureRegion... regions) {
        Pixmap pixels = new Pixmap(Gdx.files.internal(asset));
        try {
            for (TextureRegion region : regions) {
                int bottom = region.getRegionHeight();
                for (int row = region.getRegionHeight() - 1; row >= 0; row--) {
                    int solid = 0;
                    for (int col = 0; col < region.getRegionWidth(); col++) {
                        if ((pixels.getPixel(region.getRegionX() + col, region.getRegionY() + row) & 255) > 96) solid++;
                    }
                    if (solid >= 3) { bottom = row + 1; break; }
                }
                footRatios.put(region, (region.getRegionHeight() - bottom) / (float)region.getRegionHeight());
            }
        } finally { pixels.dispose(); }
    }

    private void loadIdleTexture() {

        idleTexture = new Texture(
                "player/player_idle.png"
        );

        idleTexture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );

        idleRegion = new TextureRegion(idleTexture);
    }

    private void loadRunAnimation() {

        runTexture = new Texture(
                "player/player_run.png"
        );

        runTexture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );

        TextureRegion[][] splitFrames =
                TextureRegion.split(
                        runTexture,
                        192,
                        128
                );

        runFrames = new TextureRegion[6];

        for (int i = 0; i < runFrames.length; i++) {
            runFrames[i] = splitFrames[0][i];
        }

        runAnimation = new Animation<>(
                0.09f,
                runFrames
        );

        runAnimation.setPlayMode(
                Animation.PlayMode.LOOP
        );
    }

    private void loadJumpFrames() {

        jumpTexture = new Texture(
                "player/player_jump.png"
        );

        jumpTexture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );

        TextureRegion[][] splitFrames =
                TextureRegion.split(
                        jumpTexture,
                        192,
                        128
                );

        jumpFrames = new TextureRegion[2];

        // 0번: 상승, 1번: 하강
        jumpFrames[0] = splitFrames[0][0];
        jumpFrames[1] = splitFrames[0][1];
    }

    private void loadAttackAnimation() {

        attackTexture = new Texture(
                "player/player_attack.png"
        );

        attackTexture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );

        TextureRegion[][] splitFrames =
                TextureRegion.split(
                        attackTexture,
                        attackTexture.getWidth() / 5,
                        attackTexture.getHeight()
                );

        attackFrames = new TextureRegion[5];

        for (int i = 0; i < attackFrames.length; i++) {
            attackFrames[i] = splitFrames[0][i];
        }

        attackAnimation = new Animation<>(
                attackDuration / attackFrames.length,
                attackFrames
        );

        attackAnimation.setPlayMode(
                Animation.PlayMode.NORMAL
        );
    }

    private void loadDashAnimation() {

        dashTexture = new Texture(
                "player/player_dash.png"
        );

        dashTexture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );

        TextureRegion[][] splitFrames =
                TextureRegion.split(
                        dashTexture,
                        192,
                        128
                );

        dashFrames = new TextureRegion[4];

        for (int i = 0; i < dashFrames.length; i++) {
            dashFrames[i] = splitFrames[0][i];
        }

        dashAnimation = new Animation<>(
                dashDuration / dashFrames.length,
                dashFrames
        );

        dashAnimation.setPlayMode(
                Animation.PlayMode.NORMAL
        );
    }

    public void update(float deltaTime,ArrayList<Rectangle> platforms,float worldWidth) {
        moonTime+=deltaTime;captureActionInput();
        jumpInput=pendingJump;attackInput=pendingAttack;dashInput=pendingDash;
        pendingJump=pendingAttack=pendingDash=false;skillInput=pendingSkill;pendingSkill=false;
        skillCooldown=Math.max(0,skillCooldown-deltaTime);if(skillCooldown<.0001f)skillCooldown=0;
        dropTimer=Math.max(0,dropTimer-deltaTime);if(dropTimer==0)ignoredPlatform=null;
        jumpBufferTimer=Math.max(0,jumpBufferTimer-deltaTime);attackBufferTimer=Math.max(0,attackBufferTimer-deltaTime);
        if(jumpInput&&!usingSkill)jumpBufferTimer=GameBalance.INPUT_BUFFER;
        if(attackInput&&!usingSkill)attackBufferTimer=GameBalance.INPUT_BUFFER;
        coyoteTimer=onGround?GameBalance.COYOTE_TIME:Math.max(0,coyoteTimer-deltaTime);
        if(onGround)airDashUsed=false;
        boolean down=KeyBindings.pressed(KeyBindings.Action.DOWN)||screenDown;
        boolean downEdge=KeyBindings.justPressed(KeyBindings.Action.DOWN)||pendingDown;pendingDown=false;
        if(downEdge&&onGround&&support!=null&&platforms.contains(support)&&!usingSkill&&!dashing){
            ignoredPlatform=support;dropTimer=.25f;support=null;y-=2;velocityY=-100;onGround=false;coyoteTimer=jumpBufferTimer=0;downCount++;
        }
        boolean wanted=down&&onGround&&!usingSkill&&!dashing&&!attacking;
        if(wanted&&!crouching)downCount++;
        if(wanted||crouching&&!canStand())crouching=true;else crouching=false;
        boolean jumpHeld=KeyBindings.pressed(KeyBindings.Action.JUMP)||screenJumpHeld;
        if(velocityY>0&&jumpWasHeld&&!jumpHeld&&!dashing&&!usingSkill)velocityY*=GameBalance.JUMP_CUT;
        jumpWasHeld=jumpHeld;
        attackPotionTimer=Math.max(0,attackPotionTimer-deltaTime);
        updateInvincibility(deltaTime);updateHitFlash(deltaTime);updateDashCooldown(deltaTime);moving=false;
        if(!alive){cancelNormalAttack();dashing=usingSkill=false;return;}
        if(dashing&&jumpBufferTimer>0&&(onGround||coyoteTimer>0)){dashing=false;dashTimer=dashStateTime=0;}
        float previousX=x,previousY=y;boolean freezeY=false,locked=false;
        if(hitStunTimer>0){jumpBufferTimer=attackBufferTimer=0;updateHitStun(deltaTime);locked=true;}
        else if(updateSkill(deltaTime)){jumpBufferTimer=attackBufferTimer=0;locked=true;}
        else if(updateDash(deltaTime)){freezeY=true;runStateTime=0;locked=true;}
        else{
            if(!crouching)updateAttack(deltaTime);else attackBufferTimer=jumpBufferTimer=0;
            moveHorizontal(deltaTime);jump();
        }
        resolveHorizontal(previousX);
        x=MathUtils.clamp(x,0,worldWidth-width);
        if(!locked)walkDistance+=Math.abs(x-previousX);
        if(!freezeY)applyGravity(deltaTime);else onGround=false;
        support=null;
        resolveSolids(previousY);
        checkPlatformCollision(platforms,previousY);
        if(y+bodyHeight()>worldTop){y=worldTop-bodyHeight();velocityY=Math.min(0,velocityY);}
        if(onGround&&jumpBufferTimer>0&&!locked&&!crouching)jump();
        if(!locked)updateRunAnimation(deltaTime);
        if(y<worldBottom-80){takeDamage(GameBalance.ENEMY_DAMAGE,x-100);x=safeX;y=safeY;velocityY=0;hitStunTimer=0;knockbackVelocityX=0;support=null;onGround=false;}
    }

    private void resolveHorizontal(float oldX){
        if(worldSolids==null)return;
        for(Rectangle r:worldSolids){
            if(r.width<=0||y>=r.y+r.height-.01f||y+bodyHeight()<=r.y+.01f)continue;
            if(x>oldX&&oldX+width<=r.x+.01f&&x+width>r.x)x=Math.min(x,r.x-width);
            else if(x<oldX&&oldX>=r.x+r.width-.01f&&x<r.x+r.width)x=Math.max(x,r.x+r.width);
        }
    }
    private void resolveSolids(float previousY){
        if(worldSolids==null){if(y<=groundY){y=groundY;velocityY=0;onGround=true;airDashUsed=false;}return;}
        float landing=Float.NEGATIVE_INFINITY,ceiling=Float.POSITIVE_INFINITY;
        for(Rectangle r:worldSolids){
            if(r.width<=0||x>=r.x+r.width||x+width<=r.x)continue;
            float top=r.y+r.height;
            if(velocityY<=0&&previousY>=top-.01f&&y<=top+.01f)landing=Math.max(landing,top);
            if(velocityY>0&&previousY+bodyHeight()<=r.y+.01f&&y+bodyHeight()>=r.y)ceiling=Math.min(ceiling,r.y);
        }
        if(Float.isFinite(landing)){y=landing;velocityY=0;onGround=true;airDashUsed=false;safeX=x;safeY=y;}
        else if(Float.isFinite(ceiling)){y=ceiling-bodyHeight();velocityY=0;}
    }

    private void moveHorizontal(float deltaTime) {

        if (KeyBindings.pressed(KeyBindings.Action.LEFT) || screenLeft) {

            x -= moveSpeed * (crouching?.48f:groundCombo?(isComboFinisher()?0:.65f):1f) * deltaTime;


            moving = true;

            if(!groundCombo)faceLeft();
        }

        if (KeyBindings.pressed(KeyBindings.Action.RIGHT) || screenRight) {

            x += moveSpeed * (crouching?.48f:groundCombo?(isComboFinisher()?0:.65f):1f) * deltaTime;


            moving = true;

            if(!groundCombo)faceRight();
        }
    }

    private boolean updateSkill(float deltaTime) {
        skillSweepStartX = getCenterX();
        skillRushThisFrame = false;
        float direction = ((KeyBindings.pressed(KeyBindings.Action.RIGHT) || screenRight) ? 1 : 0)
                - ((KeyBindings.pressed(KeyBindings.Action.LEFT) || screenLeft) ? 1 : 0);
        if (skillInput && canUseSkill()) {
            if (direction > 0) faceRight(); else if (direction < 0) faceLeft();
            usingSkill = chargingSkill = true;
            skillElapsed = skillChargeTime = 0; skillChargeStage = 1; skillCount++;
        }
        if (!usingSkill) return false;
        if (chargingSkill) {
            if (!(KeyBindings.pressed(KeyBindings.Action.SKILL) || screenSkillHeld)) {
                chargingSkill = usingSkill = false; skillChargeTime = skillElapsed = 0;
                return true;
            }
            skillChargeTime = Math.min(GameBalance.SKILL_FULL_CHARGE, skillChargeTime + deltaTime);
            skillChargeStage = GameBalance.chargeStage(skillChargeTime);
            if (skillChargeTime < GameBalance.SKILL_FULL_CHARGE - .0001f) return true;
            // A full focus triggers the physical forward thrust automatically.
            chargingSkill = false; skillElapsed = 0;
            skillCooldown = GameBalance.SKILL_COOLDOWN;
        }
        float previousElapsed = skillElapsed;
        skillElapsed += deltaTime;
        float rushDelta = Math.max(0, Math.min(skillElapsed, GameBalance.SKILL_RUSH_END)
                - Math.min(previousElapsed, GameBalance.SKILL_RUSH_END));
        skillRushThisFrame = rushDelta > 0;
        x += (facingRight ? 1 : -1) * GameBalance.SKILL_RUSH_SPEED * rushDelta;
        if (skillElapsed >= GameBalance.SKILL_DURATION) {
            usingSkill = false; skillElapsed = 0; completedSkillCount++;
            if (skillChargeStage == 4) completedFullChargeSkills++;
        }
        return true;
    }

    private void updateRunAnimation(
            float deltaTime) {

        if (moving
                && onGround
                && !attacking
                && !dashing) {

            runStateTime += deltaTime * (moveSpeed / 200f) / GameBalance.CHARACTER_SCALE;

        } else {

            runStateTime = 0;
        }
    }

    private void faceLeft() {

        if (facingRight) {

            flipAllFrames();

            facingRight = false;
        }
    }

    private void faceRight() {

        if (!facingRight) {

            flipAllFrames();

            facingRight = true;
        }
    }

    private void flipAllFrames() {
        for (TextureRegion frame : thrustSprites.frames) frame.flip(true, false);

        idleRegion.flip(true, false);

        for (TextureRegion frame : runFrames) {
            frame.flip(true, false);
        }

        for (TextureRegion frame : jumpFrames) {
            frame.flip(true, false);
        }

        for (TextureRegion frame : attackFrames) {
            frame.flip(true, false);
        }

        for (TextureRegion frame : dashFrames) {
            frame.flip(true, false);
        }
    }

    private void jump() {
        if (!crouching && (onGround || coyoteTimer > 0) && jumpBufferTimer > 0) {
            velocityY = jumpPower;
            onGround = false; coyoteTimer = jumpBufferTimer = 0;
            jumpWasHeld = true; jumpCount++;
        }
    }

    private void updateAttack(float deltaTime) {
        if(groundCombo){
            if(!onGround){cancelNormalAttack();return;}
            int started=swordCombo.update(deltaTime,attackInput);attackBufferTimer=0;
            if(started>0){attackCount+=started;faceAttackInput();}
            x+=(facingRight?1:-1)*swordCombo.lunge();
            attacking=swordCombo.active();attackStateTime=swordCombo.elapsed();attackTimer=swordCombo.duration()-attackStateTime;
            if(!attacking){groundCombo=false;attackStateTime=attackTimer=0;}
            return;
        }
        if (!attacking && !dashing && attackBufferTimer > 0) {
            faceAttackInput();
            if(onGround&&jumpBufferTimer<=0){
                if(swordCombo==null)swordCombo=new SwordCombo();swordCombo.start();groundCombo=attacking=true;attackCount++;attackBufferTimer=0;
                swordCombo.update(deltaTime,false);attackStateTime=swordCombo.elapsed();attackTimer=swordCombo.duration()-attackStateTime;return;
            }
            attacking = true; attackCount++; attackBufferTimer = 0;
            attackTimer = attackDuration; attackStateTime = 0;
        }
        if (attacking) {
            attackTimer -= deltaTime; attackStateTime += deltaTime;
            if (attackTimer <= 0) { attacking = false; attackTimer = attackStateTime = 0; }
        }
    }
    private void faceAttackInput(){
        boolean left=KeyBindings.pressed(KeyBindings.Action.LEFT)||screenLeft,right=KeyBindings.pressed(KeyBindings.Action.RIGHT)||screenRight;
        if(left!=right){if(right)faceRight();else faceLeft();}
    }
    private void cancelNormalAttack(){
        if(swordCombo!=null)swordCombo.cancel();groundCombo=false;attacking=false;attackTimer=attackStateTime=attackBufferTimer=0;
    }

    private void updateDashCooldown(
            float deltaTime) {

        if (dashCooldownTimer > 0) {

            dashCooldownTimer -= deltaTime;

            if (dashCooldownTimer < .0001f) {
                dashCooldownTimer = 0;
            }
        }
    }

    private boolean updateDash(
            float deltaTime) {

        boolean shiftPressed = dashInput;

        // 대시 시작
        if (shiftPressed && canDash()) {
            boolean left = KeyBindings.pressed(KeyBindings.Action.LEFT) || screenLeft;
            boolean right = KeyBindings.pressed(KeyBindings.Action.RIGHT) || screenRight;
            if (left != right) { if (right) faceRight(); else faceLeft(); }

            if (!onGround) airDashUsed = true;
            cancelNormalAttack();
            dashing = true;
            dashCount++;


            dashCooldownTimer = dashCooldown;
            dashTimer = dashDuration;
            dashStateTime = 0;

            dashDirection =
                    facingRight ? 1 : -1;

            // 대시 중에는 높이가 변하지 않는다
            velocityY = 0;
        }

        if (!dashing) {
            return false;
        }

        float activeDelta = Math.min(deltaTime, dashTimer);
        dashStateTime += activeDelta;
        x += dashDirection * dashSpeed * activeDelta;

        dashTimer -= deltaTime;

        if (dashTimer <= 0) {

            dashing = false;

            dashTimer = 0;
            dashStateTime = 0;


        }

        return true;
    }

    private void updateInvincibility(
            float deltaTime) {

        if (invincibleTimer > 0) {

            invincibleTimer -= deltaTime;

            if (invincibleTimer < 0) {
                invincibleTimer = 0;
            }
        }
    }

    private void updateHitFlash(
            float deltaTime) {

        if (hitFlashTimer > 0) {

            hitFlashTimer -= deltaTime;

            if (hitFlashTimer < 0) {
                hitFlashTimer = 0;
            }
        }
    }

    private void updateHitStun(
            float deltaTime) {

        hitStunTimer -= deltaTime;

        // 넉백 이동
        x += knockbackVelocityX * deltaTime;

        // 오른쪽으로 밀리는 중
        if (knockbackVelocityX > 0) {

            knockbackVelocityX -=
                    knockbackFriction * deltaTime;

            if (knockbackVelocityX < 0) {
                knockbackVelocityX = 0;
            }
        }

        // 왼쪽으로 밀리는 중
        else if (knockbackVelocityX < 0) {

            knockbackVelocityX +=
                    knockbackFriction * deltaTime;

            if (knockbackVelocityX > 0) {
                knockbackVelocityX = 0;
            }
        }

        if (hitStunTimer <= 0) {

            hitStunTimer = 0;
            knockbackVelocityX = 0;
        }
    }

    private void applyGravity(float deltaTime) {

        velocityY += gravity * deltaTime;
        y += velocityY * deltaTime;

        onGround = false;
    }

    private void checkPlatformCollision(ArrayList<Rectangle> platforms,float previousY) {
        if(velocityY>0)return;
        float landing=onGround?y:Float.NEGATIVE_INFINITY;Rectangle landed=null;
        for(Rectangle platform:platforms){
            if(platform==ignoredPlatform&&dropTimer>0)continue;
            float top=platform.y+platform.height;
            if(x<platform.x+platform.width&&x+width>platform.x&&previousY>=top-.01f&&y<=top+.01f&&top>=landing){
                landing=top;landed=platform;
            }
        }
        if(landed!=null){y=landing;velocityY=0;onGround=true;airDashUsed=false;support=landed;}
    }

    /*
     * attackerX는 공격한 슬라임의 중심 X 위치다.
     */
    public void takeDamage(
            int damage,
            float attackerX) {

        // 대시 중이거나 무적 상태라면 피해를 받지 않는다
        if (!alive
                || dashing
                || invincibleTimer > 0) {

            return;
        }

        health -= damage;

        invincibleTimer =
                invincibleDuration;

        hitFlashTimer =
                hitFlashDuration;

        hitStunTimer =
                hitStunDuration;

        // 공격과 이동 취소
        cancelNormalAttack();
        dashing = false;
        if (usingSkill) { interruptedSkillCount++; skillCooldown = GameBalance.SKILL_COOLDOWN; }
        usingSkill = chargingSkill = false; skillElapsed = skillChargeTime = 0;
        jumpBufferTimer = attackBufferTimer = coyoteTimer = 0;

        moving = false;

        attackTimer = 0;
        attackStateTime = 0;

        dashTimer = 0;
        dashStateTime = 0;

        /*
         * 슬라임 반대 방향으로 밀려난다.
         */
        if (getCenterX() < attackerX) {

            knockbackVelocityX =
                    -knockbackSpeed;

        } else {

            knockbackVelocityX =
                    knockbackSpeed;
        }

        System.out.println(
                "플레이어 체력: " + health
        );

        if (health <= 0) {

            health = 0;
            alive = false;

            knockbackVelocityX = 0;

            System.out.println(
                    "플레이어 사망!"
            );
        }
    }

    public void draw(SpriteBatch spriteBatch) {

        if (!alive) {
            return;
        }

        if(attacking&&groundCombo&&swordCombo!=null){
            if(comboSprites==null)comboSprites=new SwordComboSprites();
            float alpha=invincibleTimer>0&&hitFlashTimer<=0&&((int)(invincibleTimer*20)%2==0)?.3f:1;
            comboSprites.actor(spriteBatch,swordCombo,getCenterX(),y,facingRight,hitFlashTimer,alpha);return;
        }
        if (moonArt != null) { drawMoon(spriteBatch); return; }
        TextureRegion currentRegion;

        float currentDrawWidth;
        float currentDrawHeight;
        float currentFootOffset;

        if (usingSkill) {
            int pose = chargingSkill ? (skillChargeStage <= 2 ? 0 : 1)
                    : skillElapsed < .04f ? 2 : skillElapsed < .18f ? 3
                    : skillElapsed < .25f ? 4 : 5;
            currentRegion = thrustSprites.frames[pose];
            currentDrawWidth = thrustSprites.width(currentRegion); currentDrawHeight = thrustSprites.height(currentRegion);
            currentFootOffset = currentDrawHeight * ThrustSprites.FOOT_RATIO;
        }
        // Existing sprites are reused at their original aspect ratio.
        else if (dashing) {

            currentRegion =
                    dashAnimation.getKeyFrame(
                            dashStateTime,
                            false
                    );

            currentDrawWidth =
                    dashDrawWidth;

            currentDrawHeight =
                    dashDrawHeight;

            currentFootOffset =
                    dashFootOffset;
        }

        // 2순위: 공격
        else if (attacking) {

            currentRegion =
                    attackAnimation.getKeyFrame(
                            attackStateTime,
                            false
                    );

            currentDrawWidth =
                    attackDrawWidth;

            currentDrawHeight =
                    attackDrawHeight;

            currentFootOffset =
                    attackFootOffset;
        }

        // 3순위: 점프와 낙하
        else if (!onGround) {

            if (velocityY >= 0) {

                currentRegion =
                        jumpFrames[0];

            } else {

                currentRegion =
                        jumpFrames[1];
            }

            currentDrawWidth =
                    jumpDrawWidth;

            currentDrawHeight =
                    jumpDrawHeight;

            currentFootOffset =
                    jumpFootOffset;
        }

        // 4순위: 달리기
        else if (moving) {

            currentRegion =
                    runAnimation.getKeyFrame(
                            runStateTime,
                            true
                    );

            currentDrawWidth =
                    runDrawWidth;

            currentDrawHeight =
                    runDrawHeight;

            currentFootOffset =
                    runFootOffset;
        }

        // 5순위: 평상시
        else {

            currentRegion = idleRegion;

            currentDrawWidth =
                    idleDrawWidth;

            currentDrawHeight =
                    idleDrawHeight;

            currentFootOffset =
                    idleFootOffset;
        }

        /*
         * 무적 시간 동안 깜빡이는 효과
         */
        float alpha = 1f;

        if (invincibleTimer > 0 && hitFlashTimer <= 0) {

            int blinkNumber =
                    (int) (invincibleTimer * 16);

            if (blinkNumber % 2 == 0) {
                alpha = 0.30f;
            }
        }

        /*
         * 맞은 직후에는 붉은색으로 표시
         */
        if (hitFlashTimer > 0) {

            spriteBatch.setColor(
                    1f,
                    0.25f,
                    0.25f,
                    alpha
            );

        } else {

            spriteBatch.setColor(
                    1f,
                    1f,
                    1f,
                    alpha
            );
        }

        drawCentered(
                spriteBatch,
                currentRegion,
                currentDrawWidth,
                currentDrawHeight,
                currentFootOffset
        );

        // 다음 이미지에 색상이 적용되지 않도록 초기화
        spriteBatch.setColor(
                1f,
                1f,
                1f,
                1f
        );
    }

    private void drawMoon(SpriteBatch batch) {
        int frame = usingSkill ? (chargingSkill ? 14 : skillElapsed < .25f ? 15 : 11)
                : dashing ? 13 : attacking ? 8 + Math.min(3,(int)(attackStateTime / attackDuration * 4))
                : !onGround ? 12 : moving ? 4 + (int)(runStateTime / .09f) % 4 : (int)(moonTime / .28f) % 4;
        float packed=batch.getPackedColor();
        float alpha=invincibleTimer>0 && hitFlashTimer<=0 && ((int)(invincibleTimer*20)%2==0) ? .3f : 1;
        batch.setColor(1,hitFlashTimer>0?.35f:1,hitFlashTimer>0?.4f:1,alpha);
        if(crouching)moonArt.drawCrouch(batch,14,getCenterX(),y,GameBalance.character(78),32,facingRight);
        else moonArt.draw(batch,frame,0,getCenterX(),y,GameBalance.character(78),facingRight);
        batch.setPackedColor(packed);
    }
    public void drawAttackEffects(SpriteBatch batch){
        if(isAttackActive()&&groundCombo){if(comboSprites==null)comboSprites=new SwordComboSprites();comboSprites.effects(batch,swordCombo,getCenterX(),y,facingRight);}
    }

    private void drawCentered(
            SpriteBatch spriteBatch,
            TextureRegion currentRegion,
            float drawWidth,
            float drawHeight,
            float footOffset) {

        drawWidth = GameBalance.character(drawWidth);
        drawHeight = GameBalance.character(drawHeight);
        footOffset = GameBalance.character(footOffset);
        float drawX =
                x + width / 2
                - drawWidth / 2;

        Float footRatio = footRatios.get(currentRegion);
        float drawY = y - (footRatio == null ? footOffset : drawHeight * footRatio);

        spriteBatch.draw(
                currentRegion,
                drawX,
                drawY,
                drawWidth,
                drawHeight
        );
    }

    public Rectangle getBounds() {

        return new Rectangle(
                x,
                y,
                width,
                bodyHeight()
        );
    }

    public Rectangle getAttackBounds() {
        if(groundCombo&&swordCombo!=null&&swordCombo.active()){
            int step=swordCombo.step();float reach=GameBalance.character(step==1?45:step==2?40:60);
            float top=GameBalance.character(step==2?84:step==3?54:50),bottom=GameBalance.character(step==2?18:step==3?6:12);
            return new Rectangle(facingRight?x+width-attackHitboxInset:x-reach+attackHitboxInset,y+bottom,reach,top-bottom);
        }

        float attackX;

        if (facingRight) {

            attackX =
                    x + width
                    - attackHitboxInset;

        } else {

            attackX =
                    x - attackWidth
                    + attackHitboxInset;
        }

        return new Rectangle(
                attackX,
                y + GameBalance.character(12),
                attackWidth,
                attackHeight
        );
    }

    public float getX() {

        return x;
    }

    public float getY() {

        return y;
    }

    public float getCenterX() {

        return x + width / 2;
    }

    public float getHealthRatio() {

        return (float) health / maxHealth;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getAttackDamage() {
        return GameBalance.BASE_ATTACK_DAMAGE + attackUpgradeLevel + (attackPotionTimer > 0f ? GameBalance.POWER_BONUS : 0);
    }
    public int getCurrentAttackDamage(){return isComboFinisher()?Math.round(getAttackDamage()*1.4f):getAttackDamage();}

    public float getAttackPotionTimer() {
        return attackPotionTimer;
    }

    public void upgradeAttack() {
        attackUpgradeLevel++;
    }

    public void upgradeMaxHealth() {
        maxHealth += GameBalance.HEALTH_UPGRADE;
        health += GameBalance.HEALTH_UPGRADE;
    }

    public void fortifyFromReward() {
        maxHealth += GameBalance.CLEAR_HEALTH;
        health += GameBalance.CLEAR_HEALTH;
    }

    public boolean heal(int amount) {
        if (!alive || health >= maxHealth) {
            return false;
        }
        health = Math.min(maxHealth, health + amount);
        return true;
    }

    public void drinkAttackPotion() {
        attackPotionTimer = GameBalance.POWER_DURATION;
    }

    public void restoreRunStats(int attackLevels, int healthLevels,
                                int savedHealth, float potionTime) {
        restoreRunStats(attackLevels, healthLevels, savedHealth, potionTime, 0);
    }

    public void restoreRunStats(int attackLevels, int healthLevels,
                                int savedHealth, float potionTime, int rewardHealth) {
        attackUpgradeLevel = attackLevels;
        maxHealth = GameBalance.BASE_HEALTH + healthLevels * GameBalance.HEALTH_UPGRADE + rewardHealth;
        health = Math.max(1, Math.min(maxHealth, savedHealth));
        attackPotionTimer = Math.max(0f, potionTime);
    }

    public boolean isAttacking() {

        return attacking && alive;
    }

    /** Only the forward thrust and slash frames deal damage; windup/recovery do not. */
    public boolean isAttackActive() {
        return isAttacking() && (groundCombo&&swordCombo!=null?swordCombo.damaging():attackStateTime>=attackDuration*.25f&&attackStateTime<attackDuration*.75f);
    }

    public boolean isDashing() {

        return dashing;
    }

    public boolean isAlive() {

        return alive;
    }

    public void dispose() {
        if(comboSprites!=null)comboSprites.close();
        moonArt.close();
        thrustSprites.dispose();

        idleTexture.dispose();
        runTexture.dispose();
        jumpTexture.dispose();
        attackTexture.dispose();
        dashTexture.dispose();
    }
}
