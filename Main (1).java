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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {

    private static final int STAGE_ONE = 1;
    private static final int STAGE_TWO = 2;
    private static final int STAGE_THREE = 3;
    private static final int STAGE_GODDESS_CHURCH = 4;

    private static final float VIEW_WIDTH = 640f;
    private static final float VIEW_HEIGHT = 360f;

    private static final float STAGE_ONE_WORLD_WIDTH = 2400f;
    private static final float STAGE_TWO_WORLD_WIDTH = 2200f;
    private static final float STAGE_THREE_WORLD_WIDTH = 2200f;
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

    // 스테이지 2
    private Texture stage2BackgroundTexture;
    private Texture stage2GroundTileTexture;

    // 스테이지 3
    private Texture stage3BackgroundTexture;
    private Texture stage3GroundTileTexture;

    // 여신의 교회
    private Texture goddessChurchBackgroundTexture;
    private Texture goddessChurchGroundTileTexture;

    private Texture platformTileTexture;

    private Texture shopIconTexture;
    private Texture shopPanelTexture;
    private Texture attackUpgradeTexture;
    private Texture healthUpgradeTexture;
    private Texture goldIconTexture;
    private Texture closeButtonTexture;
    private BitmapFont uiFont;

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

    private int gold = 0;
    private int attackUpgradeLevel = 1;
    private int healthUpgradeLevel = 1;
    private boolean shopOpen = false;
    private boolean deathHandled = false;
    private boolean gameClear = false;

    private static final int BASE_ATTACK_UPGRADE_COST = 50;
    private static final int BASE_HEALTH_UPGRADE_COST = 50;

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
        uiFont = new BitmapFont();
        uiFont.getData().setScale(0.85f);

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

        stage2BackgroundTexture =
                new Texture(
                        "background/stage2.png"
                );

        stage2GroundTileTexture =
                new Texture(
                        "tiles/stage2_tile.png"
                );

        stage3BackgroundTexture =
                new Texture(
                        "background/stage3.png"
                );

        stage3GroundTileTexture =
                new Texture(
                        "tiles/ground_tile.png"
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

        shopIconTexture = new Texture("ui/shop_icon.png");
        shopPanelTexture = new Texture("ui/shop_panel.png");
        attackUpgradeTexture = new Texture("ui/attack_upgrade.png");
        healthUpgradeTexture = new Texture("ui/health_upgrade.png");
        goldIconTexture = new Texture("ui/gold_icon.png");
        closeButtonTexture = new Texture("ui/close_button.png");

        setNearestFilter(stage1BackgroundTexture);
        setNearestFilter(stage1GroundTileTexture);
        setNearestFilter(stage2BackgroundTexture);
        setNearestFilter(stage2GroundTileTexture);
        setNearestFilter(stage3BackgroundTexture);
        setNearestFilter(stage3GroundTileTexture);
        setNearestFilter(goddessChurchBackgroundTexture);
        setNearestFilter(goddessChurchGroundTileTexture);
        setNearestFilter(platformTileTexture);
        setNearestFilter(shopIconTexture);
        setNearestFilter(shopPanelTexture);
        setNearestFilter(attackUpgradeTexture);
        setNearestFilter(healthUpgradeTexture);
        setNearestFilter(goldIconTexture);
        setNearestFilter(closeButtonTexture);

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

        } else if (currentStage == STAGE_TWO) {

            backgroundTexture =
                    stage2BackgroundTexture;

            groundTileTexture =
                    stage2GroundTileTexture;

        } else if (currentStage == STAGE_THREE) {

            backgroundTexture =
                    stage3BackgroundTexture;

            groundTileTexture =
                    stage3GroundTileTexture;

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

        } else if (currentStage == STAGE_TWO) {

            player = new Player(
                    100f,
                    GROUND_Y
            );

            fallenGod = null;

            enemies.add(
                    new Enemy(
                            600f,
                            GROUND_Y,
                            Enemy.EnemyType.KNIGHT
                    )
            );

            enemies.add(
                    new Enemy(
                            1000f,
                            GROUND_Y,
                            Enemy.EnemyType.KNIGHT
                    )
            );

            enemies.add(
                    new Enemy(
                            1450f,
                            GROUND_Y,
                            Enemy.EnemyType.KNIGHT
                    )
            );

            enemies.add(
                    new Enemy(
                            1900f,
                            GROUND_Y,
                            Enemy.EnemyType.KNIGHT
                    )
            );

        } else if (currentStage == STAGE_THREE) {

            player = new Player(
                    100f,
                    GROUND_Y
            );

            fallenGod = null;

            enemies.add(
                    new Enemy(
                            650f,
                            GROUND_Y,
                            Enemy.EnemyType.DARK_SLIME
                    )
            );

            enemies.add(
                    new Enemy(
                            1150f,
                            GROUND_Y,
                            Enemy.EnemyType.DARK_SLIME
                    )
            );

            enemies.add(
                    new Enemy(
                            1700f,
                            GROUND_Y,
                            Enemy.EnemyType.DARK_SLIME
                    )
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

        applyCurrentUpgradesToPlayer();
        deathHandled = false;
    }

    private void applyCurrentUpgradesToPlayer() {

        player.increaseAttackDamage(
                attackUpgradeLevel - 1
        );

        player.increaseMaxHealth(
                (healthUpgradeLevel - 1) * 2
        );
    }

    private void createPlatforms() {

        platforms.clear();

        if (currentStage != STAGE_ONE) {
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

        handleShopInput();

        if (gameClear || shopOpen) {
            return;
        }

        if (!player.isAlive()) {
            handlePlayerDeath();
            return;
        }

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

        if (!player.isAlive()) {
            handlePlayerDeath();
            return;
        }

        checkPlayerAttack();
        checkBossClear();

        if (gameClear) {
            return;
        }

        checkStageChange();

    }

    private void handlePlayerDeath() {

        if (!deathHandled) {
            resetRunProgress();
            shopOpen = false;
            deathHandled = true;
        }

        if (Gdx.input.isKeyJustPressed(
                Input.Keys.R
        )) {
            restartGame();
        }
    }

    private void checkBossClear() {

        if (currentStage != STAGE_GODDESS_CHURCH ||
                fallenGod == null ||
                fallenGod.isAlive() ||
                gameClear) {
            return;
        }

        gameClear = true;
        shopOpen = false;
        resetRunProgress();

        System.out.println(
                "게임 클리어! 골드가 초기화되었습니다."
        );
    }

    private void checkStageChange() {

        if (currentStage == STAGE_GODDESS_CHURCH) {
            return;
        }

        float currentStageWidth =
                getCurrentWorldWidth();

        boolean reachedStageEnd =
                player.getX()
                        >= currentStageWidth - 100f;

        boolean testKeyPressed =
                Gdx.input.isKeyJustPressed(
                        Input.Keys.N
                );

        if (!reachedStageEnd && !testKeyPressed) {
            return;
        }

        if (currentStage == STAGE_ONE) {
            changeStage(
                    STAGE_TWO,
                    "성벽 스테이지 시작!"
            );

        } else if (currentStage == STAGE_TWO) {
            changeStage(
                    STAGE_THREE,
                    "어두운 전장 스테이지 시작!"
            );

        } else if (currentStage == STAGE_THREE) {
            changeStage(
                    STAGE_GODDESS_CHURCH,
                    "여신의 교회 스테이지 시작!"
            );
        }
    }

    private void changeStage(
            int nextStage,
            String startMessage) {

        disposeGameObjects();

        currentStage = nextStage;

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
                startMessage
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

    private void handleShopInput() {

        if (shopOpen && Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
        )) {
            shopOpen = false;
            return;
        }

        if (!Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
        )) {
            return;
        }

        Vector2 clickPosition = new Vector2(
                Gdx.input.getX(),
                Gdx.input.getY()
        );

        viewport.unproject(clickPosition);

        if (!shopOpen) {

            if (player.isAlive() &&
                    !gameClear &&
                    getShopIconBounds().contains(
                            clickPosition
                    )) {
                shopOpen = true;
            }

            return;
        }

        if (getCloseButtonBounds().contains(
                clickPosition
        )) {
            shopOpen = false;
            return;
        }

        if (getAttackUpgradeBounds().contains(
                clickPosition
        )) {
            buyAttackUpgrade();
            return;
        }

        if (getHealthUpgradeBounds().contains(
                clickPosition
        )) {
            buyHealthUpgrade();
        }
    }

    private void buyAttackUpgrade() {

        int cost = getAttackUpgradeCost();

        if (gold < cost) {
            return;
        }

        gold -= cost;
        attackUpgradeLevel++;
        player.increaseAttackDamage(1);
    }

    private void buyHealthUpgrade() {

        int cost = getHealthUpgradeCost();

        if (gold < cost) {
            return;
        }

        gold -= cost;
        healthUpgradeLevel++;
        player.increaseMaxHealth(2);
    }

    private int getAttackUpgradeCost() {

        return BASE_ATTACK_UPGRADE_COST
                * attackUpgradeLevel;
    }

    private int getHealthUpgradeCost() {

        return BASE_HEALTH_UPGRADE_COST
                * healthUpgradeLevel;
    }

    private void resetRunProgress() {

        gold = 0;
        attackUpgradeLevel = 1;
        healthUpgradeLevel = 1;
    }

    public void returnToMainMenu() {

        resetRunProgress();
        shopOpen = false;
        gameClear = false;
    }

    private void restartGame() {

        resetRunProgress();
        shopOpen = false;
        gameClear = false;

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

        if (currentStage == STAGE_TWO) {
            return STAGE_TWO_WORLD_WIDTH;
        }

        if (currentStage == STAGE_THREE) {
            return STAGE_THREE_WORLD_WIDTH;
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
                        player.getAttackDamage(),
                        player.getCenterX()
                );

                gold += enemy.claimGoldReward();

                enemiesHitThisAttack.add(enemy);
            }
        }

        if (fallenGod != null &&
                fallenGod.isAlive() &&
                !bossHitThisAttack &&
                attackBounds.overlaps(
                        fallenGod.getBounds()
                )) {

            fallenGod.takeDamage(
                    player.getAttackDamage()
            );
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

        drawHudAndShop();
    }

    private void drawHudAndShop() {

        float screenLeft =
                camera.position.x - VIEW_WIDTH / 2f;

        float screenTop =
                camera.position.y + VIEW_HEIGHT / 2f;

        spriteBatch.begin();

        spriteBatch.draw(
                goldIconTexture,
                screenLeft + 20f,
                screenTop - 72f,
                24f,
                24f
        );

        uiFont.setColor(1f, 0.95f, 0.82f, 1f);
        uiFont.draw(
                spriteBatch,
                "GOLD " + gold,
                screenLeft + 50f,
                screenTop - 54f
        );

        if (player.isAlive() && !gameClear) {

            Rectangle shopBounds =
                    getShopIconBounds();

            spriteBatch.draw(
                    shopIconTexture,
                    shopBounds.x,
                    shopBounds.y,
                    shopBounds.width,
                    shopBounds.height
            );
        }

        if (shopOpen) {
            drawShopPanel();
        }

        if (!player.isAlive()) {

            uiFont.setColor(1f, 0.3f, 0.3f, 1f);
            uiFont.draw(
                    spriteBatch,
                    "GAME OVER - PRESS R TO RETRY",
                    camera.position.x - 115f,
                    camera.position.y + 20f
            );
        }

        if (gameClear) {

            uiFont.setColor(1f, 0.95f, 0.75f, 1f);
            uiFont.draw(
                    spriteBatch,
                    "GAME CLEAR",
                    camera.position.x - 45f,
                    camera.position.y + 20f
            );
        }

        uiFont.setColor(1f, 1f, 1f, 1f);
        spriteBatch.end();
    }

    private void drawShopPanel() {

        Rectangle panelBounds =
                getShopPanelBounds();

        spriteBatch.draw(
                shopPanelTexture,
                panelBounds.x,
                panelBounds.y,
                panelBounds.width,
                panelBounds.height
        );

        Rectangle closeBounds =
                getCloseButtonBounds();

        spriteBatch.draw(
                closeButtonTexture,
                closeBounds.x,
                closeBounds.y,
                closeBounds.width,
                closeBounds.height
        );

        Rectangle attackBounds =
                getAttackUpgradeBounds();

        Rectangle healthBounds =
                getHealthUpgradeBounds();

        spriteBatch.draw(
                attackUpgradeTexture,
                attackBounds.x + 39f,
                attackBounds.y + 55f,
                32f,
                32f
        );

        spriteBatch.draw(
                healthUpgradeTexture,
                healthBounds.x + 39f,
                healthBounds.y + 55f,
                32f,
                32f
        );

        uiFont.setColor(1f, 0.95f, 0.82f, 1f);

        uiFont.draw(
                spriteBatch,
                "ATK +1  Lv." + attackUpgradeLevel,
                attackBounds.x + 13f,
                attackBounds.y + 48f
        );

        uiFont.draw(
                spriteBatch,
                getAttackUpgradeCost() + " GOLD",
                attackBounds.x + 22f,
                attackBounds.y + 28f
        );

        uiFont.draw(
                spriteBatch,
                "HP +2  Lv." + healthUpgradeLevel,
                healthBounds.x + 17f,
                healthBounds.y + 48f
        );

        uiFont.draw(
                spriteBatch,
                getHealthUpgradeCost() + " GOLD",
                healthBounds.x + 22f,
                healthBounds.y + 28f
        );
    }

    private Rectangle getShopIconBounds() {

        float screenRight =
                camera.position.x + VIEW_WIDTH / 2f;

        float screenTop =
                camera.position.y + VIEW_HEIGHT / 2f;

        return new Rectangle(
                screenRight - 64f,
                screenTop - 60f,
                48f,
                48f
        );
    }

    private Rectangle getShopPanelBounds() {

        return new Rectangle(
                camera.position.x - 150f,
                camera.position.y - 100f,
                300f,
                200f
        );
    }

    private Rectangle getCloseButtonBounds() {

        Rectangle panel = getShopPanelBounds();

        return new Rectangle(
                panel.x + 264f,
                panel.y + 164f,
                28f,
                28f
        );
    }

    private Rectangle getAttackUpgradeBounds() {

        Rectangle panel = getShopPanelBounds();

        return new Rectangle(
                panel.x + 25f,
                panel.y + 35f,
                110f,
                110f
        );
    }

    private Rectangle getHealthUpgradeBounds() {

        Rectangle panel = getShopPanelBounds();

        return new Rectangle(
                panel.x + 165f,
                panel.y + 35f,
                110f,
                110f
        );
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

        stage2BackgroundTexture.dispose();
        stage2GroundTileTexture.dispose();
        stage3BackgroundTexture.dispose();
        stage3GroundTileTexture.dispose();

        goddessChurchBackgroundTexture.dispose();
        goddessChurchGroundTileTexture.dispose();

        platformTileTexture.dispose();
        shopIconTexture.dispose();
        shopPanelTexture.dispose();
        attackUpgradeTexture.dispose();
        healthUpgradeTexture.dispose();
        goldIconTexture.dispose();
        closeButtonTexture.dispose();
        uiFont.dispose();

        if (backgroundMusic != null) {

            backgroundMusic.stop();
            backgroundMusic.dispose();
        }
    }
}
