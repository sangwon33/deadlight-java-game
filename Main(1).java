package com.example.pixelgame;

import java.util.ArrayList;
import java.util.HashSet;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {

    private static final int STAGE_ONE = 1;
    private static final int STAGE_GODDESS_CHURCH = 2;

    private static final float VIEW_WIDTH = 640f;
    private static final float VIEW_HEIGHT = 360f;

    private static final float STAGE_ONE_WORLD_WIDTH = 2400f;
    private static final float GODDESS_CHURCH_WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 360f;

    private static final float GROUND_Y = 60f;
    private static final float GROUND_TILE_SIZE = 64f;

    private static final float PLATFORM_TILE_WIDTH = 64f;
    private static final float PLATFORM_TILE_HEIGHT = 32f;

    private OrthographicCamera camera;
    private Viewport viewport;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;

    private int currentStage = STAGE_ONE;

    private Texture backgroundTexture;
    private Texture groundTileTexture;

    // 스테이지 1
    private Texture stage1BackgroundTexture;
    private Texture stage1GroundTileTexture;

    // 여신의 교회
    private Texture goddessChurchBackgroundTexture;
    private Texture goddessChurchGroundTileTexture;

    private Texture platformTileTexture;

    private Music backgroundMusic;
    private boolean musicMuted = false;

    private Player player;
    private FallenGod fallenGod;

    private final ArrayList<Enemy> enemies =
            new ArrayList<>();

    private final HashSet<Enemy> enemiesHitThisAttack =
            new HashSet<>();

    private final ArrayList<Rectangle> platforms =
            new ArrayList<>();

    private boolean bossHitThisAttack = false;

    @Override
    public void create() {

        camera = new OrthographicCamera();

        viewport = new FitViewport(
                VIEW_WIDTH,
                VIEW_HEIGHT,
                camera
        );

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();

        loadTextures();
        loadBackgroundMusic();

        createPlatforms();
        createGameObjects();

        camera.position.set(
                VIEW_WIDTH / 2f,
                WORLD_HEIGHT / 2f,
                0f
        );

        camera.update();
    }

    private void loadTextures() {

        stage1BackgroundTexture =
                new Texture(
                        "background/stage1.png"
                );

        stage1GroundTileTexture =
                new Texture(
                        "tiles/tile1.png"
                );

        goddessChurchBackgroundTexture =
                new Texture(
                        "background/goddess_church_background.png"
                );

        goddessChurchGroundTileTexture =
                new Texture(
                        "tiles/goddess_church_ground_tile.png"
                );

        platformTileTexture =
                new Texture(
                        "tiles/wood.png"
                );

        setNearestFilter(stage1BackgroundTexture);
        setNearestFilter(stage1GroundTileTexture);
        setNearestFilter(goddessChurchBackgroundTexture);
        setNearestFilter(goddessChurchGroundTileTexture);
        setNearestFilter(platformTileTexture);

        selectStageTextures();
    }

    private void setNearestFilter(Texture texture) {

        texture.setFilter(
                TextureFilter.Nearest,
                TextureFilter.Nearest
        );
    }

    private void selectStageTextures() {

        if (currentStage == STAGE_GODDESS_CHURCH) {

            backgroundTexture =
                    goddessChurchBackgroundTexture;

            groundTileTexture =
                    goddessChurchGroundTileTexture;

        } else {

            backgroundTexture =
                    stage1BackgroundTexture;

            groundTileTexture =
                    stage1GroundTileTexture;
        }
    }

    private void loadBackgroundMusic() {

        backgroundMusic =
                Gdx.audio.newMusic(
                        Gdx.files.internal(
                                "music/stage1_bgm.mp3"
                        )
                );

        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.25f);
        backgroundMusic.play();
    }

    private void createGameObjects() {

        enemies.clear();
        enemiesHitThisAttack.clear();

        bossHitThisAttack = false;

        if (currentStage == STAGE_ONE) {

            player = new Player(
                    100f,
                    GROUND_Y
            );

            fallenGod = null;

            enemies.add(
                    new Enemy(700f, GROUND_Y)
            );

            enemies.add(
                    new Enemy(1100f, GROUND_Y)
            );

            enemies.add(
                    new Enemy(1550f, GROUND_Y)
            );

            enemies.add(
                    new Enemy(2100f, GROUND_Y)
            );

        } else {

            player = new Player(
                    150f,
                    GROUND_Y
            );

            fallenGod = new FallenGod(
                    440f,
                    90f,
                    GODDESS_CHURCH_WORLD_WIDTH
            );
        }
    }

    private void createPlatforms() {

        platforms.clear();

        if (currentStage == STAGE_GODDESS_CHURCH) {
            return;
        }

        platforms.add(
                new Rectangle(
                        250f,
                        130f,
                        180f,
                        25f
                )
        );

        platforms.add(
                new Rectangle(
                        520f,
                        230f,
                        180f,
                        25f
                )
        );

        platforms.add(
                new Rectangle(
                        900f,
                        150f,
                        200f,
                        25f
                )
        );

        platforms.add(
                new Rectangle(
                        1250f,
                        260f,
                        200f,
                        25f
                )
        );

        platforms.add(
                new Rectangle(
                        1650f,
                        170f,
                        220f,
                        25f
                )
        );

        platforms.add(
                new Rectangle(
                        2050f,
                        280f,
                        200f,
                        25f
                )
        );
    }

    @Override
    public void render() {

        float deltaTime = Math.min(
                Gdx.graphics.getDeltaTime(),
                1f / 30f
        );

        updateGame(deltaTime);
        updateCamera();
        drawGame();
    }

    private void updateGame(float deltaTime) {

        updateMusicInput();

        player.update(
                deltaTime,
                platforms,
                getCurrentWorldWidth()
        );

        for (Enemy enemy : enemies) {

            enemy.update(
                    deltaTime,
                    player
            );
        }

        if (fallenGod != null &&
                fallenGod.isAlive()) {

            fallenGod.update(
                    deltaTime,
                    player
            );
        }

        checkPlayerAttack();
        checkStageChange();

        if (!player.isAlive() &&
                Gdx.input.isKeyJustPressed(
                        Input.Keys.R
                )) {

            restartGame();
        }
    }

    private void checkStageChange() {

        if (currentStage != STAGE_ONE) {
            return;
        }

        boolean reachedStageEnd =
                player.getX()
                        >= STAGE_ONE_WORLD_WIDTH - 100f;

        boolean testKeyPressed =
                Gdx.input.isKeyJustPressed(
                        Input.Keys.N
                );

        if (reachedStageEnd || testKeyPressed) {
            changeToGoddessChurch();
        }
    }

    private void changeToGoddessChurch() {

        disposeGameObjects();

        currentStage = STAGE_GODDESS_CHURCH;

        selectStageTextures();
        createPlatforms();
        createGameObjects();

        camera.position.set(
                VIEW_WIDTH / 2f,
                WORLD_HEIGHT / 2f,
                0f
        );

        camera.update();

        System.out.println(
                "여신의 교회 스테이지 시작!"
        );
    }

    private void updateMusicInput() {

        if (!Gdx.input.isKeyJustPressed(
                Input.Keys.M
        )) {
            return;
        }

        musicMuted = !musicMuted;

        backgroundMusic.setVolume(
                musicMuted ? 0f : 0.25f
        );

        System.out.println(
                musicMuted
                        ? "배경 음악 음소거"
                        : "배경 음악 재생"
        );
    }

    private void restartGame() {

        disposeGameObjects();

        selectStageTextures();
        createPlatforms();
        createGameObjects();
    }

    private void disposeGameObjects() {

        if (player != null) {

            player.dispose();
            player = null;
        }

        for (Enemy enemy : enemies) {
            enemy.dispose();
        }

        enemies.clear();

        if (fallenGod != null) {

            fallenGod.dispose();
            fallenGod = null;
        }
    }

    private void updateCamera() {

        float halfViewWidth =
                VIEW_WIDTH / 2f;

        float currentWorldWidth =
                getCurrentWorldWidth();

        camera.position.x =
                MathUtils.clamp(
                        player.getCenterX(),
                        halfViewWidth,
                        currentWorldWidth
                                - halfViewWidth
                );

        camera.position.y =
                WORLD_HEIGHT / 2f;

        camera.update();
    }

    private float getCurrentWorldWidth() {

        if (currentStage ==
                STAGE_GODDESS_CHURCH) {

            return GODDESS_CHURCH_WORLD_WIDTH;
        }

        return STAGE_ONE_WORLD_WIDTH;
    }

    private void checkPlayerAttack() {

        if (!player.isAttacking()) {

            enemiesHitThisAttack.clear();
            bossHitThisAttack = false;

            return;
        }

        Rectangle attackBounds =
                player.getAttackBounds();

        for (Enemy enemy : enemies) {

            if (!enemy.isAlive() ||
                    enemiesHitThisAttack.contains(enemy)) {

                continue;
            }

            if (attackBounds.overlaps(
                    enemy.getBounds()
            )) {

                enemy.takeDamage(
                        1,
                        player.getCenterX()
                );

                enemiesHitThisAttack.add(enemy);
            }
        }

        if (fallenGod != null &&
                fallenGod.isAlive() &&
                !bossHitThisAttack &&
                attackBounds.overlaps(
                        fallenGod.getBounds()
                )) {

            fallenGod.takeDamage(1);
            bossHitThisAttack = true;
        }
    }

    private void drawGame() {

        viewport.apply();

        ScreenUtils.clear(
                0.03f,
                0.03f,
                0.06f,
                1f
        );

        spriteBatch.setProjectionMatrix(
                camera.combined
        );

        shapeRenderer.setProjectionMatrix(
                camera.combined
        );

        spriteBatch.begin();

        drawBackground();
        drawGroundTiles();
        drawPlatformTiles();

        for (Enemy enemy : enemies) {
            enemy.draw(spriteBatch);
        }

        if (fallenGod != null) {
            fallenGod.draw(spriteBatch);
        }

        player.draw(spriteBatch);

        spriteBatch.end();

        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        drawEnemyHealthBars();
        drawBossHealthBar();
        drawAttackBounds();
        drawPlayerHealthBar();

        shapeRenderer.end();
    }

    private void drawBackground() {

        if (currentStage ==
                STAGE_GODDESS_CHURCH) {

            spriteBatch.draw(
                    backgroundTexture,
                    0f,
                    0f,
                    GODDESS_CHURCH_WORLD_WIDTH,
                    VIEW_HEIGHT
            );

            return;
        }

        float screenLeft =
                camera.position.x
                        - VIEW_WIDTH / 2f;

        float parallaxAmount =
                screenLeft * 0.15f;

        float firstBackgroundX =
                screenLeft - parallaxAmount;

        while (firstBackgroundX > screenLeft) {
            firstBackgroundX -= VIEW_WIDTH;
        }

        while (firstBackgroundX + VIEW_WIDTH
                < screenLeft) {

            firstBackgroundX += VIEW_WIDTH;
        }

        for (int i = -1; i <= 2; i++) {

            spriteBatch.draw(
                    backgroundTexture,
                    firstBackgroundX
                            + i * VIEW_WIDTH,
                    0f,
                    VIEW_WIDTH,
                    VIEW_HEIGHT
            );
        }
    }

    private void drawGroundTiles() {

        float currentWorldWidth =
                getCurrentWorldWidth();

        float groundDrawY =
                GROUND_Y - GROUND_TILE_SIZE;

        for (float tileX = 0f;
             tileX < currentWorldWidth;
             tileX += GROUND_TILE_SIZE) {

            float drawWidth =
                    Math.min(
                            GROUND_TILE_SIZE,
                            currentWorldWidth - tileX
                    );

            spriteBatch.draw(
                    groundTileTexture,
                    tileX,
                    groundDrawY,
                    drawWidth,
                    GROUND_TILE_SIZE
            );
        }
    }

    private void drawPlatformTiles() {

        for (Rectangle platform : platforms) {

            float platformTop =
                    platform.y + platform.height;

            float drawY =
                    platformTop
                            - PLATFORM_TILE_HEIGHT;

            float platformEndX =
                    platform.x + platform.width;

            for (float tileX = platform.x;
                 tileX < platformEndX;
                 tileX += PLATFORM_TILE_WIDTH) {

                float drawWidth =
                        Math.min(
                                PLATFORM_TILE_WIDTH,
                                platformEndX - tileX
                        );

                spriteBatch.draw(
                        platformTileTexture,
                        tileX,
                        drawY,
                        drawWidth,
                        PLATFORM_TILE_HEIGHT
                );
            }
        }
    }

    private void drawEnemyHealthBars() {

        for (Enemy enemy : enemies) {

            enemy.drawHealthBar(
                    shapeRenderer
            );
        }
    }

    private void drawBossHealthBar() {

        if (fallenGod == null) {
            return;
        }

        float screenTop =
                camera.position.y
                        + VIEW_HEIGHT / 2f;

        fallenGod.drawHealthBar(
                shapeRenderer,
                camera.position.x,
                screenTop
        );
    }

    private void drawAttackBounds() {

        if (player.isAttacking()) {

            Rectangle attackBounds =
                    player.getAttackBounds();

            shapeRenderer.setColor(
                    1f,
                    0.8f,
                    0.1f,
                    0.45f
            );

            shapeRenderer.rect(
                    attackBounds.x,
                    attackBounds.y,
                    attackBounds.width,
                    attackBounds.height
            );
        }

        if (fallenGod != null &&
                fallenGod.isAttacking()) {

            Rectangle attackBounds =
                    fallenGod.getAttackBounds();

            shapeRenderer.setColor(
                    0.9f,
                    0.05f,
                    0.1f,
                    0.25f
            );

            shapeRenderer.rect(
                    attackBounds.x,
                    attackBounds.y,
                    attackBounds.width,
                    attackBounds.height
            );
        }
    }

    private void drawPlayerHealthBar() {

        float screenLeft =
                camera.position.x
                        - VIEW_WIDTH / 2f;

        float screenTop =
                camera.position.y
                        + VIEW_HEIGHT / 2f;

        float barX = screenLeft + 20f;
        float barY = screenTop - 35f;

        float barWidth = 200f;
        float barHeight = 18f;

        shapeRenderer.setColor(
                0.15f,
                0.15f,
                0.15f,
                1f
        );

        shapeRenderer.rect(
                barX,
                barY,
                barWidth,
                barHeight
        );

        shapeRenderer.setColor(
                0.1f,
                0.8f,
                0.2f,
                1f
        );

        shapeRenderer.rect(
                barX,
                barY,
                barWidth
                        * player.getHealthRatio(),
                barHeight
        );
    }

    @Override
    public void resize(
            int width,
            int height) {

        viewport.update(
                width,
                height,
                false
        );
    }

    @Override
    public void dispose() {

        disposeGameObjects();

        shapeRenderer.dispose();
        spriteBatch.dispose();

        stage1BackgroundTexture.dispose();
        stage1GroundTileTexture.dispose();

        goddessChurchBackgroundTexture.dispose();
        goddessChurchGroundTileTexture.dispose();

        platformTileTexture.dispose();

        if (backgroundMusic != null) {

            backgroundMusic.stop();
            backgroundMusic.dispose();
        }
    }
}
