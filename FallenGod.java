package com.example.pixelgame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import java.util.IdentityHashMap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class FallenGod {
    public int getHealth() { return health; }
    public int getMaxHealth() { return MAX_HEALTH; }


    private enum State {
        FLOAT,
        STAB_HIGH,
        STAB_MID,
        STAB_LOW,
        FIRE_SLASH
    }

    private float x;
    private float y;

    private final float worldWidth;

    private static final float COLLISION_WIDTH = 90f;
    private static final float COLLISION_HEIGHT = 150f;

    private static final float STOP_DISTANCE = 155f;
    private static final float MOVE_SPEED = 60f;

    private static final float ATTACK_COOLDOWN = 1.3f;

    private boolean facingRight = false;

    private static final int MAX_HEALTH = 30;

    private int health = MAX_HEALTH;
    private boolean alive = true;

    private State state = State.FLOAT;

    private float floatAnimationTime = 0f;
    private float stateTime = 0f;
    private float attackCooldownTimer = 1f;
    private static final float WINDUP = .35f;
    private float windupTimer;

    private boolean attackHit = false;

    private Texture floatTexture;
    private Texture stabHighTexture;
    private Texture stabMidTexture;
    private Texture stabLowTexture;
    private Texture fireSlashTexture;

    private Animation<TextureRegion> floatAnimation;
    private Animation<TextureRegion> stabHighAnimation;
    private Animation<TextureRegion> stabMidAnimation;
    private Animation<TextureRegion> stabLowAnimation;
    private Animation<TextureRegion> fireSlashAnimation;
    private final IdentityHashMap<TextureRegion, Float> floatFootRatios = new IdentityHashMap<>();

    public FallenGod(
            float startX,
            float startY,
            float worldWidth) {

        x = startX;
        y = startY;

        this.worldWidth = worldWidth;

        loadTextures();
        createAnimations();
    }

    private void loadTextures() {

        floatTexture = new Texture(
                "boss/fall_float.png"
        );

        stabHighTexture = new Texture(
                "boss/fall_high.png"
        );

        stabMidTexture = new Texture(
                "boss/fall_mid.png"
        );

        stabLowTexture = new Texture(
                "boss/fall_low.png"
        );

        fireSlashTexture = new Texture(
                "boss/fall_fire.png"
        );

        setNearestFilter(floatTexture);
        setNearestFilter(stabHighTexture);
        setNearestFilter(stabMidTexture);
        setNearestFilter(stabLowTexture);
        setNearestFilter(fireSlashTexture);
    }

    private void setNearestFilter(Texture texture) {

        texture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );
    }

    private void createAnimations() {

        floatAnimation = createAnimation(
                floatTexture,
                6,
                0.18f,
                Animation.PlayMode.LOOP_PINGPONG
        );

        stabHighAnimation = createAnimation(
                stabHighTexture,
                6,
                0.13f,
                Animation.PlayMode.NORMAL
        );

        stabMidAnimation = createAnimation(
                stabMidTexture,
                6,
                0.13f,
                Animation.PlayMode.NORMAL
        );

        stabLowAnimation = createAnimation(
                stabLowTexture,
                6,
                0.13f,
                Animation.PlayMode.NORMAL
        );

        fireSlashAnimation = createAnimation(
                fireSlashTexture,
                8,
                0.11f,
                Animation.PlayMode.NORMAL
        );
        Pixmap pixels = new Pixmap(Gdx.files.internal("boss/fall_float.png"));
        try {
            for (TextureRegion frame : floatAnimation.getKeyFrames())
                floatFootRatios.put(frame, FrameContent.footFromBottom(pixels, frame) / (float)frame.getRegionHeight());
        } finally { pixels.dispose(); }
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

        for (int i = 0; i < frameCount; i++) {

            frames[i] = new TextureRegion(
                    texture,
                    i * frameWidth,
                    0,
                    frameWidth,
                    frameHeight
            );
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

        if (!alive || !player.isAlive()) {
            return;
        }

        floatAnimationTime += deltaTime;

        updateAttackCooldown(deltaTime);

        if (state == State.FLOAT) {

            // Commit to the telegraphed direction until this attack finishes.
            facingRight = player.getCenterX() > getCenterX();

            followPlayer(deltaTime, player);
            tryStartAttack(player);

        } else {

            updateAttack(deltaTime, player);
        }
    }

    private void updateAttackCooldown(
            float deltaTime) {

        if (attackCooldownTimer <= 0f) {
            return;
        }

        attackCooldownTimer -= deltaTime;

        if (attackCooldownTimer < 0f) {
            attackCooldownTimer = 0f;
        }
    }

    private void followPlayer(
            float deltaTime,
            Player player) {

        float differenceX =
                player.getCenterX() - getCenterX();

        float distanceX =
                Math.abs(differenceX);

        if (distanceX > STOP_DISTANCE) {

            x += Math.signum(differenceX)
                    * MOVE_SPEED
                    * deltaTime;
        }

        float targetY =
                MathUtils.clamp(
                        player.getY() + 30f,
                        82f,
                        115f
                );

        y = MathUtils.lerp(
                y,
                targetY,
                Math.min(
                        1f,
                        deltaTime * 1.4f
                )
        );

        x = MathUtils.clamp(
                x,
                0f,
                worldWidth - COLLISION_WIDTH
        );
    }

    private void tryStartAttack(Player player) {

        if (attackCooldownTimer > 0f) {
            return;
        }

        float distanceX =
                Math.abs(
                        player.getCenterX()
                                - getCenterX()
                );

        if (distanceX > 235f) {
            return;
        }

        int attackNumber =
                MathUtils.random(0, 3);

        switch (attackNumber) {

            case 0:
                state = State.STAB_HIGH;
                break;

            case 1:
                state = State.STAB_MID;
                break;

            case 2:
                state = State.STAB_LOW;
                break;

            default:
                state = State.FIRE_SLASH;
                break;
        }

        stateTime = 0f;
        attackHit = false;
        windupTimer = WINDUP;
    }

    private void updateAttack(
            float deltaTime,
            Player player) {

        if (windupTimer > 0) {
            windupTimer = Math.max(0, windupTimer - deltaTime);
            return;
        }
        stateTime += deltaTime;

        Animation<TextureRegion> animation =
                getCurrentAnimation();

        int frameIndex =
                animation.getKeyFrameIndex(
                        stateTime
                );

        boolean damageFrame =
                isDamageFrame(frameIndex);

        if (damageFrame &&
                !attackHit &&
                getAttackBounds().overlaps(
                        player.getBounds()
                )) {

            int damage = 1;

            player.takeDamage(
                    damage,
                    getCenterX()
            );

            attackHit = true;

            System.out.println(
                    "Fallen God 공격 적중! 피해량: "
                            + damage
            );
        }

        if (animation.isAnimationFinished(
                stateTime
        )) {

            state = State.FLOAT;
            stateTime = 0f;
            attackHit = false;

            attackCooldownTimer =
                    ATTACK_COOLDOWN;
        }
    }

    private boolean isDamageFrame(
            int frameIndex) {

        switch (state) {

            case FIRE_SLASH:

                return frameIndex >= 4 &&
                        frameIndex <= 6;

            case STAB_HIGH:
            case STAB_MID:
            case STAB_LOW:

                return frameIndex >= 3 &&
                        frameIndex <= 4;

            default:

                return false;
        }
    }

    private Animation<TextureRegion>
    getCurrentAnimation() {

        switch (state) {

            case STAB_HIGH:
                return stabHighAnimation;

            case STAB_MID:
                return stabMidAnimation;

            case STAB_LOW:
                return stabLowAnimation;

            case FIRE_SLASH:
                return fireSlashAnimation;

            case FLOAT:
            default:
                return floatAnimation;
        }
    }

    private TextureRegion getCurrentFrame() {

        if (state == State.FLOAT) {

            return floatAnimation.getKeyFrame(
                    floatAnimationTime,
                    true
            );
        }

        return getCurrentAnimation().getKeyFrame(
                stateTime,
                false
        );
    }

    public void takeDamage(int damage) {

        if (!alive) {
            return;
        }

        health -= damage;

        System.out.println(
                "Fallen God 체력: " + health
        );

        if (health <= 0) {

            health = 0;
            alive = false;

            System.out.println(
                    "Fallen God 처치!"
            );
        }
    }

    public void draw(SpriteBatch spriteBatch) {

        if (!alive) {
            return;
        }

        TextureRegion currentFrame =
                getCurrentFrame();

        float currentWidth;
        float currentHeight;
        float currentYOffset;

        switch (state) {

        case STAB_HIGH:
            currentWidth = 125f;
            currentHeight = 250f;
            currentYOffset = -50f;
            break;

        case STAB_MID:
            currentWidth = 125f;
            currentHeight = 250f;
            currentYOffset = -50f;
            break;

        case STAB_LOW:
            currentWidth = 125f;
            currentHeight = 250f;
            currentYOffset = -50f;
            break;

        case FIRE_SLASH:
            currentWidth = 125f;
            currentHeight = 250f;
            currentYOffset = -50f;
            break;

        case FLOAT:
        default:
            currentWidth = currentFrame.getRegionWidth() * 250f / currentFrame.getRegionHeight();
            currentHeight = 250f;
            currentYOffset = -currentHeight * floatFootRatios.get(currentFrame);
            break;
    }

        if (state != State.FLOAT) {
            String asset = state == State.STAB_HIGH ? "boss/fall_high.png"
                    : state == State.STAB_MID ? "boss/fall_mid.png"
                    : state == State.STAB_LOW ? "boss/fall_low.png" : "boss/fall_fire.png";
            AttackSheetLayout layout = AttackSheetLayout.get(asset);
            currentWidth = layout.width;
            currentHeight = layout.height;
            currentYOffset = -layout.foot;
        }

        float floatingOffset =
                MathUtils.sin(
                        floatAnimationTime * 2f
                ) * 5f;

        float drawX =
                x - (
                        currentWidth
                                - COLLISION_WIDTH
                ) / 2f;

        float drawY =
                y
                        + currentYOffset
                        + floatingOffset;

        if (facingRight) {

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
    }

    public Rectangle getBounds() {

        return new Rectangle(
                x,
                y,
                COLLISION_WIDTH,
                COLLISION_HEIGHT
        );
    }

    public Rectangle getAttackBounds() {

        float attackWidth;
        float attackHeight;
        float attackY;

        switch (state) {

            case STAB_HIGH:

                attackWidth = 220f;
                attackHeight = 55f;
                attackY = y + 90f;
                break;

            case STAB_MID:

                attackWidth = 220f;
                attackHeight = 70f;
                attackY = y + 25f;
                break;

            case STAB_LOW:

                attackWidth = 220f;
                attackHeight = 70f;
                attackY = y - 40f;
                break;

            case FIRE_SLASH:

                attackWidth = 245f;
                attackHeight = 180f;
                attackY = y - 45f;
                break;

            default:

                return new Rectangle(
                        x,
                        y,
                        0f,
                        0f
                );
        }

        float attackX;

        if (facingRight) {

            attackX =
                    x
                            + COLLISION_WIDTH
                            - 10f;

        } else {

            attackX =
                    x
                            - attackWidth
                            + 10f;
        }

        return new Rectangle(
                attackX,
                attackY,
                attackWidth,
                attackHeight
        );
    }

    public void drawHealthBar(
            ShapeRenderer shapeRenderer,
            float cameraCenterX,
            float screenTop) {

        if (!alive) {
            return;
        }

        float barWidth = 360f;
        float barHeight = 12f;

        float barX =
                cameraCenterX
                        - barWidth / 2f;

        float barY =
                screenTop - 62f;

        shapeRenderer.setColor(
                0.08f,
                0.03f,
                0.04f,
                1f
        );

        shapeRenderer.rect(
                barX,
                barY,
                barWidth,
                barHeight
        );

        float healthRatio =
                (float) health / MAX_HEALTH;

        shapeRenderer.setColor(
                0.75f,
                0.05f,
                0.12f,
                1f
        );

        shapeRenderer.rect(
                barX,
                barY,
                barWidth * healthRatio,
                barHeight
        );
    }

    public float getCenterX() {

        return x
                + COLLISION_WIDTH / 2f;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isAttacking() {

        return alive &&
                state != State.FLOAT;
    }

    public float getWarningSeconds() {
        if (!alive || state == State.FLOAT) return 0;
        return Math.max(0, windupTimer + (state == State.FIRE_SLASH ? .44f : .39f) - stateTime);
    }

    public String getWarningLabel() {
        if (getWarningSeconds() <= 0) return "";
        String height = switch (state) {
            case STAB_HIGH -> "HIGH STRIKE";
            case STAB_MID -> "MID STRIKE";
            case STAB_LOW -> "LOW STRIKE";
            default -> "FIRE SWEEP";
        };
        return height + (facingRight ? "  >" : "  <");
    }

    /** Preview the same rectangle used for damage, with a label as well as color. */
    public void drawTelegraph(ShapeRenderer s) {
        float warning = getWarningSeconds();
        if (warning <= 0) return;
        Rectangle r = getAttackBounds();
        boolean blend = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        float red = state == State.FIRE_SLASH ? .8f : 1f;
        float green = state == State.STAB_HIGH ? .78f : state == State.STAB_MID ? .48f : .28f;
        float blue = state == State.FIRE_SLASH ? .85f : .22f;
        s.begin(ShapeRenderer.ShapeType.Filled);
        s.setColor(red, green, blue, .08f);
        s.rect(r.x, r.y, r.width, r.height);
        s.setColor(red, green, blue, .85f);
        for (float x = 0; x < r.width; x += 16) {
            float width = Math.min(9, r.width - x);
            s.rect(r.x + x, r.y, width, 1.2f);
            s.rect(r.x + x, r.y + r.height - 1.2f, width, 1.2f);
        }
        s.rect(r.x, r.y, 1.2f, r.height);
        s.rect(r.x + r.width - 1.2f, r.y, 1.2f, r.height);
        float cx = r.x + r.width / 2, cy = r.y + r.height / 2;
        float dir = facingRight ? 1 : -1;
        s.rectLine(cx - dir * 12, cy, cx + dir * 12, cy, 2);
        s.triangle(cx + dir * 18, cy, cx + dir * 7, cy - 6, cx + dir * 7, cy + 6);
        s.end();
        if (!blend) Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void dispose() {

        floatTexture.dispose();
        stabHighTexture.dispose();
        stabMidTexture.dispose();
        stabLowTexture.dispose();
        fireSlashTexture.dispose();
    }
}
