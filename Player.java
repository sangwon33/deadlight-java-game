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
    private int jumpCount, attackCount, dashCount;
    public float getWalkDistance() { return walkDistance; }
    public int getJumpCount() { return jumpCount; }
    public int getAttackCount() { return attackCount; }
    public int getDashCount() { return dashCount; }
    private final IdentityHashMap<TextureRegion, Float> footRatios = new IdentityHashMap<>();
    private boolean screenLeft, screenRight;
    private boolean pendingJump, pendingAttack, pendingDash;
    private boolean pendingSkill, skillInput, usingSkill;
    private float skillElapsed, skillCooldown;
    private int skillCount, skillDamageAtCast, completedSkillCount, interruptedSkillCount;

    public float getSkillCooldown() { return skillCooldown; }
    public float getSkillElapsed() { return skillElapsed; }
    public int getSkillCount() { return skillCount; }
    public int getCompletedSkillCount() { return completedSkillCount; }
    public int getInterruptedSkillCount() { return interruptedSkillCount; }
    public boolean isUsingSkill() { return usingSkill; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isStunned() { return hitStunTimer > 0; }
    public boolean canUseSkill() { return alive && !usingSkill && !attacking && !dashing && hitStunTimer <= 0 && skillCooldown <= 0; }
    public boolean isSkillActive() { return usingSkill && skillElapsed >= GameBalance.SKILL_WINDUP && skillElapsed < GameBalance.SKILL_STRIKE_END; }
    public int getSkillDamage() { return usingSkill ? skillDamageAtCast : getAttackDamage() + GameBalance.SKILL_BONUS; }
    public String getSkillPhase() { return skillElapsed < GameBalance.SKILL_WINDUP ? "CHARGING" : isSkillActive() ? "STRIKE" : "RECOVER"; }
    public Rectangle getSkillBounds() { return new Rectangle(facingRight ? x + width - 12 : x - 90 + 12, y + 4, 90, 64); }

    public void setScreenInput(boolean left, boolean right, boolean jump, boolean attack, boolean dash) {
        setScreenInput(left, right, jump, attack, dash, false);
    }
    public void setScreenInput(boolean left, boolean right, boolean jump, boolean attack, boolean dash, boolean skill) {
        screenLeft = left; screenRight = right;
        pendingJump |= jump; pendingAttack |= attack; pendingDash |= dash;
        pendingSkill |= skill;
    }
    public float getDashCooldownTimer() { return dashCooldownTimer; }
    public void clearActionInput() {
        screenLeft = screenRight = false;
        pendingJump = pendingAttack = pendingDash = false;
        jumpInput = attackInput = dashInput = false;
        pendingSkill = skillInput = false;
    }
    private boolean jumpInput, attackInput, dashInput;

    public void captureActionInput() {
        pendingJump |= Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        pendingAttack |= Gdx.input.isKeyJustPressed(Input.Keys.J);
        pendingDash |= Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT);
        pendingSkill |= Gdx.input.isKeyJustPressed(Input.Keys.Q);
    }


    // 기본 이미지
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

    // 대시 애니메이션
    private Texture dashTexture;
    private TextureRegion[] dashFrames;
    private Animation<TextureRegion> dashAnimation;
    private float dashStateTime = 0;

    // 캐릭터 위치
    private float x;
    private float y;

    // 실제 충돌 판정 크기
    private final float width = 64;
    private final float height = 64;

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
    private final float moveSpeed = 200;
    private final float jumpPower = 600;
    private final float gravity = -1200;
    private final float groundY = 60;

    private float velocityY;
    private boolean onGround = true;
    private boolean moving = false;
    private boolean facingRight = true;

    // 공격
    private boolean attacking = false;
    private float attackTimer = 0;
    private final float attackDuration = 0.30f;

    private final float attackWidth = 45;
    private final float attackHeight = 38;
    private final float attackHitboxInset = 12;

    // 대시
    private boolean dashing = false;
    private float dashTimer = 0;
    private float dashCooldownTimer = 0;
    private float dashDirection = 1;

    private final float dashSpeed = 650;
    private final float dashDuration = 0.14f;
    private final float dashCooldown = 5f;

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

        loadIdleTexture();
        loadRunAnimation();
        loadJumpFrames();
        loadAttackAnimation();
        loadDashAnimation();
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

    public void update(
            float deltaTime,
            ArrayList<Rectangle> platforms,
            float worldWidth) {

        captureActionInput();
        jumpInput = pendingJump; attackInput = pendingAttack; dashInput = pendingDash;
        pendingJump = pendingAttack = pendingDash = false;
        skillInput = pendingSkill; pendingSkill = false;
        skillCooldown = Math.max(0, skillCooldown - deltaTime);
        if (attackPotionTimer > 0f) {
            attackPotionTimer = Math.max(0f, attackPotionTimer - deltaTime);
        }
        updateInvincibility(deltaTime);
        updateHitFlash(deltaTime);
        updateDashCooldown(deltaTime);

        moving = false;

        if (!alive) {

            attacking = false;
            dashing = false;
            usingSkill = false;

            return;
        }

        float previousY = y;

        /*
         * 피격 중에는 플레이어 입력을 받지 않고
         * 넉백과 중력만 적용한다.
         */
        if (hitStunTimer > 0) {

            updateHitStun(deltaTime);

            applyGravity(deltaTime);
            checkGroundCollision();

            checkPlatformCollision(
                    platforms,
                    previousY
            );

            x = MathUtils.clamp(
                    x,
                    0,
                    worldWidth - width
            );

            return;
        }

        if (updateSkill(deltaTime)) {
            applyGravity(deltaTime); checkGroundCollision(); checkPlatformCollision(platforms, previousY);
            x = MathUtils.clamp(x, 0, worldWidth - width);
            return;
        }

        updateAttack(deltaTime);

        if (updateDash(deltaTime)) {

            runStateTime = 0;

            x = MathUtils.clamp(
                    x,
                    0,
                    worldWidth - width
            );

            return;
        }

        float previousX = x;
        moveHorizontal(deltaTime);
        walkDistance += Math.abs(MathUtils.clamp(x, 0, worldWidth - width)
                - MathUtils.clamp(previousX, 0, worldWidth - width));

        jump();

        applyGravity(deltaTime);

        checkGroundCollision();

        checkPlatformCollision(
                platforms,
                previousY
        );

        updateRunAnimation(deltaTime);

        x = MathUtils.clamp(
                x,
                0,
                worldWidth - width
        );
    }

    private void moveHorizontal(float deltaTime) {

        if (Gdx.input.isKeyPressed(
                Input.Keys.A) || screenLeft) {

            x -= moveSpeed * deltaTime;

            moving = true;

            faceLeft();
        }

        if (Gdx.input.isKeyPressed(
                Input.Keys.D) || screenRight) {

            x += moveSpeed * deltaTime;

            moving = true;

            faceRight();
        }
    }

    private boolean updateSkill(float deltaTime) {
        float direction = ((Gdx.input.isKeyPressed(Input.Keys.D) || screenRight) ? 1 : 0)
                - ((Gdx.input.isKeyPressed(Input.Keys.A) || screenLeft) ? 1 : 0);
        if (skillInput && canUseSkill()) {
            if (direction > 0) faceRight(); else if (direction < 0) faceLeft();
            usingSkill = true; skillElapsed = 0; skillCooldown = GameBalance.SKILL_COOLDOWN;
            skillDamageAtCast = getAttackDamage() + GameBalance.SKILL_BONUS; skillCount++;
        }
        if (!usingSkill) return false;
        skillElapsed += deltaTime;
        // Facing is committed; mobility is reduced during the windup and recovery.
        x += direction * moveSpeed * (skillElapsed < GameBalance.SKILL_WINDUP ? .2f
                : skillElapsed >= GameBalance.SKILL_STRIKE_END ? .35f : 0) * deltaTime;
        if (skillElapsed >= GameBalance.SKILL_DURATION) {
            usingSkill = false; skillElapsed = 0; completedSkillCount++;
        }
        return true;
    }

    private void updateRunAnimation(
            float deltaTime) {

        if (moving
                && onGround
                && !attacking
                && !dashing) {

            runStateTime += deltaTime;

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

        if (onGround
                && jumpInput) {

            velocityY = jumpPower;
            onGround = false;
            jumpCount++;
        }
    }

    private void updateAttack(float deltaTime) {

        if (!attacking
                && !dashing
                && attackInput) {

            attacking = true;
            attackCount++;

            attackTimer = attackDuration;
            attackStateTime = 0;
        }

        if (attacking) {

            attackTimer -= deltaTime;
            attackStateTime += deltaTime;

            if (attackTimer <= 0) {

                attacking = false;

                attackTimer = 0;
                attackStateTime = 0;
            }
        }
    }

    private void updateDashCooldown(
            float deltaTime) {

        if (dashCooldownTimer > 0) {

            dashCooldownTimer -= deltaTime;

            if (dashCooldownTimer < 0) {
                dashCooldownTimer = 0;
            }
        }
    }

    private boolean updateDash(
            float deltaTime) {

        boolean shiftPressed = dashInput;

        // 대시 시작
        if (!dashing
                && !attacking
                && dashCooldownTimer <= 0
                && shiftPressed) {

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

        dashStateTime += deltaTime;

        x += dashDirection
                * dashSpeed
                * deltaTime;

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

    private void checkGroundCollision() {

        if (y <= groundY) {

            y = groundY;

            velocityY = 0;
            onGround = true;
        }
    }

    private void checkPlatformCollision(
            ArrayList<Rectangle> platforms,
            float previousY) {

        Rectangle playerBounds = getBounds();

        for (Rectangle platform : platforms) {

            float platformTop =
                    platform.y + platform.height;

            boolean falling =
                    velocityY <= 0;

            boolean wasAbovePlatform =
                    previousY >= platformTop;

            if (falling
                    && wasAbovePlatform
                    && playerBounds.overlaps(platform)) {

                y = platformTop;

                velocityY = 0;
                onGround = true;

                playerBounds.setPosition(x, y);
            }
        }
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
        attacking = false;
        dashing = false;
        if (usingSkill) interruptedSkillCount++;
        usingSkill = false; skillElapsed = 0;
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

        TextureRegion currentRegion;

        float currentDrawWidth;
        float currentDrawHeight;
        float currentFootOffset;

        if (usingSkill) {
            float animationTime = skillElapsed < GameBalance.SKILL_WINDUP
                    ? skillElapsed / GameBalance.SKILL_WINDUP * .119f
                    : skillElapsed < GameBalance.SKILL_STRIKE_END
                    ? .12f + (skillElapsed - GameBalance.SKILL_WINDUP) / (GameBalance.SKILL_STRIKE_END - GameBalance.SKILL_WINDUP) * .119f
                    : .24f + (skillElapsed - GameBalance.SKILL_STRIKE_END) / (GameBalance.SKILL_DURATION - GameBalance.SKILL_STRIKE_END) * .059f;
            currentRegion = attackAnimation.getKeyFrame(animationTime, false);
            currentDrawWidth = attackDrawWidth; currentDrawHeight = attackDrawHeight; currentFootOffset = attackFootOffset;
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

    private void drawCentered(
            SpriteBatch spriteBatch,
            TextureRegion currentRegion,
            float drawWidth,
            float drawHeight,
            float footOffset) {

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
                height
        );
    }

    public Rectangle getAttackBounds() {

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
                y + 12,
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
        return 1 + attackUpgradeLevel + (attackPotionTimer > 0f ? GameBalance.POWER_BONUS : 0);
    }

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
        return isAttacking() && attackStateTime >= .12f && attackStateTime < .24f;
    }

    public boolean isDashing() {

        return dashing;
    }

    public boolean isAlive() {

        return alive;
    }

    public void dispose() {

        idleTexture.dispose();
        runTexture.dispose();
        jumpTexture.dispose();
        attackTexture.dispose();
        dashTexture.dispose();
    }
}
