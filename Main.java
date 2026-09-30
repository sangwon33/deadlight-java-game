package com.example.pixelgame;



import java.util.ArrayList;

import java.util.HashSet;



import com.badlogic.gdx.ApplicationAdapter;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Input;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.graphics.Texture.TextureFilter;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

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



    private enum GameMode { PLAYING, PAUSED, MENU, GAME_OVER, CLEARED }

    private enum MenuPage { MAIN, STAGE_SELECT, SETTINGS }

    private GameMode gameMode = GameMode.MENU;

    private MenuPage menuPage = MenuPage.MAIN;
    private boolean settingsFromPause = false;
    private boolean confirmExit = false;
    private int highestUnlockedStage = STAGE_ONE;
    private float musicVolume = 0.25f;
    private float effectsVolume = 0.7f;
    private boolean screenShakeEnabled = true;
    private float shakeTimer = 0f;
    private float shakeStrength = 0f;
    private float bannerTimer = 0f;
    private String bannerText = "";
    private float potionEffectTimer = 0f;
    private int lastPlayerHealth = -1;
    private int windowWidth = 1280, windowHeight = 720;
    private Sound hitSound, buySound, potionSound;
    private static class FloatingText {
        float x, y, life = 1f;
        String label;
        FloatingText(String label, float x, float y) {
            this.label = label; this.x = x; this.y = y;
        }
    }
    private final ArrayList<FloatingText> floatingTexts = new ArrayList<>();



    private static final int ATTACK_PRICE = 50;

    private static final int HEALTH_PRICE = 50;

    private static final int HEAL_POTION_PRICE = 20;

    private static final int ATTACK_POTION_PRICE = 35;

    private static final float MERCHANT_FRAME_TIME = 0.38f;



    // 한 번의 플레이 동안 유지되고 종료되면 모두 초기화된다.

    private int gold = 0;

    private int attackLevels = 0;

    private int healthLevels = 0;

    private int healPotions = 0;

    private int attackPotions = 0;

    private int savedHealth = 5;

    private float savedBuffTime = 0f;

    private boolean shopOpen = false;

    private boolean inventoryOpen = false;

    private String shopMessage = "";

    private float shopMessageTimer = 0f;

    private float merchantAnimationTime = 0f;

    // 스테이지 1 시작 구간에 표시되는 조작 튜토리얼
    private boolean attackTutorialDone = false;
    private boolean dashTutorialDone = false;
    private boolean inventoryTutorialDone = false;
    private float tutorialFloatTime = 0f;



    private Texture merchantTexture;

    private TextureRegion[] merchantFrames;

    private Texture merchantInteractIcon;

    private Texture shopPanelTexture;

    private Texture closeIcon;

    private Texture goldIcon;

    private Texture attackUpgradeIcon;

    private Texture healthUpgradeIcon;

    private Texture healPotionIcon;

    private Texture attackPotionIcon;

    private Texture mainMenuBackgroundTexture;

    private Texture titleLogoTexture;

    private Texture menuButtonNormalTexture;

    private Texture menuButtonHoverTexture;

    private Texture menuButtonPressedTexture;

    private BitmapFont uiFont;

    private GlyphLayout menuTextLayout;



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

        uiFont = new BitmapFont();

        uiFont.getData().setScale(0.9f);

        menuTextLayout = new GlyphLayout();



        loadTextures();

        loadShopTextures();

        loadMainMenuTextures();

        loadBackgroundMusic();
        hitSound = optionalSound("music/hit.wav");
        buySound = optionalSound("music/buy.wav");
        potionSound = optionalSound("music/potion.wav");



        createPlatforms();

        createGameObjects();

        lastPlayerHealth = player.getHealth();



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



        setNearestFilter(stage1BackgroundTexture);

        setNearestFilter(stage1GroundTileTexture);

        setNearestFilter(stage2BackgroundTexture);

        setNearestFilter(stage2GroundTileTexture);

        setNearestFilter(stage3BackgroundTexture);

        setNearestFilter(stage3GroundTileTexture);

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



    private Texture loadOptionalTexture(String path) {

        if (!Gdx.files.internal(path).exists()) {

            System.out.println("UI asset missing: " + path);

            return null;

        }

        Texture texture = new Texture(Gdx.files.internal(path));

        setNearestFilter(texture);

        return texture;

    }



    private void loadShopTextures() {

        merchantTexture = loadOptionalTexture("npc/merchant_idle.png");

        if (merchantTexture != null) {

            if (merchantTexture.getWidth() >= 384 && merchantTexture.getHeight() >= 128) {

                merchantFrames = new TextureRegion[4];

                for (int i = 0; i < 4; i++) {

                    merchantFrames[i] = new TextureRegion(merchantTexture, i * 96, 0, 96, 128);

                }

            } else {

                merchantFrames = new TextureRegion[]{new TextureRegion(merchantTexture)};

            }

        }

        merchantInteractIcon = loadOptionalTexture("ui/merchant_interact_icon.png");

        shopPanelTexture = loadOptionalTexture("ui/shop_panel.png");

        closeIcon = loadOptionalTexture("ui/close_button.png");

        goldIcon = loadOptionalTexture("ui/gold_icon_v2.png");

        attackUpgradeIcon = loadOptionalTexture("ui/attack_upgrade.png");

        healthUpgradeIcon = loadOptionalTexture("ui/health_upgrade.png");

        healPotionIcon = loadOptionalTexture("items/health_potion.png");

        attackPotionIcon = loadOptionalTexture("items/attack_buff_potion.png");

    }



    private void loadMainMenuTextures() {

        mainMenuBackgroundTexture =
                loadOptionalTexture("background/title_character_background.png");

        titleLogoTexture =
                loadOptionalTexture("ui/deadlight_title.png");

        menuButtonNormalTexture =
                loadOptionalTexture("ui/menu_button_normal.png");

        menuButtonHoverTexture =
                loadOptionalTexture("ui/menu_button_hover.png");

        menuButtonPressedTexture =
                loadOptionalTexture("ui/menu_button_pressed.png");

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

        backgroundMusic.setVolume(musicVolume);

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



        // 새 스테이지에서도 이전 강화/체력/버프를 이어받는다.

        player.restoreRunStats(attackLevels, healthLevels, savedHealth, savedBuffTime);

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



        if (gameMode == GameMode.PAUSED) {
            updatePausedInput();
            if (gameMode == GameMode.PAUSED) {
                drawGame();
                drawPauseOverlay();
            } else if (gameMode == GameMode.MENU) drawMenuScreen();
            else drawGame();
        } else if (gameMode == GameMode.PLAYING) {

            if (shopOpen) updateShopInput(deltaTime);

            else if (inventoryOpen) updateInventoryInput();

            else updateGame(deltaTime);

            if (gameMode == GameMode.PLAYING) {

                updateCamera();

                drawGame();

            } else if (gameMode == GameMode.PAUSED) {
                drawGame();
                drawPauseOverlay();
            } else {
                drawMenuScreen();
            }

        } else {

            updateMenuInput();

            if (gameMode == GameMode.PAUSED) {
                drawGame();
                drawPauseOverlay();
            } else if (gameMode == GameMode.PLAYING) drawGame();
            else drawMenuScreen();

        }

    }



    private void updateGame(float deltaTime) {
        bannerTimer = Math.max(0f, bannerTimer - deltaTime);
        potionEffectTimer = Math.max(0f, potionEffectTimer - deltaTime);
        shakeTimer = Math.max(0f, shakeTimer - deltaTime);
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText item = floatingTexts.get(i);
            item.y += 23f * deltaTime;
            item.life -= deltaTime;
            if (item.life <= 0f) floatingTexts.remove(i);
        }



        updateMusicInput();

        if (currentStage == STAGE_ONE) {

            tutorialFloatTime += deltaTime;

            if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
                attackTutorialDone = true;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) ||
                    Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {
                dashTutorialDone = true;
            }
        }



        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            gameMode = GameMode.PAUSED;
            confirmExit = false;

            return;

        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {

            if (currentStage == STAGE_ONE) {
                inventoryTutorialDone = true;
            }

            inventoryOpen = true;

            return;

        }

        merchantAnimationTime += deltaTime;

        if (isNearMerchant() &&

                (Gdx.input.isKeyJustPressed(Input.Keys.E) ||

                 (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) &&

                  isMouseInside(merchantIconBounds())))) {

            shopOpen = true;

            shopMessage = "";

            shopMessageTimer = 0f;

            return;

        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) useHealPotion();

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) useAttackPotion();



        player.update(

                deltaTime,

                platforms,

                getCurrentWorldWidth()

        );
        if (lastPlayerHealth >= 0 && player.getHealth() < lastPlayerHealth) {
            addFloating("-" + (lastPlayerHealth - player.getHealth()),
                    player.getCenterX(), player.getY() + 90f);
            shake(5f);
            playSound(hitSound);
        }
        lastPlayerHealth = player.getHealth();



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

        for (Enemy enemy : enemies) {
            int reward = enemy.claimGoldReward();
            if (reward > 0) {
                gold += reward;
                addFloating("+" + reward + "G", enemy.getBounds().x,
                        enemy.getBounds().y + 65f);
            }
        }



        if (!player.isAlive()) {

            finishRun(GameMode.GAME_OVER);

            return;

        }

        if (fallenGod != null && !fallenGod.isAlive()) {

            finishRun(GameMode.CLEARED);

            return;

        }

        checkStageChange();

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



        savedHealth = player.getHealth();

        savedBuffTime = player.getAttackPotionTimer();

        disposeGameObjects();



        currentStage = nextStage;
        highestUnlockedStage = Math.max(highestUnlockedStage, nextStage);
        showBanner("STAGE CLEAR  /  " + stageName(nextStage));



        selectStageTextures();

        createPlatforms();

        createGameObjects();
        lastPlayerHealth = player.getHealth();



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

                musicMuted ? 0f : musicVolume

        );



        System.out.println(

                musicMuted

                        ? "배경 음악 음소거"

                        : "배경 음악 재생"

        );

    }



    private void restartGame() {

        startStage(STAGE_ONE);

    }



    private void startStage(int stage) {



        resetRunData();

        disposeGameObjects();



        currentStage = stage;

        selectStageTextures();

        createPlatforms();

        createGameObjects();
        lastPlayerHealth = player.getHealth();
        floatingTexts.clear();
        showBanner(stageName(stage));

        camera.position.set(VIEW_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);

        camera.update();

        gameMode = GameMode.PLAYING;

        menuPage = MenuPage.MAIN;

    }



    private void resetRunData() {
        floatingTexts.clear();
        potionEffectTimer = 0f;
        shakeTimer = 0f;

        gold = 0;

        attackLevels = 0;

        healthLevels = 0;

        healPotions = 0;

        attackPotions = 0;

        savedHealth = 5;

        savedBuffTime = 0f;

        shopOpen = false;

        inventoryOpen = false;

        shopMessage = "";

        shopMessageTimer = 0f;

        attackTutorialDone = false;

        dashTutorialDone = false;

        inventoryTutorialDone = false;

        tutorialFloatTime = 0f;

    }



    private void finishRun(GameMode result) {
        if (result == GameMode.CLEARED) highestUnlockedStage = STAGE_GODDESS_CHURCH;

        resetRunData();

        gameMode = result;

    }



    private void returnToMainMenu() {

        resetRunData();

        gameMode = GameMode.MENU;

        menuPage = MenuPage.MAIN;

        camera.position.set(VIEW_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);

        camera.update();

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
        if (shakeTimer > 0f && screenShakeEnabled) {
            camera.position.x += MathUtils.random(-shakeStrength, shakeStrength);
            camera.position.y += MathUtils.random(-shakeStrength, shakeStrength);
        }



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



                int healthBeforeHit = enemy.getHealth();
                enemy.takeDamage(

                        player.getAttackDamage(),

                        player.getCenterX()

                );
                if (enemy.getHealth() < healthBeforeHit) {
                    addFloating("-" + (healthBeforeHit - enemy.getHealth()),
                            enemy.getBounds().x, enemy.getBounds().y + 55f);
                    shake(2f);
                    playSound(hitSound);
                }



                enemiesHitThisAttack.add(enemy);

            }

        }



        if (fallenGod != null &&

                fallenGod.isAlive() &&

                !bossHitThisAttack &&

                attackBounds.overlaps(

                        fallenGod.getBounds()

                )) {



            fallenGod.takeDamage(player.getAttackDamage());
            addFloating("-" + player.getAttackDamage(),
                    fallenGod.getBounds().x, fallenGod.getBounds().y + 95f);
            shake(4f);
            playSound(hitSound);

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



        drawMerchant();



        for (Enemy enemy : enemies) {

            enemy.draw(spriteBatch);

        }



        if (fallenGod != null) {

            fallenGod.draw(spriteBatch);

        }



        player.draw(spriteBatch);
        drawFloatingTexts();
        if (potionEffectTimer > 0f) {
            uiFont.setColor(0.75f, 1f, 0.65f, potionEffectTimer);
            uiFont.draw(spriteBatch, "POTION", player.getCenterX() - 22f,
                    player.getY() + 105f);
            uiFont.setColor(1f, 1f, 1f, 1f);
        }

        drawStageOneTutorialHints();



        spriteBatch.end();



        shapeRenderer.begin(

                ShapeRenderer.ShapeType.Filled

        );



        drawEnemyHealthBars();

        drawBossHealthBar();

        drawAttackBounds();

        drawPlayerHealthBar();



        shapeRenderer.end();



        if (shopOpen) drawShopShapes();

        if (inventoryOpen) drawInventoryShapes();



        spriteBatch.begin();

        if (!shopOpen) drawHud();

        if (shopOpen) drawShop();

        if (inventoryOpen) drawInventory();
        if (bannerTimer > 0f) {
            uiFont.setColor(1f, 0.87f, 0.62f, Math.min(1f, bannerTimer));
            menuTextLayout.setText(uiFont, bannerText);
            uiFont.draw(spriteBatch, bannerText,
                    screenLeft() + (VIEW_WIDTH - menuTextLayout.width) / 2f, 298f);
            uiFont.setColor(1f, 1f, 1f, 1f);
        }

        spriteBatch.end();

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



    private float screenLeft() {

        return camera.position.x - VIEW_WIDTH / 2f;

    }



    private float merchantX() {

        return getCurrentWorldWidth() - 165f;

    }



    private boolean isNearMerchant() {

        return currentStage != STAGE_GODDESS_CHURCH && player != null &&

                player.isAlive() &&

                Math.abs(player.getCenterX() - (merchantX() + 48f)) <= 115f &&

                Math.abs(player.getY() - GROUND_Y) <= 70f;

    }



    private Rectangle merchantIconBounds() {

        return new Rectangle(merchantX() + 32f, GROUND_Y + 121f, 38f, 38f);

    }



    private boolean isMouseInside(Rectangle bounds) {

        Vector2 cursor = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        return bounds.contains(cursor);

    }



    private void drawMerchant() {

        if (currentStage == STAGE_GODDESS_CHURCH) return;

        if (merchantFrames != null) {

            int frame = ((int) (merchantAnimationTime / MERCHANT_FRAME_TIME))

                    % merchantFrames.length;

            spriteBatch.draw(merchantFrames[frame], merchantX(), GROUND_Y - 9f, 96f, 128f);

        }

        if (isNearMerchant()) {

            Rectangle prompt = merchantIconBounds();

            if (merchantInteractIcon != null) {

                spriteBatch.draw(merchantInteractIcon, prompt.x, prompt.y,

                        prompt.width, prompt.height);

            } else {

                uiFont.draw(spriteBatch, "E SHOP", prompt.x, prompt.y + 23f);

            }

        }

    }



    private void drawStageOneTutorialHints() {

        if (currentStage != STAGE_ONE || shopOpen || inventoryOpen) {
            return;
        }

        float floatingY = MathUtils.sin(tutorialFloatTime * 2.4f) * 3f;

        if (!attackTutorialDone) {
            drawTutorialHint("[J] ATTACK", 150f, 205f + floatingY);
        }

        if (!dashTutorialDone) {
            drawTutorialHint("[SHIFT] DASH", 325f, 205f + floatingY);
        }

        if (!inventoryTutorialDone) {
            drawTutorialHint("[I] INVENTORY", 520f, 292f + floatingY);
        }
    }



    private void drawTutorialHint(String text, float x, float y) {

        // 그림자를 먼저 그려 밝은 스테이지 1 배경에서도 잘 보이게 한다.
        uiFont.setColor(0f, 0f, 0f, 0.9f);
        uiFont.draw(spriteBatch, text, x + 1.5f, y - 1.5f);

        uiFont.setColor(1f, 0.92f, 0.72f, 1f);
        uiFont.draw(spriteBatch, text, x, y);

        uiFont.setColor(1f, 1f, 1f, 1f);
    }



    private void showShopMessage(String message) {

        shopMessage = message;

        shopMessageTimer = 1.4f;

    }



    private Rectangle closeBounds() {

        return new Rectangle(screenLeft() + 531f, 283f, 35f, 35f);

    }



    private Rectangle buyBounds(int row) {

        return new Rectangle(screenLeft() + 459f, 220f - row * 48f, 68f, 30f);

    }



    private void updateShopInput(float deltaTime) {

        if (shopMessageTimer > 0f) shopMessageTimer -= deltaTime;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            shopOpen = false;

            return;

        }

        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;

        if (isMouseInside(closeBounds())) {

            shopOpen = false;

            return;

        }

        for (int row = 0; row < 4; row++) {

            if (isMouseInside(buyBounds(row))) {

                purchase(row);

                return;

            }

        }

    }



    private void purchase(int row) {

        int price;

        switch (row) {

            case 0: price = ATTACK_PRICE; break;

            case 1: price = HEALTH_PRICE; break;

            case 2: price = HEAL_POTION_PRICE; break;

            case 3: price = ATTACK_POTION_PRICE; break;

            default: return;

        }

        if (gold < price) {

            showShopMessage("Not enough gold!");

            return;

        }

        gold -= price;

        switch (row) {

            case 0:

                attackLevels++;

                player.upgradeAttack();

                break;

            case 1:

                healthLevels++;

                player.upgradeMaxHealth();

                break;

            case 2:

                healPotions++;

                break;

            case 3:

                attackPotions++;

                break;

            default: break;

        }

        showShopMessage("Purchase complete!");
        playSound(buySound);

    }



    private void useHealPotion() {

        if (healPotions > 0 && player.heal(30)) {
            healPotions--;
            potionEffectTimer = 0.85f;
            playSound(potionSound);
        }

    }



    private void useAttackPotion() {

        if (attackPotions > 0 && player.isAlive()) {

            attackPotions--;

            player.drinkAttackPotion();
            potionEffectTimer = 0.85f;
            playSound(potionSound);

        }

    }



    private Rectangle inventoryUseBounds(int row) {

        return new Rectangle(screenLeft() + 388f, 176f - row * 55f, 56f, 30f);

    }



    private void updateInventoryInput() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.I) ||

                Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            inventoryOpen = false;

            return;

        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) useHealPotion();

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) useAttackPotion();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {

            if (isMouseInside(inventoryUseBounds(0))) useHealPotion();

            else if (isMouseInside(inventoryUseBounds(1))) useAttackPotion();

        }

    }



    private void drawInventoryShapes() {

        float x = screenLeft();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.03f, 0.03f, 0.04f, 1f);

        shapeRenderer.rect(x + 155f, 81f, 330f, 215f);

        shapeRenderer.setColor(0.32f, 0.25f, 0.17f, 1f);

        shapeRenderer.rect(inventoryUseBounds(0).x, inventoryUseBounds(0).y, 56f, 30f);

        shapeRenderer.rect(inventoryUseBounds(1).x, inventoryUseBounds(1).y, 56f, 30f);

        shapeRenderer.end();

    }



    private void drawInventory() {

        float x = screenLeft();

        uiFont.draw(spriteBatch, "INVENTORY [I to close]", x + 174f, 269f);

        if (healPotionIcon != null) spriteBatch.draw(healPotionIcon, x + 176f, 174f, 38f, 38f);

        uiFont.draw(spriteBatch, "Heal potion x" + healPotions, x + 225f, 193f);

        uiFont.draw(spriteBatch, "USE", x + 402f, 197f);

        if (attackPotionIcon != null) spriteBatch.draw(attackPotionIcon, x + 176f, 119f, 38f, 38f);

        uiFont.draw(spriteBatch, "Power potion x" + attackPotions, x + 225f, 138f);

        uiFont.draw(spriteBatch, "USE", x + 402f, 142f);

    }



    private void drawHud() {

        float x = screenLeft();

        if (goldIcon != null) spriteBatch.draw(goldIcon, x + 508f, 325f, 20f, 20f);

        uiFont.draw(spriteBatch, "GOLD " + gold, x + 531f, 341f);

        if (healPotionIcon != null) spriteBatch.draw(healPotionIcon, x + 22f, 274f, 24f, 24f);

        uiFont.draw(spriteBatch, "1 x" + healPotions, x + 48f, 292f);

        if (attackPotionIcon != null) spriteBatch.draw(attackPotionIcon, x + 100f, 274f, 24f, 24f);

        uiFont.draw(spriteBatch, "2 x" + attackPotions, x + 126f, 292f);

        if (player.getAttackPotionTimer() > 0f) {

            uiFont.draw(spriteBatch, "ATK +3: " +

                    (int) Math.ceil(player.getAttackPotionTimer()) + "s", x + 185f, 292f);

        }

    }



    private void drawShop() {

        float x = screenLeft();

        uiFont.draw(spriteBatch, "MERCHANT", x + 88f, 312f);

        if (goldIcon != null) spriteBatch.draw(goldIcon, x + 352f, 286f, 20f, 20f);

        uiFont.draw(spriteBatch, "GOLD " + gold, x + 377f, 303f);

        if (closeIcon != null) {

            Rectangle b = closeBounds();

            spriteBatch.draw(closeIcon, b.x, b.y, b.width, b.height);

        } else {

            uiFont.draw(spriteBatch, "X", x + 544f, 307f);

        }

        drawShopRow(0, attackUpgradeIcon, "Attack upgrade", "Damage +1 (this run)", ATTACK_PRICE);

        drawShopRow(1, healthUpgradeIcon, "Health upgrade", "Max HP +10 (this run)", HEALTH_PRICE);

        drawShopRow(2, healPotionIcon, "Heal potion", "Inventory: restore 30 HP [1]", HEAL_POTION_PRICE);

        drawShopRow(3, attackPotionIcon, "Power potion", "Inventory: ATK +3 for 10s [2]", ATTACK_POTION_PRICE);

        if (shopMessageTimer > 0f) {

            uiFont.draw(spriteBatch, shopMessage, x + 213f, 56f);

        }

    }



    private void drawShopShapes() {

        float x = screenLeft();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.02f, 0.02f, 0.03f, 0.75f);

        shapeRenderer.rect(x, 0f, VIEW_WIDTH, VIEW_HEIGHT);

        if (shopPanelTexture == null) {

            shapeRenderer.setColor(0.12f, 0.09f, 0.08f, 1f);

            shapeRenderer.rect(x + 60f, 20f, 520f, 320f);

        }

        shapeRenderer.end();

        if (shopPanelTexture != null) {

            spriteBatch.begin();

            spriteBatch.draw(shopPanelTexture, x + 60f, 20f, 520f, 320f);

            spriteBatch.end();

        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int row = 0; row < 4; row++) {

            boolean hovered = isMouseInside(buyBounds(row));
            shapeRenderer.setColor(hovered ? 0.29f : 0.18f,
                    hovered ? 0.21f : 0.14f, 0.12f, 0.92f);

            shapeRenderer.rect(x + 78f, 214f - row * 48f, 460f, 45f);

            Rectangle b = buyBounds(row);

            shapeRenderer.setColor(gold >= new int[]{ATTACK_PRICE, HEALTH_PRICE,

                    HEAL_POTION_PRICE, ATTACK_POTION_PRICE}[row]

                    ? (hovered ? 0.56f : 0.36f) : 0.19f, 0.25f, 0.12f, 1f);

            shapeRenderer.rect(b.x, b.y, b.width, b.height);

        }

        shapeRenderer.end();

    }



    private void drawShopRow(int row, Texture icon, String name, String info, int price) {

        float x = screenLeft();

        float top = 258f - row * 48f;

        if (icon != null) spriteBatch.draw(icon, x + 88f, top - 37f, 32f, 32f);

        uiFont.draw(spriteBatch, name, x + 127f, top - 11f);

        uiFont.draw(spriteBatch, info, x + 127f, top - 28f);

        uiFont.draw(spriteBatch, price + "G", x + 410f, top - 17f);

        uiFont.draw(spriteBatch, "BUY", x + 476f, top - 17f);

    }



    private void updateMenuInput() {

        if (gameMode != GameMode.MENU) {

            updateResultScreenInput();

            return;

        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            if (menuPage == MenuPage.SETTINGS && settingsFromPause) {
                gameMode = GameMode.PAUSED;
                settingsFromPause = false;
            } else if (menuPage != MenuPage.MAIN) {
                menuPage = MenuPage.MAIN;
            }

            return;

        }

        if (menuPage == MenuPage.MAIN &&
                Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {

            restartGame();

            return;

        }

        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            return;
        }

        if (menuPage == MenuPage.MAIN) {

            if (isMouseInside(mainMenuButtonBounds(0))) {
                restartGame();
            } else if (isMouseInside(mainMenuButtonBounds(1))) {
                menuPage = MenuPage.STAGE_SELECT;
            } else if (isMouseInside(mainMenuButtonBounds(2))) {
                settingsFromPause = false;
                menuPage = MenuPage.SETTINGS;
            } else if (isMouseInside(mainMenuButtonBounds(3))) {
                Gdx.app.exit();
            }

        } else if (menuPage == MenuPage.STAGE_SELECT) {

            for (int row = 0; row < 4; row++) {
                if (row + 1 <= highestUnlockedStage &&
                        isMouseInside(subMenuButtonBounds(row))) {
                    startStage(row + 1);
                    return;
                }
            }

            if (isMouseInside(subMenuButtonBounds(4))) {
                menuPage = MenuPage.MAIN;
            }

        } else {
            updateSettingsClick();
        }

    }



    private void updateResultScreenInput() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.R)) {

            if (gameMode == GameMode.CLEARED) returnToMainMenu();
            else restartGame();

            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            returnToMainMenu();
            return;
        }

        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            return;
        }

        if (isMouseInside(resultButtonBounds(0))) {
            if (gameMode == GameMode.CLEARED) returnToMainMenu();
            else restartGame();
        } else if (gameMode == GameMode.GAME_OVER &&
                isMouseInside(resultButtonBounds(1))) {
            returnToMainMenu();
        }
    }



    private Rectangle mainMenuButtonBounds(int row) {
        return new Rectangle(screenLeft() + 42f, 180f - row * 57f, 240f, 56f);
    }



    private Rectangle subMenuButtonBounds(int row) {
        return new Rectangle(screenLeft() + 55f, 220f - row * 48f, 210f, 44f);
    }



    private Rectangle settingsButtonBounds(int row) {
        return new Rectangle(screenLeft() + 55f, 215f - row * 45f, 240f, 42f);
    }



    private Rectangle resultButtonBounds(int row) {
        return new Rectangle(screenLeft() + 200f, 135f - row * 66f, 240f, 56f);
    }



    private void drawMenuScreen() {

        viewport.apply();

        ScreenUtils.clear(0.03f, 0.03f, 0.06f, 1f);

        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();

        drawMenuBackground();

        if (gameMode == GameMode.MENU) {

            if (menuPage == MenuPage.MAIN) drawMainMenu();
            else if (menuPage == MenuPage.STAGE_SELECT) drawStageSelectMenu();
            else drawSettingsMenu();

        } else {

            drawResultMenu();
        }

        spriteBatch.end();

    }



    private void drawMenuBackground() {

        if (mainMenuBackgroundTexture != null) {
            spriteBatch.draw(mainMenuBackgroundTexture, screenLeft(), 0f,
                    VIEW_WIDTH, VIEW_HEIGHT);
        }
    }



    private void drawMainMenu() {

        float x = screenLeft();

        if (titleLogoTexture != null) {
            spriteBatch.draw(titleLogoTexture, x + 22f, 244f, 300f, 96f);
        } else {
            drawCenteredMenuText("DEADLIGHT",
                    new Rectangle(x + 22f, 244f, 300f, 96f));
        }

        drawMenuButton(mainMenuButtonBounds(0), "START GAME");
        drawMenuButton(mainMenuButtonBounds(1), "STAGE SELECT");
        drawMenuButton(mainMenuButtonBounds(2), "SETTINGS");
        drawMenuButton(mainMenuButtonBounds(3), "EXIT");
    }



    private void drawStageSelectMenu() {

        float x = screenLeft();

        uiFont.setColor(1f, 0.92f, 0.72f, 1f);
        uiFont.draw(spriteBatch, "STAGE SELECT", x + 102f, 306f);
        uiFont.setColor(1f, 1f, 1f, 1f);

        for (int stage = 1; stage <= 4; stage++) {
            drawMenuButton(subMenuButtonBounds(stage - 1),
                    stage <= highestUnlockedStage ? stageName(stage) : "LOCKED");
        }
        drawMenuButton(subMenuButtonBounds(4), "BACK");
    }



    private void drawSettingsMenu() {

        float x = screenLeft();

        uiFont.setColor(1f, 0.92f, 0.72f, 1f);
        uiFont.draw(spriteBatch, "SETTINGS", x + 112f, 278f);
        uiFont.setColor(1f, 1f, 1f, 1f);

        drawMenuButton(settingsButtonBounds(0),
                "MUSIC " + (musicMuted ? "OFF" : (int)(musicVolume * 400f) + "%"));
        drawMenuButton(settingsButtonBounds(1), "SFX " + (int)(effectsVolume * 100f) + "%");
        drawMenuButton(settingsButtonBounds(2),
                Gdx.graphics.isFullscreen() ? "FULLSCREEN" : "WINDOWED");
        drawMenuButton(settingsButtonBounds(3),
                screenShakeEnabled ? "SHAKE ON" : "SHAKE OFF");
        drawMenuButton(settingsButtonBounds(4), "BACK");
    }



    private void drawResultMenu() {

        float x = screenLeft();
        String title = gameMode == GameMode.GAME_OVER ? "GAME OVER" : "BOSS CLEARED";

        drawCenteredMenuText(title, new Rectangle(x + 150f, 225f, 340f, 70f));

        drawMenuButton(resultButtonBounds(0),
                gameMode == GameMode.GAME_OVER ? "RETRY" : "MAIN MENU");

        if (gameMode == GameMode.GAME_OVER) {
            drawMenuButton(resultButtonBounds(1), "MAIN MENU");
        }
    }



    private void drawMenuButton(Rectangle bounds, String label) {

        boolean hovered = isMouseInside(bounds);
        boolean pressed = hovered && Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        Texture buttonTexture = menuButtonNormalTexture;

        if (pressed && menuButtonPressedTexture != null) {
            buttonTexture = menuButtonPressedTexture;
        } else if (hovered && menuButtonHoverTexture != null) {
            buttonTexture = menuButtonHoverTexture;
        }

        if (buttonTexture != null) {
            spriteBatch.draw(buttonTexture, bounds.x, bounds.y,
                    bounds.width, bounds.height);
        }

        drawCenteredMenuText(label, bounds);
    }

    private void updatePausedInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            confirmExit = false;
            gameMode = GameMode.PLAYING;
            return;
        }
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        if (confirmExit) {
            if (isMouseInside(pauseButtonBounds(1))) {
                confirmExit = false;
                returnToMainMenu();
            } else if (isMouseInside(pauseButtonBounds(0))) confirmExit = false;
            return;
        }
        if (isMouseInside(pauseButtonBounds(0))) gameMode = GameMode.PLAYING;
        else if (isMouseInside(pauseButtonBounds(1))) restartGame();
        else if (isMouseInside(pauseButtonBounds(2))) {
            settingsFromPause = true;
            menuPage = MenuPage.SETTINGS;
            gameMode = GameMode.MENU;
        } else if (isMouseInside(pauseButtonBounds(3))) confirmExit = true;
    }

    private Rectangle pauseButtonBounds(int row) {
        return new Rectangle(screenLeft() + 200f, 205f - row * 45f, 240f, 42f);
    }

    private void drawPauseOverlay() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.72f);
        shapeRenderer.rect(screenLeft(), 0f, VIEW_WIDTH, VIEW_HEIGHT);
        shapeRenderer.end();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        drawCenteredMenuText(confirmExit ? "RETURN TO MENU?" : "PAUSED",
                new Rectangle(screenLeft() + 160f, 265f, 320f, 55f));
        if (confirmExit) {
            drawMenuButton(pauseButtonBounds(0), "CANCEL");
            drawMenuButton(pauseButtonBounds(1), "CONFIRM");
        } else {
            drawMenuButton(pauseButtonBounds(0), "CONTINUE");
            drawMenuButton(pauseButtonBounds(1), "RESTART");
            drawMenuButton(pauseButtonBounds(2), "SETTINGS");
            drawMenuButton(pauseButtonBounds(3), "MAIN MENU");
        }
        spriteBatch.end();
    }

    private void updateSettingsClick() {
        if (isMouseInside(settingsButtonBounds(0))) {
            musicVolume = musicVolume >= 0.5f ? 0f : musicVolume + 0.125f;
            musicMuted = musicVolume == 0f;
            backgroundMusic.setVolume(musicVolume);
        } else if (isMouseInside(settingsButtonBounds(1))) {
            effectsVolume = effectsVolume >= 1f ? 0f : Math.min(1f, effectsVolume + 0.25f);
        } else if (isMouseInside(settingsButtonBounds(2))) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(windowWidth, windowHeight);
            } else {
                windowWidth = Gdx.graphics.getWidth();
                windowHeight = Gdx.graphics.getHeight();
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        } else if (isMouseInside(settingsButtonBounds(3))) {
            screenShakeEnabled = !screenShakeEnabled;
        } else if (isMouseInside(settingsButtonBounds(4))) {
            menuPage = MenuPage.MAIN;
            if (settingsFromPause) {
                settingsFromPause = false;
                gameMode = GameMode.PAUSED;
            }
        }
    }

    private Sound optionalSound(String file) {
        return Gdx.files.internal(file).exists()
                ? Gdx.audio.newSound(Gdx.files.internal(file)) : null;
    }

    private void playSound(Sound sound) {
        if (sound != null && effectsVolume > 0f) sound.play(effectsVolume);
    }

    private void shake(float strength) {
        if (screenShakeEnabled) {
            shakeTimer = 0.15f;
            shakeStrength = strength;
        }
    }

    private void addFloating(String label, float x, float y) {
        floatingTexts.add(new FloatingText(label, x, y));
    }

    private void drawFloatingTexts() {
        for (FloatingText item : floatingTexts) {
            uiFont.setColor(1f, 0.88f, 0.52f, Math.min(1f, item.life * 2f));
            uiFont.draw(spriteBatch, item.label, item.x, item.y);
        }
        uiFont.setColor(1f, 1f, 1f, 1f);
    }

    private void showBanner(String label) {
        bannerText = label;
        bannerTimer = 1.8f;
    }

    private String stageName(int stage) {
        if (stage == STAGE_GODDESS_CHURCH) return "BOSS STAGE";
        return "STAGE " + stage;
    }



    private void drawCenteredMenuText(String text, Rectangle bounds) {

        uiFont.getData().setScale(1.1f);

        menuTextLayout.setText(uiFont, text);

        float textX = bounds.x + (bounds.width - menuTextLayout.width) / 2f;
        float textY = bounds.y + (bounds.height + menuTextLayout.height) / 2f;

        uiFont.setColor(0.08f, 0.03f, 0.02f, 0.85f);
        uiFont.draw(spriteBatch, text, textX + 1f, textY - 1f);

        uiFont.setColor(1f, 0.94f, 0.78f, 1f);
        uiFont.draw(spriteBatch, text, textX, textY);

        uiFont.setColor(1f, 1f, 1f, 1f);

        uiFont.getData().setScale(0.9f);
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



        if (merchantTexture != null) merchantTexture.dispose();

        if (merchantInteractIcon != null) merchantInteractIcon.dispose();

        if (shopPanelTexture != null) shopPanelTexture.dispose();

        if (closeIcon != null) closeIcon.dispose();

        if (goldIcon != null) goldIcon.dispose();

        if (attackUpgradeIcon != null) attackUpgradeIcon.dispose();

        if (healthUpgradeIcon != null) healthUpgradeIcon.dispose();

        if (healPotionIcon != null) healPotionIcon.dispose();

        if (attackPotionIcon != null) attackPotionIcon.dispose();

        if (mainMenuBackgroundTexture != null) mainMenuBackgroundTexture.dispose();

        if (titleLogoTexture != null) titleLogoTexture.dispose();

        if (menuButtonNormalTexture != null) menuButtonNormalTexture.dispose();

        if (menuButtonHoverTexture != null) menuButtonHoverTexture.dispose();

        if (menuButtonPressedTexture != null) menuButtonPressedTexture.dispose();

        uiFont.dispose();
        if (hitSound != null) hitSound.dispose();
        if (buySound != null) buySound.dispose();
        if (potionSound != null) potionSound.dispose();



        if (backgroundMusic != null) {



            backgroundMusic.stop();

            backgroundMusic.dispose();

        }

    }

}
