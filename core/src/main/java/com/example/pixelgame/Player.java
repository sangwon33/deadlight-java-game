package com.example.pixelgame;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    private float walkDistance;
    private java.util.List<Rectangle> worldSolids;
    private Rectangle support,ignoredPlatform;
    private float dropTimer,worldBottom=-Float.MAX_VALUE,worldTop=Float.MAX_VALUE,safeX,safeY;
    private boolean crouching;
    private final DownGesture downGesture=new DownGesture();
    public boolean isCrouching(){return crouching;}
    private boolean canLookStill(){
        return alive && onGround && !attacking && !dashing && !usingSkill && hitStunTimer<=0
                && !KeyBindings.pressed(KeyBindings.Action.LEFT) && !KeyBindings.pressed(KeyBindings.Action.RIGHT)
                && !screenLeft && !screenRight;
    }
    public boolean isLookingUp(){return canLookStill() && !crouching
            && KeyBindings.pressed(KeyBindings.Action.LOOK_UP) && !KeyBindings.pressed(KeyBindings.Action.LOOK_DOWN);}
    public boolean isLookingDown(){return canLookStill() && crouching
            && downGesture.looking() && !KeyBindings.pressed(KeyBindings.Action.LOOK_UP);}
    public boolean isGrounded(){return onGround;}
    public Rectangle getSupport(){return support;}
    public float getVelocityY(){return velocityY;}
    public void configureWorld(java.util.List<Rectangle> solids,float bottom,float top){worldSolids=solids;worldBottom=bottom;worldTop=top;safeX=x;safeY=y;}
    public boolean wantsDropThrough(){
        return downGesture.releasingTap(KeyBindings.pressed(KeyBindings.Action.LOOK_DOWN),support);
    }
    public void carry(float dx,float dy){x+=dx;y+=dy;}
    public void launch(float speed){if(!alive)return;velocityY=speed;onGround=false;support=null;crouching=false;jumpWasHeld=false;airDashUsed=false;coyoteTimer=0;}
    private float bodyHeight(){return crouching?32f:height;}
    private boolean canStand(){if(worldSolids==null)return true;Rectangle standing=new Rectangle(x,y,width,height);for(Rectangle r:worldSolids)if(r.width>0&&standing.overlaps(r))return false;return true;}

    private int jumpCount, attackCount, dashCount, dropCount;
    public float getWalkDistance() { return walkDistance; }
    public int getJumpCount() { return jumpCount; }
    public int getAttackCount() { return attackCount; }
    public int getDashCount() { return dashCount; }
    public int getDropCount() { return dropCount; }
    private boolean screenLeft, screenRight;
    private boolean pendingJump, pendingAttack, pendingDash;
    private boolean pendingSkill, skillInput, usingSkill;
    private boolean chargingSkill, screenSkillHeld;
    private float skillChargeTime;
    private int skillChargeStage = 1, completedFullChargeSkills;
    private float skillSweepStartX;
    private boolean skillRushThisFrame;
    private float skillElapsed, skillCooldown,skillReadyFlash;
    private boolean skillReadyPending;
    public float getSkillReadyFlash(){return skillReadyFlash;}
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
    public int getSkillDamage() { return Math.round((GameBalance.SKILL_DAMAGE+attackUpgradeLevel*3)*(attackPotionTimer>0?GameBalance.POWER_MULTIPLIER:1)); }
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
        screenJumpHeld = false;
        pendingJump |= jump; pendingAttack |= attack; pendingDash |= dash;
        pendingSkill |= skill;
        screenSkillHeld = skillHeld;
    }
    public float getDashCooldownTimer() { return dashCooldownTimer; }
    public void clearActionInput() {
        downGesture.cancel(KeyBindings.pressed(KeyBindings.Action.LOOK_DOWN));
        screenLeft = screenRight = false;
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


    private final MoonAtlas moonArt;
    private final MoonAtlas lookArt;
    private HeroTransitions transitionArt;
    private AirAttackSprites airArt;
    private AirAttackSprites air(){if(airArt==null)airArt=new AirAttackSprites();return airArt;}
    private int airFrame(){return Math.min(5,(int)(attackStateTime/attackDuration*6));}
    private float landingTimer;
    private float moonTime;
    private float runStateTime;
    private float attackStateTime;
    private SwordCombo swordCombo;
    private SwordComboSprites comboSprites;
    private boolean groundCombo;
    public int getComboStep(){return groundCombo&&swordCombo!=null?swordCombo.step():0;}
    public boolean isComboFinisher(){return getComboStep()==3;}

    // 캐릭터 위치
    private float x;
    private float y;

    // 실제 충돌 판정 크기
    private final float width = GameBalance.character(64);
    private final float height = GameBalance.character(64);

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
    private float attackPotionTimer = 0f, hastePotionTimer,healingAuraTimer;
    float getHealingAuraTimer(){return healingAuraTimer;}
    void showHealingAura(){healingAuraTimer=.9f;}
    private boolean attackCritical,skillCritical;
    private int completedCombos;
    public int getCompletedComboCount(){return completedCombos;}
    public boolean isAttackCritical(){return attackCritical;}
    public boolean isSkillCritical(){return skillCritical;}
    public int getCurrentSkillDamage(){return Math.round(getSkillDamage()*(skillCritical?getCriticalDamageMultiplier():1));}
    public float getHastePotionTimer(){return hastePotionTimer;}
    public void drinkHastePotion(){hastePotionTimer=GameBalance.HASTE_DURATION;}
    public void restoreHaste(float seconds){hastePotionTimer=MathUtils.clamp(seconds,0,GameBalance.HASTE_DURATION);}

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

        moonArt = MoonAtlas.acquireIsolated("art/hero.png",4,4,11);
        lookArt = MoonAtlas.acquire("art/hero-look.png",2,2);
    }

    public void update(float deltaTime,ArrayList<Rectangle> platforms,float worldWidth) {
        boolean groundedBefore=onGround;
        landingTimer=Math.max(0,landingTimer-deltaTime);
        moonTime+=deltaTime;captureActionInput();
        jumpInput=pendingJump;attackInput=pendingAttack;dashInput=pendingDash;
        pendingJump=pendingAttack=pendingDash=false;skillInput=pendingSkill;pendingSkill=false;
        float oldCooldown=skillCooldown;
        skillCooldown=Math.max(0,skillCooldown-deltaTime);if(skillCooldown<.0001f)skillCooldown=0;
        skillReadyFlash=Math.max(0,skillReadyFlash-deltaTime);
        if(oldCooldown>0&&skillCooldown==0)skillReadyPending=true;
        dropTimer=Math.max(0,dropTimer-deltaTime);if(dropTimer==0)ignoredPlatform=null;
        jumpBufferTimer=Math.max(0,jumpBufferTimer-deltaTime);attackBufferTimer=Math.max(0,attackBufferTimer-deltaTime);
        if(jumpInput&&!usingSkill)jumpBufferTimer=GameBalance.INPUT_BUFFER;
        if(attackInput&&!usingSkill)attackBufferTimer=GameBalance.INPUT_BUFFER;
        coyoteTimer=onGround?GameBalance.COYOTE_TIME:Math.max(0,coyoteTimer-deltaTime);
        if(onGround)airDashUsed=false;
        boolean canGesture=alive&&onGround&&!usingSkill&&!dashing&&!attacking&&hitStunTimer<=0
                &&!jumpInput&&!attackInput&&!dashInput&&!skillInput&&!KeyBindings.pressed(KeyBindings.Action.LOOK_UP);
        boolean tap=downGesture.update(deltaTime,KeyBindings.justPressed(KeyBindings.Action.LOOK_DOWN),
                KeyBindings.pressed(KeyBindings.Action.LOOK_DOWN),canGesture,support);
        boolean drop=tap&&support!=null&&platforms.contains(support);
        if(drop){
            dropCount++;
            ignoredPlatform=support;dropTimer=.25f;support=null;y-=2;velocityY=-100;onGround=false;
            coyoteTimer=jumpBufferTimer=0;jumpInput=false;
        }
        boolean peekDown = downGesture.preparing() && !KeyBindings.pressed(KeyBindings.Action.LOOK_UP)
                && !KeyBindings.pressed(KeyBindings.Action.LEFT) && !KeyBindings.pressed(KeyBindings.Action.RIGHT)
                && !screenLeft && !screenRight && !jumpInput && !attackInput && !dashInput && !skillInput;
        boolean wanted=peekDown&&onGround&&!usingSkill&&!dashing&&!attacking&&hitStunTimer<=0;
        if(wanted||crouching&&!canStand())crouching=true;else crouching=false;
        boolean jumpHeld=KeyBindings.pressed(KeyBindings.Action.JUMP)||screenJumpHeld;
        if(velocityY>0&&jumpWasHeld&&!jumpHeld&&!dashing&&!usingSkill)velocityY*=GameBalance.JUMP_CUT;
        jumpWasHeld=jumpHeld;
        attackPotionTimer=Math.max(0,attackPotionTimer-deltaTime);hastePotionTimer=Math.max(0,hastePotionTimer-deltaTime);healingAuraTimer=Math.max(0,healingAuraTimer-deltaTime);
        updateInvincibility(deltaTime);updateHitFlash(deltaTime);updateDashCooldown(deltaTime);moving=false;
        if(!alive){cancelNormalAttack();dashing=usingSkill=false;return;}
        if(dashing&&jumpBufferTimer>0&&(onGround||coyoteTimer>0)){dashing=false;dashTimer=0;}
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
        float impactSpeed=velocityY;
        if(!freezeY)applyGravity(deltaTime);else onGround=false;
        support=null;
        resolveSolids(previousY);
        checkPlatformCollision(platforms,previousY);
        if(!groundedBefore&&onGround&&impactSpeed < -150)landingTimer=.14f;
        if(y+bodyHeight()>worldTop){y=worldTop-bodyHeight();velocityY=Math.min(0,velocityY);}
        if(onGround&&jumpBufferTimer>0&&!locked&&!crouching)jump();
        if(!locked)updateRunAnimation(deltaTime);
        if(skillReadyPending&&canUseSkill()){skillReadyPending=false;skillReadyFlash=.9f;}
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

            x -= getMoveSpeed() * (groundCombo?(isComboFinisher()?0:.65f):1f) * deltaTime;


            moving = true;

            if(!groundCombo)faceLeft();
        }

        if (KeyBindings.pressed(KeyBindings.Action.RIGHT) || screenRight) {

            x += getMoveSpeed() * (groundCombo?(isComboFinisher()?0:.65f):1f) * deltaTime;


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
            skillReadyPending=false;skillReadyFlash=0;
            usingSkill = chargingSkill = true;skillCritical=MathUtils.random()<getCriticalChance();
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
        facingRight = false;
    }

    private void faceRight() {
        facingRight = true;
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
            int previousStep=swordCombo.step();
            int started=swordCombo.update(deltaTime,attackInput);
            if(previousStep==3&&!swordCombo.active())completedCombos++;attackBufferTimer=0;
            if(started>0){attackCritical=MathUtils.random()<getCriticalChance();attackCount+=started;faceAttackInput();}
            x+=(facingRight?1:-1)*swordCombo.lunge();
            attacking=swordCombo.active();attackStateTime=swordCombo.elapsed();attackTimer=swordCombo.duration()-attackStateTime;
            if(!attacking){groundCombo=false;attackStateTime=attackTimer=0;}
            return;
        }
        if (!attacking && !dashing && attackBufferTimer > 0) {
            faceAttackInput();attackCritical=MathUtils.random()<getCriticalChance();
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

            dashDirection =
                    facingRight ? 1 : -1;

            // 대시 중에는 높이가 변하지 않는다
            velocityY = 0;
        }

        if (!dashing) {
            return false;
        }

        float activeDelta = Math.min(deltaTime, dashTimer);
        x += dashDirection * dashSpeed * activeDelta;

        dashTimer -= deltaTime;

        if (dashTimer <= 0) {

            dashing = false;

            dashTimer = 0;


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

        downGesture.cancel(KeyBindings.pressed(KeyBindings.Action.LOOK_DOWN));
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
            float alpha=invincibleTimer>0&&hitFlashTimer<=0?(GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS)?.72f:((int)(invincibleTimer*20)%2==0?.3f:1)):1;
            comboSprites.actor(spriteBatch,swordCombo,getCenterX(),y,facingRight,hitFlashTimer,alpha);return;
        }
        drawMoon(spriteBatch);
    }

    private final Rectangle visualBounds=new Rectangle();
    private int transitionFrame(){
        if(hitStunTimer>0&&!crouching)return hitStunTimer>.09f?4:5;
        if(usingSkill){
            if(chargingSkill)return 7;
            if(skillElapsed>=GameBalance.SKILL_RUSH_END)return skillElapsed<.30f?0:1;
            return -1;
        }
        if(dashing||attacking||crouching||isLookingUp())return -1;
        if(!onGround&&velocityY < -70)return 6;
        if(onGround&&!moving&&landingTimer>0)return landingTimer>.07f?2:3;
        return -1;
    }
    private HeroTransitions transitions(){if(transitionArt==null)transitionArt=new HeroTransitions();return transitionArt;}
    private int moonFrame(){
        return usingSkill ? (chargingSkill ? 14 : skillElapsed < .25f ? 15 : 11)
                : dashing ? 13 : attacking ? 8 + Math.min(3,(int)(attackStateTime / attackDuration * 4))
                : !onGround ? 12 : moving ? 4 + (int)(runStateTime / .09f) % 4 : (int)(moonTime / .28f) % 4;
    }
    Rectangle getVisualBounds(){
        if(attacking&&!groundCombo)return air().bounds(airFrame(),getCenterX(),y,facingRight,visualBounds);
        int supplemental=transitionFrame();
        if(supplemental>=0)return transitions().bounds(supplemental,getCenterX(),y,facingRight,visualBounds);
        int frame;MoonAtlas atlas;
        if(isLookingUp()){atlas=lookArt;frame=1;}
        else if(isLookingDown()){atlas=lookArt;frame=2+(int)(moonTime/.6f)%2;}
        else if(crouching){atlas=lookArt;frame=2;}
        else{atlas=moonArt;frame=moonFrame();}
        return atlas.inkBounds(frame,0,getCenterX(),y,GameBalance.character(78),facingRight,visualBounds);
    }

    private void drawMoon(SpriteBatch batch) {
        int frame = moonFrame();
        float packed=batch.getPackedColor();
        boolean gentle=GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS);
        float alpha=invincibleTimer>0&&hitFlashTimer<=0?(gentle?.72f:((int)(invincibleTimer*20)%2==0)?.3f:1):1;
        batch.setColor(1,hitFlashTimer>0?(gentle?.75f:.35f):1,hitFlashTimer>0?(gentle?.75f:.4f):1,alpha);
        int supplemental=transitionFrame();
        if(attacking&&!groundCombo)air().draw(batch,airFrame(),getCenterX(),y,facingRight);
        else if(supplemental>=0)transitions().draw(batch,supplemental,getCenterX(),y,facingRight);
        else if(isLookingUp())lookArt.draw(batch,1,0,getCenterX(),y,GameBalance.character(78),facingRight);
        else if(isLookingDown())lookArt.draw(batch,2+(int)(moonTime/.6f)%2,0,getCenterX(),y,GameBalance.character(78),facingRight);
        else if(crouching)lookArt.draw(batch,2,0,getCenterX(),y,GameBalance.character(78),facingRight);
        else moonArt.draw(batch,frame,0,getCenterX(),y,GameBalance.character(78),facingRight);
        batch.setPackedColor(packed);
    }
    public void drawAttackEffects(SpriteBatch batch){
        if(isAttackActive()&&groundCombo){if(comboSprites==null)comboSprites=new SwordComboSprites();comboSprites.effects(batch,swordCombo,getCenterX(),y,facingRight);}
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
        return Math.round((GameBalance.BASE_ATTACK_DAMAGE + attackUpgradeLevel)*(attackPotionTimer>0?GameBalance.POWER_MULTIPLIER:1));
    }
    public int getCurrentAttackDamage(){int base=isComboFinisher()?Math.round(getAttackDamage()*1.4f):getAttackDamage();return Math.round(base*(attackCritical?getCriticalDamageMultiplier():1));}
    public float getMoveSpeed(){return moveSpeed*(hastePotionTimer>0?GameBalance.HASTE_MULTIPLIER:1);}
    public float getAttackSpeed(){return SwordCombo.attacksPerSecond();}
    // Critical is rolled once per swing/cast, shared by all targets hit by that action.
    public float getCriticalChance(){return GameBalance.CRITICAL_CHANCE;}
    public float getCriticalDamageMultiplier(){return GameBalance.CRITICAL_MULTIPLIER;}

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
        if(airArt!=null)airArt.close();
        if(transitionArt!=null)transitionArt.close();
        if(comboSprites!=null)comboSprites.close();
        moonArt.close();
        lookArt.close();
    }
}
