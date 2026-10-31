package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    public int getHealth() { return health; }


    public enum EnemyType {
        SLIME,
        KNIGHT,
        DARK_SLIME
    }

    // Measure the body, not transparent padding or the sword's attack trail.
    private static final float KNIGHT_BODY_HEIGHT = 105f;
    private static final class BodyFrame extends TextureRegion {
        final float drawScale;
        final int footFromBottom;

        BodyFrame(TextureRegion region, float targetBodyHeight, float sourceBodyHeight, Pixmap pixels) {
            super(region);
            drawScale = targetBodyHeight / sourceBodyHeight;
            int bottom = getRegionHeight();
            for (int row = getRegionHeight() - 1; row >= 0; row--) {
                int solid = 0;
                for (int col = 0; col < getRegionWidth(); col++) {
                    if ((pixels.getPixel(getRegionX() + col, getRegionY() + row) & 255) > 96) solid++;
                }
                if (solid >= 3) { bottom = row + 1; break; }
            }
            footFromBottom = getRegionHeight() - bottom;
        }
    }

    private final EnemyType enemyType;
    private final boolean sourceFacesRight;

    // 충돌 판정 위치
    private float x;
    private float y;

    // 실제 몸 충돌 크기
    private float collisionWidth;
    private float collisionHeight;

    // 가만히 있을 때
    private float idleSpriteWidth;
    private float idleSpriteHeight;
    private float idleYOffset;

    // 움직일 때
    private float moveSpriteWidth;
    private float moveSpriteHeight;
    private float moveYOffset;

    // 공격할 때
    private float attackSpriteWidth;
    private float attackSpriteHeight;
    private float attackYOffset;

    // 사망할 때
    private float deathSpriteWidth;
    private float deathSpriteHeight;
    private float deathYOffset;

    // 이미지
    private Texture idleTexture;
    private Texture moveTexture;
    private Texture attackTexture;
    private Texture deathTexture;

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> moveAnimation;
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> deathAnimation;

    // 이동
    private float idleAnimationTime = 0;
    private float moveAnimationTime = 0;
    private boolean moving = false;

    // 공격
    private float attackAnimationTime = 0;
    private boolean attacking = false;
    private boolean attackDamageApplied = false;

    // 세 번째 공격 프레임에서 피해 적용
    private float attackHitTime;

    // 사망
    private float deathAnimationTime = 0;
    private boolean dying = false;

    // 마지막 사망 프레임 유지 시간
    private final float deathLastFrameHold = 0.25f;

    // 방향
    private boolean facingRight = true;

    // 이동과 감지
    private float moveSpeed;
    private float detectionRange;
    private float attackRange;

    // 공격 재사용 시간
    private float attackCooldownTimer = 0;
    private float attackCooldown;
    private float poiseTimer;

    /** A brief steady stance prevents basic-attack stun loops; heavy attacks break it. */
    public boolean isBraced() {
        return enemyType == EnemyType.KNIGHT && alive && !dying
                && (poiseTimer > 0 || (attacking && attackAnimationTime >= .12f && attackAnimationTime < .40f));
    }

    /** The cue uses the same locked direction and height as this attack's damage bounds. */
    public boolean isAttackCueVisible() {
        return alive && !dying && attacking && attackAnimationTime < attackHitTime + .09f;
    }

    public float getAttackCueProgress() {
        return Math.max(0, Math.min(1, attackAnimationTime / attackHitTime));
    }

    // 체력
    private int health;
    private int maxHealth;
    private boolean alive = true;
    private boolean goldRewardClaimed = false;

    // 피격 효과
    private float hitFlashTimer = 0;
    private final float hitFlashDuration = 0.15f;

    // 피격 후 무적 시간
    private float hitInvincibleTimer = 0;
    private final float hitInvincibleDuration = 0.20f;

    // 넉백
    private float knockbackVelocityX = 0;
    private final float knockbackSpeed = 280;
    private final float knockbackFriction = 900;

    public Enemy(float startX, float startY) {

        this(
                startX,
                startY,
                EnemyType.SLIME
        );
    }

    public Enemy(
            float startX,
            float startY,
            EnemyType enemyType) {

        x = startX;
        y = startY;
        this.enemyType = enemyType;
        sourceFacesRight =
                enemyType != EnemyType.KNIGHT;

        configureStats();
        loadTextures();
        createAnimations();
    }

    private void configureStats() {

        if (enemyType == EnemyType.KNIGHT) {

            collisionWidth = 55f;
            collisionHeight = 105f;

            idleSpriteWidth = 125f;
            idleSpriteHeight = 150f;
            idleYOffset = -22f;

            moveSpriteWidth = 125f;
            moveSpriteHeight = 150f;
            moveYOffset = -22f;

            attackSpriteWidth = 180f;
            attackSpriteHeight = 150f;
            attackYOffset = -22f;

            deathSpriteWidth = 125f;
            deathSpriteHeight = 150f;
            deathYOffset = -22f;

            moveSpeed = 62f;
            detectionRange = 390f;
            attackRange = 92f;
            attackCooldown = 1.4f;
            attackHitTime = 0.30f;

            maxHealth = 5;
            health = maxHealth;

        } else if (enemyType == EnemyType.DARK_SLIME) {

            collisionWidth = 58f;
            collisionHeight = 72f;

            idleSpriteWidth = 145f;
            idleSpriteHeight = 120f;
            idleYOffset = -25f;

            moveSpriteWidth = 145f;
            moveSpriteHeight = 120f;
            moveYOffset = -25f;

            attackSpriteWidth = 170f;
            attackSpriteHeight = 125f;
            attackYOffset = -25f;

            deathSpriteWidth = 145f;
            deathSpriteHeight = 120f;
            deathYOffset = -25f;

            moveSpeed = 70f;
            detectionRange = 380f;
            attackRange = 90f;
            attackCooldown = 1.3f;
            attackHitTime = 0.22f;

            maxHealth = 4;
            health = maxHealth;

        } else {

            collisionWidth = 52f;
            collisionHeight = 58f;

            idleSpriteWidth = 150f;
            idleSpriteHeight = 120f;
            idleYOffset = -30f;

            moveSpriteWidth = 120f;
            moveSpriteHeight = 96f;
            moveYOffset = -18f;

            attackSpriteWidth = 190f;
            attackSpriteHeight = 130f;
            attackYOffset = -30f;

            deathSpriteWidth = 150f;
            deathSpriteHeight = 100f;
            deathYOffset = -18f;

            moveSpeed = 75f;
            detectionRange = 350f;
            attackRange = 72f;
            attackCooldown = 1.2f;
            attackHitTime = 0.24f;

            maxHealth = 3;
            health = maxHealth;
        }
    }

    private void loadTextures() {

        if (enemyType == EnemyType.KNIGHT) {

            idleTexture = new Texture(
                    "enemy/k_stand.png"
            );

            moveTexture = new Texture(
                    "enemy/k_walk.png"
            );

            attackTexture = new Texture(
                    "enemy/k_attack.png"
            );

            deathTexture = null;

        } else if (enemyType == EnemyType.DARK_SLIME) {

            // Idle is reused for movement; the attack has its own four-frame sheet.
            idleTexture = new Texture(
                    "enemy/dark_slime_idle.png"
            );

            moveTexture = new Texture(
                    "enemy/dark_slime_idle.png"
            );

            attackTexture = new Texture(
                    "enemy/dark_slime_attack_idle.png"
            );

            deathTexture = new Texture("enemy/dark_slime_death.png");

        } else {

            idleTexture = new Texture(
                    "enemy/slime stand.png"
            );

            moveTexture = new Texture(
                    "enemy/slime walk.png"
            );

            attackTexture = new Texture(
                    "enemy/slime attack.png"
            );

            // No green death sheet exists: reuse the green body and fade it, never another enemy's art.
            deathTexture = null;
        }

        idleTexture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );

        moveTexture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );

        attackTexture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );

        if (deathTexture != null) {

            deathTexture.setFilter(
                    TextureFilter.Nearest,
                    TextureFilter.Nearest
            );
        }

    }

    private void createAnimations() {

        int idleFrameCount =
                enemyType == EnemyType.DARK_SLIME
                        ? 1
                        : 4;

        int actionFrameCount =
                enemyType == EnemyType.DARK_SLIME
                        ? 1
                        : 6;

        idleAnimation = createAnimation(
                idleTexture,
                idleFrameCount,
                0.18f,
                Animation.PlayMode.LOOP_PINGPONG
        );

        moveAnimation = createAnimation(
                moveTexture,
                actionFrameCount,
                0.12f,
                Animation.PlayMode.LOOP
        );

        attackAnimation = createAnimation(
                attackTexture,
                enemyType == EnemyType.DARK_SLIME ? 4 : actionFrameCount,
                0.10f,
                Animation.PlayMode.NORMAL
        );

        if (enemyType == EnemyType.SLIME) {
            TextureRegion[] idle = idleAnimation.getKeyFrames();
            deathAnimation = new Animation<>(.16f, new TextureRegion[]{idle[0], idle[2], idle[2], idle[2]});
            deathAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        } else if (deathTexture != null) {
            deathAnimation = createAnimation(
                    deathTexture,
                    4,
                    0.16f,
                    Animation.PlayMode.NORMAL
            );
        }
    }

    private Animation<TextureRegion> createAnimation(
            Texture texture,
            int frameCount,
            float frameDuration,
            Animation.PlayMode playMode) {

        int frameWidth =
                texture.getWidth() / frameCount;

        int frameHeight =
                texture.getHeight();

        TextureRegion[] frames =
                new TextureRegion[frameCount];

        // Native body dimensions exclude the sheet's transparent gutters and sword trails.
        boolean knight = enemyType == EnemyType.KNIGHT;
        boolean dark = enemyType == EnemyType.DARK_SLIME;
        String bodyAsset = knight ? (texture == idleTexture ? "enemy/k_stand.png"
                : texture == moveTexture ? "enemy/k_walk.png" : "enemy/k_attack.png")
                : dark ? (texture == deathTexture ? "enemy/dark_slime_death.png"
                        : texture == attackTexture ? "enemy/dark_slime_attack_idle.png" : "enemy/dark_slime_idle.png")
                : texture == idleTexture ? "enemy/slime stand.png" : texture == moveTexture ? "enemy/slime walk.png"
                : "enemy/slime attack.png";
        float sourceBodyHeight = knight ? (texture == idleTexture ? 553f
                : texture == moveTexture ? 423f : 347f) : dark ? (texture == deathTexture ? 107f : texture == attackTexture ? 330f : 650f)
                : texture == idleTexture ? 299f : texture == moveTexture ? 223f : 216f;
        Pixmap bodyPixels = new Pixmap(Gdx.files.internal(bodyAsset));
        try {
        for (int i = 0; i < frameCount; i++) {

            frames[i] = new TextureRegion(
                    texture,
                    i * frameWidth,
                    0,
                    frameWidth,
                    frameHeight
            );
            if (enemyType == EnemyType.SLIME && texture == moveTexture) {
                Rectangle body = FrameContent.mainBody(bodyPixels, frames[i]);
                frames[i] = new TextureRegion(texture, (int)body.x, (int)body.y, (int)body.width, (int)body.height);
            }
            frames[i] = new BodyFrame(frames[i], knight ? KNIGHT_BODY_HEIGHT : dark ? 70f : 52f, sourceBodyHeight, bodyPixels);
        }
        } finally {
            if (bodyPixels != null) bodyPixels.dispose();
        }

        Animation<TextureRegion> animation =
                new Animation<>(
                        frameDuration,
                        frames
                );

        animation.setPlayMode(playMode);

        return animation;
    }

    public void update(
            float deltaTime,
            Player player) {

        moving = false;

        updateHitTimers(deltaTime);
        poiseTimer = Math.max(0, poiseTimer - deltaTime);

        if (!alive) {
            return;
        }

        idleAnimationTime += deltaTime;

        /*
         * 사망 중에는 이동과 공격을 하지 않고
         * 사망 애니메이션만 진행한다.
         */
        if (dying) {

            updateKnockback(deltaTime);
            updateDeathAnimation(deltaTime);

            return;
        }

        updateAttackCooldown(deltaTime);

        boolean beingKnockedBack =
                updateKnockback(deltaTime);

        if (beingKnockedBack) {

            attacking = false;
            attackAnimationTime = 0;

            return;
        }

        if (!player.isAlive()) {
            return;
        }

        float playerCenterX =
                player.getCenterX();

        float enemyCenterX =
                getCenterX();

        float distanceX =
                Math.abs(
                        playerCenterX - enemyCenterX
                );

        float distanceY =
                Math.abs(
                        player.getY() - y
                );

        // 공격 중
        if (attacking) {

            updateAttack(
                    deltaTime,
                    player,
                    distanceX,
                    distanceY
            );

            return;
        }

        updateDirection(
                playerCenterX,
                enemyCenterX
        );

        // 플레이어가 감지 범위 안에 있을 때
        if (distanceX <= detectionRange) {

            // 공격 범위 밖이면 추적
            if (distanceX > attackRange) {

                moving = true;

                if (playerCenterX < enemyCenterX) {

                    x -= moveSpeed * deltaTime;

                } else {

                    x += moveSpeed * deltaTime;
                }
            }

            // 공격 범위 안이면 공격 시작
            else if (distanceY < collisionHeight
                    && attackCooldownTimer <= 0) {

                startAttack();
            }
        }

        updateMoveAnimation(deltaTime);
    }

    private void startAttack() {

        attacking = true;
        moving = false;

        attackAnimationTime = 0;
        attackDamageApplied = false;

        System.out.println(
                getEnemyName() + " 공격 시작!"
        );
    }

    private void updateAttack(
            float deltaTime,
            Player player,
            float distanceX,
            float distanceY) {

        attackAnimationTime += deltaTime;

        // 세 번째 공격 프레임에서 피해 판정
        if (!attackDamageApplied
                && attackAnimationTime >= attackHitTime) {

            float signedDistance = player.getCenterX() - getCenterX();
            boolean inFront = facingRight ? signedDistance >= 0 : signedDistance <= 0;
            if (distanceX <= attackRange && inFront && getAttackBounds().overlaps(player.getBounds())) {

            	player.takeDamage(
            	        1,
            	        getCenterX()
            	);

                System.out.println(
                        getEnemyName() + " 공격 적중!"
                );

            } else {

                System.out.println(
                        getEnemyName() + " 공격 빗나감!"
                );
            }

            attackDamageApplied = true;
        }

        // 공격 애니메이션 종료
        if (attackAnimation.isAnimationFinished(
                attackAnimationTime)) {

            attacking = false;
            attackAnimationTime = 0;

            attackCooldownTimer =
                    attackCooldown;
        }
    }

    private void updateDeathAnimation(
            float deltaTime) {

        deathAnimationTime += deltaTime;

        float disappearTime =
                deathAnimation.getAnimationDuration()
                + deathLastFrameHold;

        /*
         * 사망 애니메이션이 끝난 뒤
         * 마지막 프레임을 잠시 보여주고 사라진다.
         */
        if (deathAnimationTime >= disappearTime) {

            alive = false;

            System.out.println(
                getEnemyName() + " 사라짐!"
            );
        }
    }

    private void updateAttackCooldown(
            float deltaTime) {

        if (attackCooldownTimer > 0) {

            attackCooldownTimer -= deltaTime;

            if (attackCooldownTimer < 0) {
                attackCooldownTimer = 0;
            }
        }
    }

    private void updateHitTimers(
            float deltaTime) {

        if (hitFlashTimer > 0) {

            hitFlashTimer -= deltaTime;

            if (hitFlashTimer < 0) {
                hitFlashTimer = 0;
            }
        }

        if (hitInvincibleTimer > 0) {

            hitInvincibleTimer -= deltaTime;

            if (hitInvincibleTimer < 0) {
                hitInvincibleTimer = 0;
            }
        }
    }

    private boolean updateKnockback(
            float deltaTime) {

        if (Math.abs(knockbackVelocityX) <= 1) {

            knockbackVelocityX = 0;

            return false;
        }

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
        else {

            knockbackVelocityX +=
                    knockbackFriction * deltaTime;

            if (knockbackVelocityX > 0) {
                knockbackVelocityX = 0;
            }
        }

        return true;
    }

    private void updateDirection(
            float playerCenterX,
            float enemyCenterX) {

        if (playerCenterX < enemyCenterX) {

            facingRight = false;

        } else if (playerCenterX > enemyCenterX) {

            facingRight = true;
        }
    }

    private void updateMoveAnimation(
            float deltaTime) {

        if (moving) {

            moveAnimationTime += deltaTime;

        } else {

            moveAnimationTime = 0;
        }
    }

    public void takeDamage(
            int damage,
            float attackerX) {
        takeDamage(damage, attackerX, false);
    }

    public void takeDamage(int damage, float attackerX, boolean heavy) {

        if (!alive
                || dying
                || hitInvincibleTimer > 0) {

            return;
        }

        health -= damage;

        hitFlashTimer =
                hitFlashDuration;

        hitInvincibleTimer =
                hitInvincibleDuration;

        boolean steady = !heavy && isBraced();
        if (!steady) {
            attacking = false; moving = false; attackAnimationTime = 0; moveAnimationTime = 0;
            if (enemyType == EnemyType.KNIGHT) {
                poiseTimer = heavy ? 0 : 1f;
                if (heavy) attackCooldownTimer = Math.max(attackCooldownTimer, .65f);
            }
        }

        // 플레이어 반대 방향으로 넉백
        if (getCenterX() < attackerX) {

            knockbackVelocityX =
                    -knockbackSpeed;

        } else {

            knockbackVelocityX =
                    knockbackSpeed;
        }
        if (steady) knockbackVelocityX = 0;
        else if (heavy) knockbackVelocityX *= 1.2f;

        System.out.println(
                getEnemyName() + " 체력: " + health
        );

        // 체력이 0이 되면 사망 애니메이션 시작
        if (health <= 0) {

            health = 0;
            if (deathAnimation != null) {

                dying = true;
                deathAnimationTime = 0;

                // 죽을 때 넉백을 조금 약하게 적용
                knockbackVelocityX *= 0.7f;

                System.out.println(
                        getEnemyName()
                                + " 사망 애니메이션 시작!"
                );

            } else {

                alive = false;

                System.out.println(
                        getEnemyName() + " 처치!"
                );
            }
        }
    }

    public void draw(SpriteBatch spriteBatch) {

        if (!alive) {
            return;
        }

        TextureRegion currentFrame;

        float currentWidth;
        float currentHeight;
        float currentYOffset;

        // 사망 애니메이션
        if (dying) {

            currentFrame =
                    deathAnimation.getKeyFrame(
                            deathAnimationTime,
                            false
                    );

            currentWidth =
                    deathSpriteWidth;

            currentHeight =
                    deathSpriteHeight;

            currentYOffset =
                    deathYOffset;
        }

        // 공격 애니메이션
        else if (attacking) {

            currentFrame =
                    attackAnimation.getKeyFrame(
                            attackAnimationTime,
                            false
                    );

            currentWidth =
                    attackSpriteWidth;

            currentHeight =
                    attackSpriteHeight;

            currentYOffset =
                    attackYOffset;
        }

        // 이동 애니메이션
        else if (moving) {

            currentFrame =
                    moveAnimation.getKeyFrame(
                            moveAnimationTime,
                            true
                    );

            currentWidth =
                    moveSpriteWidth;

            currentHeight =
                    moveSpriteHeight;

            currentYOffset =
                    moveYOffset;
        }

        // 가만히 있을 때
        else {

            currentFrame =
                    idleAnimation.getKeyFrame(
                            idleAnimationTime,
                            true
                    );

            currentWidth =
                    idleSpriteWidth;

            currentHeight =
                    idleSpriteHeight;

            currentYOffset =
                    idleYOffset;
        }

        // Repacked sheets preserve source pixels and share a fixed foot anchor.
        if (attacking && !dying && enemyType != EnemyType.DARK_SLIME) {
            AttackSheetLayout layout = AttackSheetLayout.get(enemyType == EnemyType.KNIGHT
                    ? "enemy/k_attack.png" : "enemy/slime attack.png");
            currentWidth = layout.width;
            currentHeight = layout.height;
            currentYOffset = -layout.foot;
        }

        if (currentFrame instanceof BodyFrame) {
            BodyFrame frame = (BodyFrame) currentFrame;
            currentWidth = frame.getRegionWidth() * frame.drawScale;
            currentHeight = frame.getRegionHeight() * frame.drawScale;
            currentYOffset = -frame.footFromBottom * frame.drawScale;
        }

        float deathAlpha = 1;
        if (dying && enemyType == EnemyType.SLIME) {
            float progress = Math.min(1, deathAnimationTime / deathAnimation.getAnimationDuration());
            float scale = 1 - .75f * progress;
            currentWidth *= scale;
            currentHeight *= scale;
            currentYOffset *= scale;
            deathAlpha = 1 - progress;
        } else if (dying) {
            deathAlpha = Math.min(1, Math.max(0, (deathAnimation.getAnimationDuration()
                    + deathLastFrameHold - deathAnimationTime) / deathLastFrameHold));
        }

        float drawX =
                x - (currentWidth - collisionWidth) / 2;

        float drawY =
                y + currentYOffset;

        // 피격 중 붉게 표시
        if (hitFlashTimer > 0 && !dying) {

            spriteBatch.setColor(
                    1f,
                    0.25f,
                    0.25f,
                    1f
            );

        } else {

            spriteBatch.setColor(
                    1f,
                    1f,
                    1f,
                    deathAlpha
            );
        }

        boolean drawWithoutFlip =
                facingRight == sourceFacesRight;

        if (drawWithoutFlip) {

            spriteBatch.draw(
                    currentFrame,
                    drawX,
                    drawY,
                    currentWidth,
                    currentHeight
            );

        } else {

            spriteBatch.draw(
                    currentFrame,
                    drawX + currentWidth,
                    drawY,
                    -currentWidth,
                    currentHeight
            );
        }

        // 다른 이미지에 색이 적용되지 않도록 초기화
        spriteBatch.setColor(
                1f,
                1f,
                1f,
                1f
        );
    }

    public void drawHealthBar(
            ShapeRenderer shapeRenderer) {

        if (!alive || dying) {
            return;
        }

        float barWidth = collisionWidth + 8;
        float barHeight = 4;

        float barX =
                x + collisionWidth / 2
                - barWidth / 2;

        float barY = y + collisionHeight + 8;

        if (isBraced()) {
            shapeRenderer.setColor(.94f, .76f, .39f, 1);
            shapeRenderer.rect(barX + barWidth / 2 - 3, barY + 8, 6, 6);
            shapeRenderer.triangle(barX + barWidth / 2 - 3, barY + 8,
                    barX + barWidth / 2 + 3, barY + 8, barX + barWidth / 2, barY + 4);
        }

        shapeRenderer.setColor(.38f, .34f, .26f, 1);
        shapeRenderer.rect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);

        // 체력바 배경
        shapeRenderer.setColor(
                0.15f,
                0.15f,
                0.15f,
                1
        );

        shapeRenderer.rect(
                barX,
                barY,
                barWidth,
                barHeight
        );

        float healthRatio =
                (float) health / maxHealth;

        // 현재 체력
        shapeRenderer.setColor(
                0.8f,
                0.1f,
                0.1f,
                1
        );

        shapeRenderer.rect(
                barX,
                barY,
                barWidth * healthRatio,
                barHeight
        );
    }

    public Rectangle getBounds() {

        return new Rectangle(
                x,
                y,
                collisionWidth,
                collisionHeight
        );
    }

    public Rectangle getAttackBounds() {
        float bottom = y + (enemyType == EnemyType.KNIGHT ? 12 : 0);
        float height = enemyType == EnemyType.KNIGHT ? 72 : collisionHeight;
        return new Rectangle(facingRight ? getCenterX() : getCenterX() - attackRange,
                bottom, attackRange, height);
    }

    public void update(float deltaTime, Player player, float worldWidth) {
        update(deltaTime, player);
        float bounded = Math.max(0, Math.min(worldWidth - collisionWidth, x));
        if (bounded != x) { x = bounded; knockbackVelocityX = 0; }
    }

    private float getCenterX() {

        return x + collisionWidth / 2;
    }

    private String getEnemyName() {

        if (enemyType == EnemyType.KNIGHT) {
            return "기사";
        }

        if (enemyType == EnemyType.DARK_SLIME) {
            return "검은 슬라임";
        }

        return "슬라임";
    }

    public boolean isAlive() {

        return alive;
    }

    /** 체력이 0이 되는 순간 한 번만 골드를 지급한다. 사망 애니메이션과 무관하다. */
    public int claimGoldReward() {
        if (health > 0 || goldRewardClaimed) {
            return 0;
        }
        goldRewardClaimed = true;
        if (enemyType == EnemyType.KNIGHT) return 20;
        if (enemyType == EnemyType.DARK_SLIME) return 30;
        return 10;
    }

    public boolean isAttacking() {

        return attacking && alive;
    }

    public boolean isDying() {

        return dying;
    }

    public void dispose() {

        idleTexture.dispose();
        moveTexture.dispose();
        attackTexture.dispose();
        if (deathTexture != null) {
            deathTexture.dispose();
        }
    }
}
