package com.example.pixelgame;



import java.util.ArrayList;

import java.util.HashSet;



import com.badlogic.gdx.ApplicationAdapter;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

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

    // Development overlay only; combat collision checks run independently.
    private static final boolean DEBUG_ATTACK_BOUNDS = false;
    private final CombatFeedback feedback = new CombatFeedback();
    private Sound combatHitSound, playerHurtSound;



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



    private static final int ATTACK_PRICE = 50;

    private static final int HEALTH_PRICE = 40;

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

    private ReadabilityBatch spriteBatch;
    private GameHud gameHud;
    private UiTheme uiTheme;
    private InventoryView inventoryView;
    private String inventoryMessage = "";
    private float inventoryMessageTimer;
    private boolean inventoryMessageSuccess;
    private int previewStage = 1;
    private JourneyView journeyView;
    private final TutorialGuide tutorial = new TutorialGuide();
    private RunStats runStats = new RunStats();
    private ClearRecords clearRecords;
    private StageCheckpoint checkpoint, retryCheckpoint;
    private RunStats retryStats;
    private int retryTutorialStep = TutorialGuide.LESSON_COUNT;
    private ShopView shopView;
    private ResultView resultView;
    private RewardView rewardView;
    private final ArrayList<StageReward> runRewards = new ArrayList<>();
    private boolean rewardOpen;
    private int rewardSelection, rewardDestination, resultPage;
    private String rewardStageMessage;
    private PauseView pauseView;
    private final GameControls gameControls = new GameControls();
    private int pendingPauseAction;
    private static final boolean DEBUG_STAGE_SKIP = Boolean.getBoolean("deadlight.debug");
    private RunResult lastResult;
    private int runDefeated, runGoldEarned;
    private final StageTransition stageTransition = new StageTransition();
    private int pendingStage;
    private String pendingStageMessage;
    private boolean shopMessageSuccess;
    private int shopLastPurchase = -1;



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
    private Preferences preferences;
    private float musicVolume = .25f, effectsVolume = .5f;
    private boolean showScreenControls = true;



    private Player player;

    private FallenGod fallenGod;



    private final ArrayList<Enemy> enemies =

            new ArrayList<>();



    private final HashSet<Enemy> enemiesHitThisAttack =

            new HashSet<>();



    private final ArrayList<Rectangle> platforms =

            new ArrayList<>();



    private boolean bossHitThisAttack = false;
    private final HashSet<Enemy> enemiesHitThisSkill = new HashSet<>();
    private boolean bossHitThisSkill;
    private final ShockSlashView shockSlashView = new ShockSlashView();



    @Override

    public void create() {
        preferences = Gdx.app.getPreferences("deadlight.settings");
        musicVolume = MathUtils.clamp(preferences.getFloat("musicVolume", .25f), 0, 1);
        effectsVolume = MathUtils.clamp(preferences.getFloat("effectsVolume", .5f), 0, 1);
        musicMuted = preferences.getBoolean("musicMuted", false);
        showScreenControls = preferences.getBoolean("showScreenControls", true);



        camera = new OrthographicCamera();



        viewport = new FitViewport(

                VIEW_WIDTH,

                VIEW_HEIGHT,

                camera

        );



        shapeRenderer = new ShapeRenderer();

        spriteBatch = new ReadabilityBatch();
        gameHud = new GameHud();
        uiTheme = new UiTheme();
        inventoryView = new InventoryView(uiTheme);
        journeyView = new JourneyView(uiTheme);
        clearRecords = new ClearRecords(Gdx.app.getPreferences("deadlight.records"));
        shopView = new ShopView();
        resultView = new ResultView();
        rewardView = new RewardView(uiTheme);
        pauseView = new PauseView();

        uiFont = new BitmapFont();

        uiFont.getData().setScale(0.9f);

        menuTextLayout = new GlyphLayout();



        loadTextures();

        loadShopTextures();

        loadMainMenuTextures();

        loadBackgroundMusic();
        combatHitSound = Gdx.audio.newSound(Gdx.files.internal("audio/combat_hit.wav"));
        playerHurtSound = Gdx.audio.newSound(Gdx.files.internal("audio/player_hurt.wav"));



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

        applyMusicVolume();

        backgroundMusic.play();

    }



    private void createGameObjects() {
        enemiesHitThisSkill.clear(); bossHitThisSkill = false;



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

        player.restoreRunStats(attackLevels, healthLevels, savedHealth, savedBuffTime, rewardHealthBonus());
        feedback.reset(player.getHealthRatio());

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
        float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        if (gameMode == GameMode.PLAYING)
            inventoryMessageTimer = Math.max(0, inventoryMessageTimer - deltaTime);
        if (gameMode == GameMode.PAUSED) {
            updatePauseInput();
        } else if (gameMode == GameMode.PLAYING) {
            if (!shopOpen && !inventoryOpen && !rewardOpen && !stageTransition.active()) {
                feedback.update(Math.min(Gdx.graphics.getDeltaTime(), .1f), player.getHealthRatio());
            }
            if (stageTransition.active()) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) pauseGame();
                else advanceStageTransition(Math.min(Gdx.graphics.getDeltaTime(), .1f));
            } else if (rewardOpen) updateRewardInput();
            else if (shopOpen) updateShopInput(deltaTime);
            else if (inventoryOpen) updateInventoryInput();
            else updateGame(deltaTime);
        } else updateMenuInput();

        if (gameMode == GameMode.PLAYING || gameMode == GameMode.PAUSED) {
            if (gameMode == GameMode.PLAYING) updateCamera();
            drawGame();
            if (gameMode == GameMode.PAUSED) drawPause();
        } else drawMenuScreen();
    }

    private void pauseGame() {
        player.clearActionInput();
        pendingPauseAction = 0;
        gameMode = GameMode.PAUSED;
    }

    private void applyPauseAction(int row) {
        if (pendingPauseAction != 0) {
            if (row == 0) pendingPauseAction = 0;
            else if (row == 1) {
                int action = pendingPauseAction;
                pendingPauseAction = 0;
                if (action == 1) restartGame(); else returnToMainMenu();
            }
        } else if (row == 0) gameMode = GameMode.PLAYING;
        else if (row == 1) pendingPauseAction = 1;
        else if (row == 2) pendingPauseAction = 2;
    }

    private void updatePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (pendingPauseAction != 0) pendingPauseAction = 0;
            else gameMode = GameMode.PLAYING;
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            applyPauseAction(pendingPauseAction == 0 ? 0 : 1);
            return;
        }
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        for (int row = 0; row < (pendingPauseAction == 0 ? 3 : 2); row++) {
            if (isMouseInside(PauseView.buttonBounds(screenLeft(), row))) {
                applyPauseAction(row); return;
            }
        }
    }

    private void drawPause() {
        int hovered = -1;
        for (int row = 0; row < (pendingPauseAction == 0 ? 3 : 2); row++) {
            if (isMouseInside(PauseView.buttonBounds(screenLeft(), row))) hovered = row;
        }
        spriteBatch.begin();
        pauseView.draw(spriteBatch, uiFont, screenLeft(), pendingPauseAction, hovered,
                Gdx.input.isButtonPressed(Input.Buttons.LEFT));
        spriteBatch.end();
    }

    private void updateGame(float deltaTime) {
        updateMusicInput();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseInside(GameHud.pauseBounds(screenLeft())))) {
            pauseGame(); return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I) ||
                (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseInside(GameHud.inventoryBounds(screenLeft())))) {
            player.clearActionInput(); inventoryOpen = true; return;
        }
        if (currentStage == STAGE_ONE && tutorial.visible() && !tutorial.finished()
                && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
                && isMouseInside(JourneyView.skipBounds(screenLeft()))) {
            tutorial.skip(); return;
        }
        merchantAnimationTime += deltaTime;
        if (isNearMerchant() && (Gdx.input.isKeyJustPressed(Input.Keys.E) ||
                (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseInside(merchantIconBounds())))) {
            player.clearActionInput(); shopOpen = true; shopMessage = ""; shopMessageTimer = 0; return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) useHealPotion();
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) useAttackPotion();
        // Stopwatch uses real active frame time, independent of the physics delta clamp.
        runStats.advance(Gdx.graphics.getDeltaTime());
        if (showScreenControls) gameControls.update(player);
        else player.setScreenInput(false, false, false, false, false);
        player.captureActionInput();
        if (feedback.stopped()) return;
        int healthBeforeCombat = player.getHealth();
        int interruptionsBeforeCombat = player.getInterruptedSkillCount();
        int attacksBefore = player.getAttackCount(), skillsBefore = player.getSkillCount(), dashesBefore = player.getDashCount();
        player.update(deltaTime, platforms, getCurrentWorldWidth());
        runStats.actions(player.getAttackCount() - attacksBefore, player.getSkillCount() - skillsBefore,
                player.getDashCount() - dashesBefore);
        for (Enemy enemy : enemies) enemy.update(deltaTime, player, getCurrentWorldWidth());
        if (fallenGod != null && fallenGod.isAlive()) fallenGod.update(deltaTime, player);
        int damageTaken = healthBeforeCombat - player.getHealth();
        if (damageTaken > 0) {
            runStats.damage(damageTaken);
            feedback.hit(player.getCenterX(), player.getY() + 35, damageTaken, true);
            feedback.health(player.getHealthRatio());
            playerHurtSound.play(effectsVolume * .6f);
        }
        if (player.getInterruptedSkillCount() > interruptionsBeforeCombat) feedback.skillInterrupted();
        checkPlayerAttack();
        checkSkillAttack();
        if (currentStage == STAGE_ONE) tutorial.update(player, deltaTime);
        for (Enemy enemy : enemies) {
            int reward = enemy.claimGoldReward();
            if (reward > 0) { gold += reward; runGoldEarned += reward; runDefeated++; }
        }
        if (!player.isAlive()) { finishRun(GameMode.GAME_OVER); return; }
        if (fallenGod != null && !fallenGod.isAlive()) { finishRun(GameMode.CLEARED); return; }
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



        boolean testKeyPressed = DEBUG_STAGE_SKIP &&
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



    private void changeStage(int nextStage, String startMessage) {
        if (stageTransition.active() || rewardOpen) return;
        for (StageReward reward : runRewards) if (reward.stage() == currentStage) {
            beginStageTransition(nextStage, startMessage); return;
        }
        rewardOpen = true; rewardSelection = 0;
        rewardDestination = nextStage; rewardStageMessage = startMessage;
        player.clearActionInput();
    }

    private int rewardHealthBonus() {
        int bonus = 0;
        for (StageReward reward : runRewards) if (reward.kind() == StageReward.Kind.FORTIFY) bonus += GameBalance.CLEAR_HEALTH;
        return bonus;
    }

    private void updateRewardInput() {
        updateMusicInput();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { pauseGame(); return; }
        for (int i = 0; i < 3; i++) if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) rewardSelection = i;
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) rewardSelection = (rewardSelection + 2) % 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) rewardSelection = (rewardSelection + 1) % 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { confirmReward(); return; }
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        for (int i = 0; i < 3; i++) if (isMouseInside(RewardView.cardBounds(screenLeft(), i))) { rewardSelection = i; return; }
        if (isMouseInside(RewardView.continueBounds(screenLeft()))) confirmReward();
    }

    private void confirmReward() {
        if (!rewardOpen || gameMode != GameMode.PLAYING || !player.isAlive()) return;
        StageReward.Kind kind = StageReward.Kind.values()[rewardSelection];
        String detail;
        if (kind == StageReward.Kind.REST) {
            if (player.getHealth() == player.getMaxHealth()) { healPotions++; detail = "+1 HEAL POTION"; }
            else {
                int before = player.getHealth(); player.heal(player.getMaxHealth());
                detail = "HP " + before + "/" + player.getMaxHealth() + " -> " + player.getHealth() + "/" + player.getMaxHealth();
            }
        } else if (kind == StageReward.Kind.GOLD) {
            gold += GameBalance.CLEAR_GOLD; runGoldEarned += GameBalance.CLEAR_GOLD;
            detail = "+" + GameBalance.CLEAR_GOLD + " GOLD";
        } else { player.fortifyFromReward(); detail = "+" + GameBalance.CLEAR_HEALTH + " MAX HP"; }
        runRewards.add(new StageReward(currentStage, kind, detail)); runStats.clearRegion();
        rewardOpen = false; player.clearActionInput();
        beginStageTransition(rewardDestination, rewardStageMessage);
        rewardDestination = 0; rewardStageMessage = null;
    }

    private void beginStageTransition(int nextStage, String startMessage) {
        pendingStage = nextStage;
        pendingStageMessage = startMessage;
        stageTransition.begin(nextStage, true);
    }

    private void advanceStageTransition(float delta) {
        if (stageTransition.update(delta) && pendingStage != 0) {
            enterStage(pendingStage, pendingStageMessage);
            pendingStage = 0;
            pendingStageMessage = null;
        }
    }

    private void enterStage(int nextStage, String startMessage) {
        savedHealth = player.getHealth(); savedBuffTime = player.getAttackPotionTimer();
        disposeGameObjects(); currentStage = nextStage;
        runStats.enterStage(nextStage);
        selectStageTextures(); createPlatforms(); createGameObjects();
        camera.position.set(VIEW_WIDTH / 2, WORLD_HEIGHT / 2, 0); camera.update();
        tutorial.bind(player, TutorialGuide.LESSON_COUNT);
        captureCheckpoint();
        System.out.println(startMessage);
    }



    private void updateMusicInput() {



        if (!Gdx.input.isKeyJustPressed(

                Input.Keys.M

        )) {

            return;

        }



        musicMuted = !musicMuted;



        if (!musicMuted && musicVolume == 0) musicVolume = .25f;
        applyMusicVolume();
        saveSettings();



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
        resetRunData(); disposeGameObjects();
        currentStage = stage;
        selectStageTextures(); createPlatforms(); createGameObjects();
        camera.position.set(VIEW_WIDTH / 2, WORLD_HEIGHT / 2, 0); camera.update();
        gameMode = GameMode.PLAYING;
        menuPage = MenuPage.MAIN;
        runStats.reset(stage);
        tutorial.bind(player, stage == STAGE_ONE ? 0 : TutorialGuide.LESSON_COUNT);
        captureCheckpoint();
        stageTransition.begin(stage, false);
    }



    private void resetRunData() {
        rewardOpen = false; rewardSelection = rewardDestination = resultPage = 0; rewardStageMessage = null;
        runRewards.clear();
        runDefeated = 0;
        runGoldEarned = 0;
        stageTransition.reset();
        pendingPauseAction = 0;
        pendingStage = 0;
        pendingStageMessage = null;
        feedback.reset(1f);

        gold = 0;

        attackLevels = 0;

        healthLevels = 0;

        healPotions = 0;

        attackPotions = 0;

        savedHealth = 5;

        savedBuffTime = 0f;

        shopOpen = false;

        inventoryOpen = false;
        inventoryMessage = "";
        inventoryMessageTimer = 0;

        shopMessage = "";

        shopMessageTimer = 0f;

        tutorial.skip();
        runStats.reset(1);
        checkpoint = retryCheckpoint = null;
        retryStats = null;
        retryTutorialStep = TutorialGuide.LESSON_COUNT;

    }



    private void finishRun(GameMode result) {
        if (gameMode != GameMode.PLAYING) return;
        boolean cleared = result == GameMode.CLEARED;
        if (cleared) runStats.clearRegion();
        StageCheckpoint savedCheckpoint = checkpoint;
        RunStats savedStats = runStats.copy();
        int lesson = tutorial.step();
        boolean best = cleared && clearRecords.saveClear(runStats.startStage, runStats.seconds, runStats.damageTaken);
        ClearRecords.Summary record = clearRecords.read(runStats.startStage);
        lastResult = new RunResult(cleared, currentStage, runDefeated + (cleared ? 1 : 0), runGoldEarned, gold,
                runStats.seconds, runStats.damageTaken, runStats.potionsUsed, runStats.retries,
                runStats.startStage, best, record.bestSeconds(), record.clears(),
                runStats.snapshot(currentStage), runRewards, attackLevels, healthLevels, rewardHealthBonus(),
                runStats.attacks, runStats.skills, runStats.dashes);
        resetRunData();
        if (!cleared) { retryCheckpoint = savedCheckpoint; retryStats = savedStats; retryTutorialStep = lesson; }
        gameMode = result;
    }

    private void captureCheckpoint() {
        checkpoint = new StageCheckpoint(currentStage, gold, attackLevels, healthLevels,
                healPotions, attackPotions, player.getHealth(), player.getAttackPotionTimer(), runDefeated, runGoldEarned, runRewards);
    }

    private void retryStage() {
        if (retryCheckpoint == null) { startStage(lastResult == null ? STAGE_ONE : lastResult.stage()); return; }
        StageCheckpoint entry = retryCheckpoint;
        RunStats accumulated = retryStats.copy();
        int lesson = retryTutorialStep;
        resetRunData();
        disposeGameObjects();
        runStats = accumulated; runStats.retry();
        currentStage = entry.stage(); gold = entry.gold();
        attackLevels = entry.attackLevels(); healthLevels = entry.healthLevels();
        healPotions = entry.heals(); attackPotions = entry.powers();
        savedHealth = entry.health(); savedBuffTime = entry.buffSeconds();
        runDefeated = entry.defeated(); runGoldEarned = entry.earned();
        runRewards.addAll(entry.rewards());
        selectStageTextures(); createPlatforms(); createGameObjects();
        checkpoint = entry;
        tutorial.bind(player, currentStage == STAGE_ONE ? lesson : TutorialGuide.LESSON_COUNT);
        camera.position.set(VIEW_WIDTH / 2, WORLD_HEIGHT / 2, 0); camera.update();
        gameMode = GameMode.PLAYING; menuPage = MenuPage.MAIN;
        stageTransition.begin(currentStage, false);
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



        if (!player.isAttackActive()) return;

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



                int dealt = healthBeforeHit - enemy.getHealth();
                Rectangle enemyBounds = enemy.getBounds();
                feedback.hit(enemyBounds.x + enemyBounds.width / 2, enemyBounds.y + 35, dealt, false, false,
                        enemyBounds.y + enemyBounds.height + 14);
                if (dealt > 0 && feedback.allowImpactSound()) combatHitSound.play(effectsVolume * .46f);
                enemiesHitThisAttack.add(enemy);

            }

        }



        if (fallenGod != null &&

                fallenGod.isAlive() &&

                !bossHitThisAttack &&

                attackBounds.overlaps(

                        fallenGod.getBounds()

                )) {



            int healthBeforeHit = fallenGod.getHealth();
            fallenGod.takeDamage(player.getAttackDamage());
            int dealt = healthBeforeHit - fallenGod.getHealth();
            Rectangle bossBounds = fallenGod.getBounds();
            feedback.hit(bossBounds.x + bossBounds.width / 2, bossBounds.y + 75, dealt, false, false,
                    Math.min(200, bossBounds.y + bossBounds.height + 14));
            if (dealt > 0 && feedback.allowImpactSound()) combatHitSound.play(effectsVolume * .46f);

            bossHitThisAttack = true;

        }

    }



    private void checkSkillAttack() {
        if (!player.isUsingSkill()) { enemiesHitThisSkill.clear(); bossHitThisSkill = false; return; }
        if (!player.isSkillActive()) return;
        Rectangle bounds = player.getSkillBounds();
        for (Enemy enemy : enemies) {
            if (enemy.getHealth() <= 0 || enemiesHitThisSkill.contains(enemy) || !bounds.overlaps(enemy.getBounds())) continue;
            int before = enemy.getHealth();
            enemy.takeDamage(player.getSkillDamage(), player.getCenterX(), true);
            int dealt = before - enemy.getHealth();
            Rectangle body = enemy.getBounds();
            feedback.hit(body.x + body.width / 2, body.y + 38, dealt, false, true, body.y + body.height + 14);
            if (dealt > 0 && feedback.allowImpactSound()) combatHitSound.play(effectsVolume * .65f, .75f, 0);
            enemiesHitThisSkill.add(enemy);
        }
        if (fallenGod != null && fallenGod.isAlive() && !bossHitThisSkill && bounds.overlaps(fallenGod.getBounds())) {
            int before = fallenGod.getHealth(); fallenGod.takeDamage(player.getSkillDamage());
            Rectangle body = fallenGod.getBounds();
            feedback.hit(body.x + body.width / 2, body.y + 65, before - fallenGod.getHealth(), false, true,
                    Math.min(200, body.y + body.height + 14));
            if (feedback.allowImpactSound()) combatHitSound.play(effectsVolume * .65f, .75f, 0);
            bossHitThisSkill = true;
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



        if (currentStage == STAGE_THREE) spriteBatch.atmosphere();
        drawBackground();
        spriteBatch.normal();

        drawGroundTiles();

        drawPlatformTiles();



        drawMerchant();
        if (currentStage != STAGE_GODDESS_CHURCH)
            journeyView.exit(spriteBatch, uiFont, getCurrentWorldWidth() - 100, GROUND_Y);



        if (currentStage == STAGE_THREE) spriteBatch.silhouette(false);
        for (Enemy enemy : enemies) {

            enemy.draw(spriteBatch);

        }



        if (fallenGod != null) {

            fallenGod.draw(spriteBatch);

        }



        if (currentStage == STAGE_THREE) spriteBatch.silhouette(true);
        player.draw(spriteBatch);
        spriteBatch.normal();





        spriteBatch.end();



        shapeRenderer.begin(

                ShapeRenderer.ShapeType.Filled

        );



        drawEnemyHealthBars();



        if (DEBUG_ATTACK_BOUNDS) drawAttackBounds();

        feedback.drawSparks(shapeRenderer);
        shapeRenderer.end();
        shockSlashView.draw(shapeRenderer, player);








        spriteBatch.begin();

        feedback.drawText(spriteBatch, uiFont, uiTheme);
        if (!shopOpen && !inventoryOpen && !rewardOpen && !stageTransition.active()) drawHud();

        if (shopOpen) drawShop();

        if (inventoryOpen) drawInventory();
        if (rewardOpen) drawReward();
        stageTransition.draw(spriteBatch, uiFont, screenLeft());

        spriteBatch.end();
        if (showScreenControls && gameMode == GameMode.PLAYING && !shopOpen && !inventoryOpen && !rewardOpen && !stageTransition.active()) {
            gameControls.draw(player, spriteBatch, shapeRenderer, uiFont);
            spriteBatch.setProjectionMatrix(camera.combined);
            shapeRenderer.setProjectionMatrix(camera.combined);
        }

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



    private float screenLeft() {

        return camera.position.x - VIEW_WIDTH / 2f;

    }



    private float merchantX() {

        return getCurrentWorldWidth() - 250f;

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











    private void showShopMessage(String message) {

        shopMessage = message;

        shopMessageTimer = 1.4f;

    }



    private Rectangle closeBounds() {

        return ShopView.closeBounds(screenLeft());

    }



    private Rectangle buyBounds(int row) {

        return ShopView.buyBounds(screenLeft(), row);

    }



    private void updateShopInput(float deltaTime) {

        if (shopMessageTimer > 0f) shopMessageTimer -= deltaTime;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            shopOpen = false;
            player.clearActionInput();

            return;

        }

        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;

        if (isMouseInside(closeBounds())) {

            shopOpen = false;
            player.clearActionInput();

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

            shopMessageSuccess = false;
            shopLastPurchase = -1;
            showShopMessage("Need " + (price - gold) + " more gold.");

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

        shopMessageSuccess = true;
        shopLastPurchase = row;
        showShopMessage(new String[]{"Attack upgraded.", "Max health upgraded.",
                "Heal potion added to inventory.", "Power potion added to inventory."}[row]);

    }



    private void useHealPotion() {
        if (healPotions <= 0) {
            showInventoryMessage("No heal potions. Buy one from the merchant.", false);
            return;
        }
        int before = player.getHealth();
        if (player.heal(GameBalance.healing(player.getMaxHealth()))) {
            healPotions--;
            runStats.potion();
            showInventoryMessage("Recovered " + (player.getHealth() - before) + " HP.", true);
        } else showInventoryMessage("HP is already full. Potion kept.", false);
    }

    private void showInventoryMessage(String message, boolean success) {
        inventoryMessage = message;
        inventoryMessageSuccess = success;
        inventoryMessageTimer = 3f;
    }



    private void useAttackPotion() {
        if (attackPotions <= 0) {
            showInventoryMessage("No power potions. Buy one from the merchant.", false);
        } else if (player.isAlive()) {
            attackPotions--;
            runStats.potion();
            player.drinkAttackPotion();
            showInventoryMessage("Power active: +" + GameBalance.POWER_BONUS + " damage for "
                    + (int)GameBalance.POWER_DURATION + " seconds.", true);
        }
    }



    private Rectangle inventoryUseBounds(int row) {
        return InventoryView.useBounds(screenLeft(), row);
    }



    private void updateInventoryInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.I) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            inventoryOpen = false;
            player.clearActionInput();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) useHealPotion();
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) useAttackPotion();
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        if (isMouseInside(InventoryView.closeBounds(screenLeft()))) {
            inventoryOpen = false;
            player.clearActionInput();
        } else if (isMouseInside(inventoryUseBounds(0))) useHealPotion();
        else if (isMouseInside(inventoryUseBounds(1))) useAttackPotion();
    }







    private void drawInventory() {
        int hovered = -1;
        for (int row = 0; row < 2; row++) if (isMouseInside(inventoryUseBounds(row))) hovered = row;
        inventoryView.draw(spriteBatch, uiFont, screenLeft(), player, healPotions, attackPotions,
                healPotionIcon, attackPotionIcon, hovered, Gdx.input.isButtonPressed(Input.Buttons.LEFT),
                isMouseInside(InventoryView.closeBounds(screenLeft())),
                inventoryMessageTimer > 0 ? inventoryMessage : "", inventoryMessageSuccess);
    }



    private void drawHud() {
        feedback.drawScreenPulse(spriteBatch, uiTheme, screenLeft());
        gameHud.draw(spriteBatch, uiFont, screenLeft(), player, feedback.trailRatio(),
                healPotions, attackPotions, gold, healPotionIcon, attackPotionIcon, goldIcon);
        gameHud.drawBoss(spriteBatch, uiFont, screenLeft(), fallenGod);
        gameHud.drawActions(spriteBatch, uiFont, screenLeft(), player, showScreenControls,
                feedback.showsSkillInterrupted());
        int defeated = 0;
        for (Enemy enemy : enemies) if (enemy.getHealth() <= 0) defeated++;
        journeyView.progress(spriteBatch, uiFont, screenLeft(), currentStage, defeated, enemies.size(),
                player.getX(), getCurrentWorldWidth(), fallenGod, runStats);
        if (currentStage == STAGE_ONE) journeyView.tutorial(spriteBatch, uiFont, screenLeft(), tutorial,
                isMouseInside(JourneyView.skipBounds(screenLeft())), Gdx.input.isButtonPressed(Input.Buttons.LEFT));
    }

    private void drawReward() {
        int hovered = -1;
        for (int i = 0; i < 3; i++) if (isMouseInside(RewardView.cardBounds(screenLeft(), i))) hovered = i;
        rewardView.draw(spriteBatch, uiFont, screenLeft(), currentStage, player, gold, rewardSelection, hovered,
                isMouseInside(RewardView.continueBounds(screenLeft())), Gdx.input.isButtonPressed(Input.Buttons.LEFT),
                new Texture[]{healPotionIcon, goldIcon, healthUpgradeIcon});
    }

    private void drawShop() {
        int hovered = -1;
        for (int row = 0; row < 4; row++) {
            if (isMouseInside(buyBounds(row))) hovered = row;
        }
        shopView.draw(spriteBatch, uiFont, screenLeft(), shopPanelTexture, goldIcon,
                new Texture[]{attackUpgradeIcon, healthUpgradeIcon, healPotionIcon, attackPotionIcon},
                gold, new int[]{ATTACK_PRICE, HEALTH_PRICE, HEAL_POTION_PRICE, ATTACK_POTION_PRICE},
                new int[]{attackLevels, healthLevels, healPotions, attackPotions}, hovered,
                Gdx.input.isButtonPressed(Input.Buttons.LEFT), isMouseInside(closeBounds()),
                shopMessage, shopMessageSuccess, shopLastPurchase, shopMessageTimer,
                player.getAttackDamage(), player.getMaxHealth(), player.getHealth(), player.getAttackPotionTimer() > 0 ? 1 : 0);
    }

    private void updateMenuInput() {
        if (gameMode != GameMode.MENU) { updateResultScreenInput(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            menuPage = MenuPage.MAIN;
            return;
        }
        if (menuPage == MenuPage.MAIN && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            restartGame(); return;
        }
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        if (menuPage == MenuPage.MAIN) {
            if (isMouseInside(mainMenuButtonBounds(0))) restartGame();
            else if (isMouseInside(mainMenuButtonBounds(1))) menuPage = MenuPage.STAGE_SELECT;
            else if (isMouseInside(mainMenuButtonBounds(2))) menuPage = MenuPage.SETTINGS;
            else if (isMouseInside(mainMenuButtonBounds(3))) Gdx.app.exit();
        } else if (menuPage == MenuPage.STAGE_SELECT) {
            for (int row = 0; row < 4; row++) {
                if (isMouseInside(subMenuButtonBounds(row))) { startStage(row + 1); return; }
            }
            if (isMouseInside(subMenuButtonBounds(4))) menuPage = MenuPage.MAIN;
        } else {
            for (int row = 0; row < 2; row++) {
                for (int direction : new int[]{-1, 1}) {
                    if (isMouseInside(settingsVolumeBounds(row, direction))) {
                        changeVolume(row, direction); return;
                    }
                }
            }
            if (isMouseInside(settingsButtonBounds(2))) {
                showScreenControls = !showScreenControls; saveSettings();
            } else if (isMouseInside(settingsButtonBounds(3))) {
                if (Gdx.graphics.isFullscreen()) Gdx.graphics.setWindowedMode(1280, 720);
                else Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else if (isMouseInside(settingsButtonBounds(4))) menuPage = MenuPage.MAIN;
        }
    }

    private Rectangle settingsVolumeBounds(int row, int direction) {
        Rectangle r = settingsButtonBounds(row);
        return new Rectangle(r.x + (direction < 0 ? 0 : r.width - 32), r.y, 32, r.height);
    }

    private void changeVolume(int row, int direction) {
        if (row == 0) {
            musicVolume = MathUtils.clamp((musicMuted ? 0 : musicVolume) + direction * .25f, 0, 1);
            musicMuted = musicVolume == 0;
            applyMusicVolume();
        } else effectsVolume = MathUtils.clamp(effectsVolume + direction * .25f, 0, 1);
        saveSettings();
    }



    private void updateResultScreenInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) { resultPage = 1 - resultPage; return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { applyResultAction(0); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) { restartGame(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { returnToMainMenu(); return; }
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        for (int page = 0; page < 2; page++) if (isMouseInside(ResultView.tabBounds(screenLeft(), page))) { resultPage = page; return; }
        for (int row = 0; row < 3; row++) {
            if (isMouseInside(resultButtonBounds(row))) { applyResultAction(row); return; }
        }
    }

    private void applyResultAction(int row) {
        if (row == 0) {
            if (lastResult != null && !lastResult.cleared()) retryStage(); else restartGame();
        } else if (row == 1) {
            if (lastResult != null && lastResult.cleared()) {
                returnToMainMenu(); menuPage = MenuPage.STAGE_SELECT;
            } else restartGame();
        } else returnToMainMenu();
    }



    private Rectangle mainMenuButtonBounds(int row) {
        return new Rectangle(screenLeft() + 42f, 186f - row * 57f, 240f, 44f);
    }



    private Rectangle subMenuButtonBounds(int row) {
        return new Rectangle(screenLeft() + 55f, 222f - row * 48f, 210f, 40f);
    }



    private Rectangle settingsButtonBounds(int row) {
        return new Rectangle(screenLeft() + 55f, 248f - row * 48f, 260f, 42f);
    }



    private Rectangle resultButtonBounds(int row) {
        return ResultView.buttonBounds(screenLeft(), row);
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
        journeyView.records(spriteBatch, uiFont, screenLeft(), clearRecords.read(1));
    }



    private void drawStageSelectMenu() {
        float left = screenLeft();
        uiTheme.rect(spriteBatch, left, 0, 640, 360, com.badlogic.gdx.graphics.Color.BLACK, .42f);
        uiTheme.text(spriteBatch, uiFont, "STAGE SELECT", left + 55, 326, 1.15f, UiTheme.TEXT, 260);
        uiTheme.text(spriteBatch, uiFont, "Choose a region to practice.", left + 55, 305, .65f, UiTheme.MUTED, 260);
        String[] names = {"VERDANT RUINS", "CASTLE RAMPARTS", "ASHEN BATTLEFIELD", "GODDESS CHURCH"};
        for (int row = 0; row < 4; row++) {
            if (isMouseInside(subMenuButtonBounds(row))) previewStage = row + 1;
            drawMenuButton(subMenuButtonBounds(row), "0" + (row + 1) + "  " + names[row]);
        }
        drawMenuButton(subMenuButtonBounds(4), "BACK");
        int index = previewStage - 1;
        uiTheme.panel(spriteBatch, left + 300, 50, 310, 238);
        uiTheme.text(spriteBatch, uiFont, "REGION 0" + previewStage, left + 314, 273, .6f, UiTheme.GOLD, 280);
        String[] difficulty = {"INTRO", "NORMAL", "HARD", "BOSS"};
        uiTheme.text(spriteBatch, uiFont, "DIFFICULTY: " + difficulty[index], left + 450, 273, .6f, UiTheme.MUTED, 146);
        Texture[] previews = {stage1BackgroundTexture, stage2BackgroundTexture, stage3BackgroundTexture,
                goddessChurchBackgroundTexture};
        float packed = spriteBatch.getPackedColor();
        spriteBatch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        spriteBatch.draw(previews[index], left + 314, 130, 282, 126);
        spriteBatch.setPackedColor(packed);
        uiTheme.text(spriteBatch, uiFont, names[index], left + 314, 116, 1f, UiTheme.TEXT, 282);
        String[] focus = {"Slimes / Learn the basics", "Knights / Close combat",
                "Dark slimes / Timing & spacing", "Boss / Watch attack height"};
        uiTheme.text(spriteBatch, uiFont, focus[index], left + 314, 95, .65f, UiTheme.MUTED, 282);
        uiTheme.text(spriteBatch, uiFont, "FRESH RUN / Starts with base stats", left + 314, 70, .6f, UiTheme.GOLD, 282);
        ClearRecords.Summary record = clearRecords.read(previewStage);
        uiTheme.text(spriteBatch, uiFont, record.exists() ? "BEST " + JourneyView.time(record.bestSeconds())
                + " / CLEARS " + record.clears() : "No clear record for this starting region.",
                left + 314, 57, .52f, UiTheme.MUTED, 282);
    }



    private void drawSettingsMenu() {
        float left = screenLeft();
        uiTheme.rect(spriteBatch, left, 0, 640, 360, com.badlogic.gdx.graphics.Color.BLACK, .35f);
        uiTheme.text(spriteBatch, uiFont, "SETTINGS", left + 55, 326, 1.15f, UiTheme.TEXT, 260);
        uiTheme.text(spriteBatch, uiFont, "Volume: use - / + in 25% steps.", left + 55, 305, .65f, UiTheme.MUTED, 260);
        for (int row = 0; row < 2; row++) {
            Rectangle area = settingsButtonBounds(row);
            float volume = row == 0 ? (musicMuted ? 0 : musicVolume) : effectsVolume;
            uiTheme.panel(spriteBatch, area.x, area.y, area.width, area.height);
            uiTheme.centered(spriteBatch, uiFont, (row == 0 ? "MUSIC " : "EFFECTS ")
                    + Math.round(volume * 100) + "%", new Rectangle(area.x + 36, area.y + 12, area.width - 72, 26), .8f, UiTheme.TEXT);
            uiTheme.rect(spriteBatch, area.x + 48, area.y + 8, area.width - 96, 3, UiTheme.BORDER, 1);
            uiTheme.rect(spriteBatch, area.x + 48, area.y + 8, (area.width - 96) * volume, 3, UiTheme.GOLD, 1);
            for (int direction : new int[]{-1, 1}) {
                Rectangle button = settingsVolumeBounds(row, direction);
                uiTheme.button(spriteBatch, uiFont, button, direction < 0 ? "-" : "+",
                        direction < 0 ? volume > 0 : volume < 1, isMouseInside(button),
                        Gdx.input.isButtonPressed(Input.Buttons.LEFT), 1.1f);
            }
        }
        drawMenuButton(settingsButtonBounds(2), "SCREEN BUTTONS " + (showScreenControls ? "ON" : "OFF"));
        drawMenuButton(settingsButtonBounds(3), Gdx.graphics.isFullscreen() ? "FULLSCREEN" : "WINDOWED");
        drawMenuButton(settingsButtonBounds(4), "BACK");
        uiTheme.text(spriteBatch, uiFont, "Audio & button preferences save automatically.", left + 55, 32, .58f, UiTheme.MUTED, 300);
    }

    private void applyMusicVolume() {
        if (backgroundMusic != null) backgroundMusic.setVolume(musicMuted ? 0 : musicVolume);
    }

    private void saveSettings() {
        preferences.putFloat("musicVolume", musicVolume).putFloat("effectsVolume", effectsVolume)
                .putBoolean("musicMuted", musicMuted).putBoolean("showScreenControls", showScreenControls).flush();
    }

    @Override public void pause() {
        if (gameMode == GameMode.PLAYING) pauseGame();
    }

    private void drawResultMenu() {
        int hovered = -1;
        for (int row = 0; row < 3; row++) {
            if (isMouseInside(resultButtonBounds(row))) hovered = row;
        }
        resultView.draw(spriteBatch, uiFont, screenLeft(), lastResult, hovered,
                Gdx.input.isButtonPressed(Input.Buttons.LEFT), resultPage);
    }

    private void drawMenuButton(Rectangle bounds, String label) {
        boolean hovered = isMouseInside(bounds);
        uiTheme.button(spriteBatch, uiFont, bounds, label, true, hovered,
                hovered && Gdx.input.isButtonPressed(Input.Buttons.LEFT), 1f);
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
        gameHud.dispose();
        uiTheme.dispose();
        shopView.dispose();
        resultView.dispose();
        pauseView.dispose();
        stageTransition.dispose();
        if (combatHitSound != null) combatHitSound.dispose();
        if (playerHurtSound != null) playerHurtSound.dispose();



        if (backgroundMusic != null) {



            backgroundMusic.stop();

            backgroundMusic.dispose();

        }

    }

}
