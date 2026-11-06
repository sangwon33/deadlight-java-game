package com.example.pixelgame;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.IdentityHashMap;

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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

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
    private final RegionGuide regionGuide = new RegionGuide();
    private final WorldGimmicks gimmicks=new WorldGimmicks();
    private final ArrayList<Rectangle> solidFloors=new ArrayList<>();
    private boolean viewingMap;
    private final EnemyAttackView enemyAttackView = new EnemyAttackView();
    private final ArrayList<Enemy> guardians = new ArrayList<>();
    private int lastAttackSequence = -1, lastSkillSequence = -1;
    private Sound combatHitSound, playerHurtSound;

    private static final int STAGE_TUTORIAL = 0;
    private static final float TUTORIAL_WORLD_WIDTH = StageLayout.width(0);
    private KeyBindings.Action waitingForKey;
    private String keyMessage = "항목을 클릭한 뒤 사용할 키를 누르세요.";
    private boolean settingsFromPause;
    private FreeTypeFontGenerator fontGenerator;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private Texture tutorialBackground;
    private Enemy trainingTarget;
    private static final int STAGE_ONE = 1;

    private static final int STAGE_TWO = 2;

    private static final int STAGE_THREE = 3;

    private static final int STAGE_GODDESS_CHURCH = 4;

    private static final float VIEW_WIDTH = 640f;

    private static final float VIEW_HEIGHT = 360f;

    private static final float STAGE_ONE_WORLD_WIDTH = StageLayout.width(1);

    private static final float STAGE_TWO_WORLD_WIDTH = StageLayout.width(2);

    private static final float STAGE_THREE_WORLD_WIDTH = StageLayout.width(3);

    private static final float GODDESS_CHURCH_WORLD_WIDTH = StageLayout.width(4);

    private static final float WORLD_HEIGHT = 360f;

    private static final float GROUND_Y = 60f;

    private static final float GROUND_TILE_SIZE = 64f;

    private static final float PLATFORM_TILE_WIDTH = 96f;

    private static final float PLATFORM_TILE_HEIGHT = 18f;

    private enum GameMode { PLAYING, PAUSED, MENU, GAME_OVER, CLEARED }

    private enum MenuPage { MAIN, STAGE_SELECT, SETTINGS, CONTROLS }

    private GameMode gameMode = GameMode.MENU;

    private MenuPage menuPage = MenuPage.MAIN;
    private final LobbyView lobbyView=new LobbyView();
    private int lobbySelection, lobbyMouseX=-1, lobbyMouseY=-1;

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

    private int savedHealth = GameBalance.BASE_HEALTH;

    private float savedBuffTime = 0f;

    private boolean shopOpen = false;

    private boolean inventoryOpen = false;

    private String shopMessage = "";

    private float shopMessageTimer = 0f;

    private float merchantAnimationTime = 0f;

    private MoonAtlas moonRelics;
    private float moonTime;
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
    private final IdentityHashMap<Texture, TerrainTile> terrainTiles = new IdentityHashMap<>();

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
    private LamplighterEncounter lamplighter;
    private boolean lamplighterHitThisAttack,lamplighterHitThisSkill;
    private SideBossEncounter sideBoss;
    private boolean sideBossHitThisAttack,sideBossHitThisSkill;

    @Override

    public void create() {
        preferences = Gdx.app.getPreferences("deadlight.settings");
        musicVolume = MathUtils.clamp(preferences.getFloat("musicVolume", .25f), 0, 1);
        effectsVolume = MathUtils.clamp(preferences.getFloat("effectsVolume", .5f), 0, 1);
        musicMuted = preferences.getBoolean("musicMuted", false);
        showScreenControls = preferences.getBoolean("showScreenControls", true);

        KeyBindings.load();
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

        uiCamera = new OrthographicCamera();
        uiViewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, uiCamera);
        uiCamera.position.set(VIEW_WIDTH / 2f, VIEW_HEIGHT / 2f, 0f); uiCamera.update();
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/NanumGothic-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = UiTypography.RASTER_PIXELS; parameter.incremental = true;
        parameter.shadowOffsetX = 0; parameter.shadowOffsetY = 2;
        parameter.shadowColor.set(0f, 0f, 0f, .65f);
        parameter.borderWidth = 2f;
        parameter.borderColor.set(.015f, .025f, .04f, .85f);
        parameter.minFilter = parameter.magFilter = TextureFilter.Linear;
        uiFont = fontGenerator.generateFont(parameter);
        // Rounding each scaled glyph separately shifts characters off their shared baseline.
        uiFont.setUseIntegerPositions(false);

        UiTypography.size(uiFont, .9f);

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
        stage1BackgroundTexture = new Texture("art/grotto.png");
        stage2BackgroundTexture = new Texture("art/rampart.png");
        stage3BackgroundTexture = new Texture("art/orchard.png");
        goddessChurchBackgroundTexture = new Texture("art/cathedral.png");
        for(Texture bg:new Texture[]{stage1BackgroundTexture,stage2BackgroundTexture,stage3BackgroundTexture,goddessChurchBackgroundTexture})
            bg.setFilter(TextureFilter.Linear,TextureFilter.Linear);
        MoonAtlas terrain=MoonAtlas.acquire("art/terrain.png",3,2);
        try {
            stage1GroundTileTexture=terrain.croppedTexture(0);
            stage2GroundTileTexture=terrain.croppedTexture(1);
            stage3GroundTileTexture=terrain.croppedTexture(2);
            goddessChurchGroundTileTexture=terrain.croppedTexture(3);
            platformTileTexture=terrain.croppedTexture(4);
            Texture[] ground={stage1GroundTileTexture,stage2GroundTileTexture,stage3GroundTileTexture,goddessChurchGroundTileTexture,platformTileTexture};
            for(int i=0;i<ground.length;i++)terrainTiles.put(ground[i],new TerrainTile(ground[i],terrain.surfaceRow(i)));
        }finally{terrain.close();}
        tutorialBackground = new Texture("art/grotto.png");
        tutorialBackground.setFilter(TextureFilter.Linear,TextureFilter.Linear);
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
        moonRelics=MoonAtlas.acquire("art/relics.png",4,3);
        merchantInteractIcon=moonRelics.croppedTexture(7);
        goldIcon=moonRelics.croppedTexture(6);
        attackUpgradeIcon=moonRelics.croppedTexture(8);
        healthUpgradeIcon=moonRelics.croppedTexture(9);
        closeIcon=moonRelics.croppedTexture(10);
        healPotionIcon=moonRelics.croppedTexture(4);
        attackPotionIcon=moonRelics.croppedTexture(5);
    }

    private void loadMainMenuTextures() {
        mainMenuBackgroundTexture = new Texture("art/title.png");
        mainMenuBackgroundTexture.setFilter(TextureFilter.Linear,TextureFilter.Linear);
    }

    private void selectStageTextures() {

        if (currentStage == STAGE_TUTORIAL) {
            backgroundTexture = tutorialBackground != null ? tutorialBackground : stage3BackgroundTexture;
            groundTileTexture = stage3GroundTileTexture;
        } else if (currentStage == STAGE_GODDESS_CHURCH) {

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
        viewingMap=false;
        enemiesHitThisSkill.clear(); enemiesHitThisAttack.clear();
        bossHitThisAttack = bossHitThisSkill = false;
        lamplighterHitThisAttack=lamplighterHitThisSkill=false;
        sideBossHitThisAttack=sideBossHitThisSkill=false;
        lastAttackSequence = lastSkillSequence = -1;
        enemies.clear(); guardians.clear(); regionGuide.enter(currentStage);
        player = new Player(currentStage == STAGE_GODDESS_CHURCH ? 150 : 100, GROUND_Y);
        player.configureWorld(solidFloors,StageLayout.bottom(currentStage),StageLayout.top(currentStage));
        fallenGod = currentStage == STAGE_GODDESS_CHURCH
                ? new FallenGod(780,GROUND_Y,GODDESS_CHURCH_WORLD_WIDTH) : null;
        trainingTarget = null;
        if (currentStage == STAGE_TUTORIAL) {
            trainingTarget = new Enemy(450,GROUND_Y); trainingTarget.setTrainingDummy(); enemies.add(trainingTarget);
        } else {
            for (StageLayout.Spawn spawn : StageLayout.enemies(currentStage)) {
                Enemy enemy = spawn.create(); enemy.setWorldSolids(solidFloors); enemies.add(enemy);
                if(enemy.isFlying()||enemy.isRanged()){
                    ArrayList<Rectangle> flightObstacles=new ArrayList<>(solidFloors);
                    flightObstacles.addAll(platforms);enemy.setWorldSolids(flightObstacles);
                }
                if (enemy.isGuardian()) guardians.add(enemy);
            }
        }

        // 새 스테이지에서도 이전 강화/체력/버프를 이어받는다.

        player.restoreRunStats(attackLevels, healthLevels, savedHealth, savedBuffTime, rewardHealthBonus());
        feedback.reset(player.getHealthRatio());

    }

    private void createPlatforms() {
        platforms.clear();
        platforms.addAll(StageLayout.platforms(currentStage));
        solidFloors.clear();solidFloors.addAll(StageLayout.floors(currentStage));
        gimmicks.enter(currentStage,platforms,solidFloors);
        if(lamplighter!=null)lamplighter.close();
        lamplighter=currentStage==STAGE_TWO?new LamplighterEncounter(solidFloors):null;
        if(sideBoss!=null)sideBoss.close();
        SideBossArena arena=SideBossArena.forStage(currentStage);
        sideBoss=arena!=null?new SideBossEncounter(arena,solidFloors):null;
    }

    private int remainingGuardians() {
        int remaining = 0;
        for (Enemy enemy : guardians) if (enemy.getHealth() > 0) remaining++;
        return remaining;
    }

    private void updateRegionGuide(float delta) {
        regionGuide.update(delta, player);
        StageLayout.Supply supply;
        while ((supply = regionGuide.takeCollected()) != null) {
            gold += supply.gold(); runGoldEarned += supply.gold();
            healPotions += supply.heals(); attackPotions += supply.powers();
            String item = supply.heals() > 0 ? "회복 물약 +"+supply.heals()
                    : supply.powers() > 0 ? "공격 물약 +"+supply.powers() : "숨겨진 보급품";
            regionGuide.notice(item, "골드 +"+supply.gold()+" / 높은 길 탐색 보상");
        }
    }

    @Override
    public void render() {
        float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        if(gameMode != GameMode.PAUSED) moonTime += deltaTime;
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
        else if (row == 2) { settingsFromPause = true; menuPage = MenuPage.SETTINGS; gameMode = GameMode.MENU; }
        else if (row == 3) pendingPauseAction = 2;
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
        for (int row = 0; row < (pendingPauseAction == 0 ? 4 : 2); row++) {
            if (isMouseInside(PauseView.buttonBounds(screenLeft(), row))) {
                applyPauseAction(row); return;
            }
        }
    }

    private void drawPause() {
        int hovered = -1;
        for (int row = 0; row < (pendingPauseAction == 0 ? 4 : 2); row++) {
            if (isMouseInside(PauseView.buttonBounds(screenLeft(), row))) hovered = row;
        }
        uiViewport.apply();
        spriteBatch.setProjectionMatrix(uiCamera.combined);
        spriteBatch.begin();
        pauseView.draw(spriteBatch, uiFont, screenLeft(), pendingPauseAction, hovered,
                Gdx.input.isButtonPressed(Input.Buttons.LEFT));
        spriteBatch.end();
    }

    private void updateGame(float deltaTime) {
        if (currentStage == STAGE_TUTORIAL) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)
                    || (tutorial.finished() && Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
                beginStageTransition(STAGE_ONE, "모험을 시작합니다."); return;
            }
        }
        viewingMap=currentStage>=1&&currentStage<=3&&Gdx.input.isKeyPressed(Input.Keys.TAB);
        if(viewingMap){player.clearActionInput();return;}
        updateMusicInput();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseInside(GameHud.pauseBounds(screenLeft())))) {
            pauseGame(); return;
        }
        if (KeyBindings.justPressed(KeyBindings.Action.INVENTORY) ||
                (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseInside(GameHud.inventoryBounds(screenLeft())))) {
            player.clearActionInput(); inventoryOpen = true; return;
        }
        if (currentStage == STAGE_TUTORIAL && tutorial.visible() && !tutorial.finished()
                && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
                && isMouseInside(JourneyView.skipBounds(screenLeft()))) {
            tutorial.skip(); beginStageTransition(STAGE_ONE, "모험을 시작합니다."); return;
        }
        merchantAnimationTime += deltaTime;
        if (isNearMerchant() && (KeyBindings.justPressed(KeyBindings.Action.INTERACT) ||
                (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && isMouseInsideWorld(merchantIconBounds())))) {
            player.clearActionInput(); shopOpen = true; shopMessage = ""; shopMessageTimer = 0; return;
        }
        if (KeyBindings.justPressed(KeyBindings.Action.HEAL)) useHealPotion();
        if (KeyBindings.justPressed(KeyBindings.Action.POWER)) useAttackPotion();
        // Stopwatch uses real active frame time, independent of the physics delta clamp.
        if (currentStage != STAGE_TUTORIAL) runStats.advance(Gdx.graphics.getDeltaTime());
        if (showScreenControls) gameControls.update(player);
        else player.setScreenInput(false, false, false, false, false);
        player.captureActionInput();
        if (feedback.stopped()) return;
        int healthBeforeCombat = player.getHealth();
        int interruptionsBeforeCombat = player.getInterruptedSkillCount();
        int attacksBefore = player.getAttackCount(), skillsBefore = player.getSkillCount(), dashesBefore = player.getDashCount();
        gimmicks.beforePhysics(deltaTime,player);
        player.update(deltaTime, platforms, getCurrentWorldWidth());
        gimmicks.afterPhysics(player);
        if(lamplighter!=null&&KeyBindings.justPressed(KeyBindings.Action.INTERACT)&&lamplighter.tryStart(player))
            regionGuide.notice("등불 감시탑의 시련","창과 등불의 궤적을 피해 움직이세요","체력이 절반 아래로 내려가면 공격이 빨라집니다");
        if(sideBoss!=null&&KeyBindings.justPressed(KeyBindings.Action.INTERACT)&&sideBoss.tryStart(player))
            regionGuide.notice(sideBoss.arena.room()+"의 시련",sideBoss.arena.roots()?"뿌리와 돌진을 뛰어넘어 피하세요":"도약과 낙하 바늘의 경로를 피해 움직이세요","공격 뒤 빈틈에 반격하세요");
        checkSkillAttack();
        if (currentStage != STAGE_TUTORIAL) runStats.actions(player.getAttackCount() - attacksBefore, player.getSkillCount() - skillsBefore,
                player.getDashCount() - dashesBefore);
        Rectangle combatView=camera==null?null:new Rectangle(camera.position.x-VIEW_WIDTH/2,camera.position.y-VIEW_HEIGHT/2,VIEW_WIDTH,VIEW_HEIGHT);
        for (Enemy enemy : enemies) {
            enemy.setRangedVisible(combatView==null||combatView.overlaps(enemy.getBounds()));
            enemy.update(deltaTime, player, getCurrentWorldWidth());
            if(enemy.takeShotFired()&&combatHitSound!=null)combatHitSound.play(effectsVolume*.22f,1.35f,0);
        }
        if (fallenGod != null) fallenGod.update(deltaTime, player);
        if(lamplighter!=null)lamplighter.update(deltaTime,player);
        if(sideBoss!=null)sideBoss.update(deltaTime,player);
        int damageTaken = healthBeforeCombat - player.getHealth();
        if (damageTaken > 0) {
            runStats.damage(damageTaken);
            feedback.hit(player.getCenterX(), player.getY() + 35, damageTaken, true);
            feedback.health(player.getHealthRatio());
            playerHurtSound.play(effectsVolume * .6f);
        }
        if (player.getInterruptedSkillCount() > interruptionsBeforeCombat) feedback.skillInterrupted();
        checkPlayerAttack();
        if (currentStage != STAGE_TUTORIAL && player.isAlive()) updateRegionGuide(deltaTime);
        if(gimmicks.takeOpenedNotice())regionGuide.notice("석문 개방","지상 지름길이 열렸습니다","지도를 보려면 Tab을 누르세요");
        claimLamplighterReward();
        claimSideBossReward();
        if (currentStage == STAGE_TUTORIAL) {
            if (trainingTarget != null && trainingTarget.getHealth() <= 0) tutorial.targetDefeated();
            tutorial.update(player, deltaTime);
        }
        for (Enemy enemy : enemies) {
            int reward = enemy.claimGoldReward();
            if (reward > 0) { gold += reward; runGoldEarned += reward; runDefeated++; }
        }
        if (!player.isAlive()) { finishRun(GameMode.GAME_OVER); return; }
        if (fallenGod != null && fallenGod.isDefeatedComplete()) { finishRun(GameMode.CLEARED); return; }
        checkStageChange();
    }

    private void claimLamplighterReward(){
        if(lamplighter==null||!player.isAlive()||!lamplighter.boss.claimReward())return;
        gold+=Lamplighter.GOLD;runGoldEarned+=Lamplighter.GOLD;runDefeated++;
        player.upgradeAttack();attackLevels++;healPotions++;
        regionGuide.notice("등불지기 격파","골드 +80 · 공격력 +1 · 회복 포션 +1","감시탑의 봉인이 풀렸습니다");
    }

    private void claimSideBossReward(){
        if(sideBoss==null||!player.isAlive()||!sideBoss.boss.claimReward())return;
        gold+=sideBoss.arena.gold();runGoldEarned+=sideBoss.arena.gold();runDefeated++;
        player.upgradeAttack();attackLevels++;
        if(sideBoss.arena.roots())healPotions++;else attackPotions++;
        regionGuide.notice(sideBoss.arena.name()+" 격파","골드 +"+sideBoss.arena.gold()+" · 공격력 +1 · "+(sideBoss.arena.roots()?"회복 포션":"공격 포션")+" +1","시련의 봉인이 풀렸습니다");
    }

    private void checkStageChange() {
        if (currentStage == STAGE_TUTORIAL || currentStage == STAGE_GODDESS_CHURCH) return;

        float currentStageWidth =

                getCurrentWorldWidth();

        boolean reachedStageEnd =

                player.getX()

                        >= currentStageWidth - 100f;

        boolean testKeyPressed = DEBUG_STAGE_SKIP &&
                Gdx.input.isKeyJustPressed(

                        Input.Keys.N

                );

        if (!testKeyPressed && (remainingGuardians() > 0 || player.getY() > GROUND_Y + 12)) return;
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || KeyBindings.justPressed(KeyBindings.Action.LEFT)) rewardSelection = (rewardSelection + 2) % 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || KeyBindings.justPressed(KeyBindings.Action.RIGHT)) rewardSelection = (rewardSelection + 1) % 3;
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
            if (player.getHealth() == player.getMaxHealth()) { healPotions++; detail = "+1 회복 포션"; }
            else {
                int before = player.getHealth(); player.heal(player.getMaxHealth());
                detail = "체력 " + before + "/" + player.getMaxHealth() + " -> " + player.getHealth() + "/" + player.getMaxHealth();
            }
        } else if (kind == StageReward.Kind.GOLD) {
            gold += GameBalance.CLEAR_GOLD; runGoldEarned += GameBalance.CLEAR_GOLD;
            detail = "+" + GameBalance.CLEAR_GOLD + " 골드";
        } else { player.fortifyFromReward(); detail = "+" + GameBalance.CLEAR_HEALTH + " 최대 체력"; }
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
        if (currentStage == STAGE_TUTORIAL) { startStage(nextStage); return; }
        savedHealth = player.getHealth(); savedBuffTime = player.getAttackPotionTimer();
        disposeGameObjects(); currentStage = nextStage;
        runStats.enterStage(nextStage);
        selectStageTextures(); createPlatforms(); createGameObjects();
        camera.position.set(VIEW_WIDTH / 2, WORLD_HEIGHT / 2, 0); camera.update();
        tutorial.bind(player, TutorialGuide.LESSON_COUNT);
        if (currentStage != STAGE_TUTORIAL) captureCheckpoint();
        System.out.println(startMessage);
    }

    private void updateMusicInput() {

        if (!KeyBindings.justPressed(KeyBindings.Action.MUTE)) {

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

        startStage(STAGE_TUTORIAL);

    }

    private void startStage(int stage) {
        resetRunData(); disposeGameObjects();
        currentStage = stage;
        selectStageTextures(); createPlatforms(); createGameObjects();
        camera.position.set(VIEW_WIDTH / 2, WORLD_HEIGHT / 2, 0); camera.update();
        gameMode = GameMode.PLAYING;
        menuPage = MenuPage.MAIN;
        runStats.reset(Math.max(1, stage));
        tutorial.bind(player, stage == STAGE_TUTORIAL ? 0 : TutorialGuide.LESSON_COUNT);
        if (stage == STAGE_TUTORIAL) tutorial.requireTarget();
        if (currentStage != STAGE_TUTORIAL) captureCheckpoint();
        if (stage != STAGE_TUTORIAL) stageTransition.begin(stage, false);
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

        savedHealth = GameBalance.BASE_HEALTH;

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
        if(lamplighter!=null){lamplighter.close();lamplighter=null;}
        if(sideBoss!=null){sideBoss.close();sideBoss=null;}

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
        float dt=Math.min(Gdx.graphics.getDeltaTime(),.05f),blend=1-(float)Math.exp(-13*dt);
        float targetX=MathUtils.clamp(player.getCenterX()+(player.isFacingRight()?60:-60)+feedback.cameraKickX(),VIEW_WIDTH/2,getCurrentWorldWidth()-VIEW_WIDTH/2);
        camera.position.x=MathUtils.lerp(camera.position.x,targetX,blend);
        float targetY=player.getY()+112+MathUtils.clamp(player.getVelocityY()*.06f,-30,40);
        targetY=MathUtils.clamp(targetY,StageLayout.bottom(currentStage)+VIEW_HEIGHT/2,StageLayout.top(currentStage)-VIEW_HEIGHT/2);
        camera.position.y=MathUtils.lerp(camera.position.y,targetY,blend);
        if(player.isGrounded())camera.position.y=Math.min(camera.position.y,player.getY()+118);
        camera.position.x=MathUtils.clamp(camera.position.x,VIEW_WIDTH/2,getCurrentWorldWidth()-VIEW_WIDTH/2);
        camera.position.y=MathUtils.clamp(camera.position.y,StageLayout.bottom(currentStage)+VIEW_HEIGHT/2,StageLayout.top(currentStage)-VIEW_HEIGHT/2);
        camera.update();
    }

    private float getCurrentWorldWidth() {
        if (currentStage == STAGE_TUTORIAL) return TUTORIAL_WORLD_WIDTH;

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
        if (lastAttackSequence != player.getAttackCount()) {
            lastAttackSequence = player.getAttackCount(); enemiesHitThisAttack.clear(); bossHitThisAttack = false;
            lamplighterHitThisAttack=false;
            sideBossHitThisAttack=false;
        }

        if (!player.isAttacking()) {

            enemiesHitThisAttack.clear();

            bossHitThisAttack = false;
            lamplighterHitThisAttack=false;
            sideBossHitThisAttack=false;

            return;

        }

        if (!player.isAttackActive()) return;

        Rectangle attackBounds =

                player.getAttackBounds();
        if(lamplighter!=null&&!lamplighterHitThisAttack){
            int dealt=lamplighter.hit(attackBounds,player.getCurrentAttackDamage());
            if(dealt>0){lamplighterHitThisAttack=true;lamplighterImpact(dealt,player.isComboFinisher());}
        }
        if(sideBoss!=null&&!sideBossHitThisAttack){
            int dealt=sideBoss.hit(attackBounds,player.getCurrentAttackDamage());
            if(dealt>0){sideBossHitThisAttack=true;sideBossImpact(dealt,player.isComboFinisher());}
        }

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

                        player.getCurrentAttackDamage(),

                        player.getCenterX(),player.isComboFinisher()

                );

                int dealt = healthBeforeHit - enemy.getHealth();
                Rectangle enemyBounds = enemy.getBounds();
                swordImpact(enemyBounds,dealt,player.isComboFinisher(),enemyBounds.y+enemyBounds.height+14);
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
            fallenGod.takeDamage(player.getCurrentAttackDamage());
            int dealt = healthBeforeHit - fallenGod.getHealth();
            Rectangle bossBounds = fallenGod.getBounds();
            swordImpact(bossBounds,dealt,player.isComboFinisher(),Math.min(200,bossBounds.y+bossBounds.height+14));
            if (dealt > 0 && feedback.allowImpactSound()) combatHitSound.play(effectsVolume * .46f);

            bossHitThisAttack = true;

        }

    }

    private void checkSkillAttack() {
        if (lastSkillSequence != player.getSkillCount()) {
            lastSkillSequence = player.getSkillCount(); enemiesHitThisSkill.clear(); bossHitThisSkill = false;
            lamplighterHitThisSkill=false;
            sideBossHitThisSkill=false;
        }
        if (!player.isUsingSkill()) { enemiesHitThisSkill.clear(); bossHitThisSkill = false; lamplighterHitThisSkill=sideBossHitThisSkill=false;return; }
        if (!player.isSkillActive()) return;
        Rectangle bounds = player.getSkillBounds();
        if(lamplighter!=null&&!lamplighterHitThisSkill){
            float center=lamplighter.boss.cx();
            if(player.isFacingRight()?center>=player.getSkillSweepStartX():center<=player.getSkillSweepStartX()){
                int dealt=lamplighter.hit(bounds,player.getSkillDamage());
                if(dealt>0){lamplighterHitThisSkill=true;lamplighterImpact(dealt);}
            }
        }
        if(sideBoss!=null&&!sideBossHitThisSkill){
            float center=sideBoss.boss.cx();
            if(player.isFacingRight()?center>=player.getSkillSweepStartX():center<=player.getSkillSweepStartX()){
                int dealt=sideBoss.hit(bounds,player.getSkillDamage());
                if(dealt>0){sideBossHitThisSkill=true;sideBossImpact(dealt);}
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy.getHealth() <= 0 || enemiesHitThisSkill.contains(enemy) || !bounds.overlaps(enemy.getBounds())) continue;
            float targetCenter = enemy.getBounds().x + enemy.getBounds().width / 2;
            if (player.isFacingRight() ? targetCenter < player.getSkillSweepStartX() : targetCenter > player.getSkillSweepStartX()) continue;
            enemy.takeDamage(player.getSkillDamage(), player.getCenterX(), true);
            enemiesHitThisSkill.add(enemy);
        }
        if (fallenGod != null && fallenGod.isAlive() && !bossHitThisSkill && bounds.overlaps(fallenGod.getBounds())
                && (player.isFacingRight() ? fallenGod.getBounds().x + fallenGod.getBounds().width/2 >= player.getSkillSweepStartX()
                        : fallenGod.getBounds().x + fallenGod.getBounds().width/2 <= player.getSkillSweepStartX())) {
            fallenGod.takeDamage(player.getSkillDamage());
            bossHitThisSkill = true;
        }
    }

    private void lamplighterImpact(int dealt){
        lamplighterImpact(dealt,false,false);
    }
    private void lamplighterImpact(int dealt,boolean heavy){
        lamplighterImpact(dealt,heavy,true);
    }
    private void lamplighterImpact(int dealt,boolean heavy,boolean sword){
        Rectangle r=lamplighter.boss.body;
        if(sword)swordImpact(r,dealt,heavy,r.y+r.height+14);
        else feedback.hit(r.x+r.width/2,r.y+65,dealt,false,heavy,r.y+r.height+14);
        if(combatHitSound!=null&&feedback.allowImpactSound())combatHitSound.play(effectsVolume*.55f);
    }

    private void sideBossImpact(int dealt){
        sideBossImpact(dealt,false,false);
    }
    private void sideBossImpact(int dealt,boolean heavy){
        sideBossImpact(dealt,heavy,true);
    }
    private void sideBossImpact(int dealt,boolean heavy,boolean sword){
        Rectangle r=sideBoss.boss.body;
        if(sword)swordImpact(r,dealt,heavy,r.y+r.height+14);
        else feedback.hit(r.x+r.width/2,r.y+65,dealt,false,heavy,r.y+r.height+14);
        if(combatHitSound!=null&&feedback.allowImpactSound())combatHitSound.play(effectsVolume*.55f);
    }
    private void swordImpact(Rectangle target,int dealt,boolean heavy,float labelY){
        Rectangle blade=player.getAttackBounds();
        float x=(Math.max(blade.x,target.x)+Math.min(blade.x+blade.width,target.x+target.width))*.5f;
        float y=(Math.max(blade.y,target.y)+Math.min(blade.y+blade.height,target.y+target.height))*.5f;
        feedback.swordHit(x,y,dealt,heavy,labelY);
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

        spriteBatch.normal();
        drawBackground();
        drawMoonAtmosphere();
        spriteBatch.normal();

        drawGroundTiles();

        drawPlatformTiles();

        gimmicks.draw(spriteBatch,uiTheme,uiFont,player,terrainTiles.get(stage2GroundTileTexture));
        if(lamplighter!=null)lamplighter.drawRoom(spriteBatch,uiTheme,uiFont,player,terrainTiles.get(stage2GroundTileTexture));
        if(sideBoss!=null)sideBoss.drawRoom(spriteBatch,uiTheme,uiFont,player,terrainTiles.get(groundTileTexture));
        drawMerchant();
        regionGuide.drawSupplies(spriteBatch, uiTheme, healPotionIcon, attackPotionIcon, goldIcon);
        if (currentStage != STAGE_TUTORIAL && currentStage != STAGE_GODDESS_CHURCH)
            journeyView.exit(spriteBatch, uiFont, getCurrentWorldWidth() - 100, GROUND_Y, remainingGuardians());

        spriteBatch.silhouette(false);
        for (Enemy enemy : enemies) {

            enemy.draw(spriteBatch);

        }

        if (fallenGod != null) {
            spriteBatch.normal();
            fallenGod.draw(spriteBatch);

        }

        spriteBatch.silhouette(true);
        if(lamplighter!=null){spriteBatch.normal();lamplighter.drawActor(spriteBatch,player);spriteBatch.silhouette(true);}
        if(sideBoss!=null){spriteBatch.normal();sideBoss.drawActor(spriteBatch,player);spriteBatch.silhouette(true);}
        player.draw(spriteBatch);
        spriteBatch.normal();
        for(Enemy enemy:enemies)enemy.drawProjectiles(spriteBatch);
        if(lamplighter!=null)lamplighter.drawEffects(spriteBatch);
        if(sideBoss!=null)sideBoss.drawEffects(spriteBatch);
        if(fallenGod!=null)fallenGod.drawEffects(spriteBatch);
        player.drawAttackEffects(spriteBatch);

        spriteBatch.end();

        shapeRenderer.begin(

                ShapeRenderer.ShapeType.Filled

        );

        drawEnemyHealthBars();

        if (DEBUG_ATTACK_BOUNDS) drawAttackBounds();

        feedback.drawSparks(shapeRenderer);
        shapeRenderer.end();
        enemyAttackView.draw(shapeRenderer, enemies, fallenGod);

        spriteBatch.begin();

        enemyAttackView.labels(spriteBatch, uiFont, uiTheme, enemies, fallenGod);
        feedback.drawText(spriteBatch, uiFont, uiTheme);
        spriteBatch.end();
        uiViewport.apply();
        spriteBatch.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        spriteBatch.begin();
        if (!shopOpen && !inventoryOpen && !rewardOpen && !stageTransition.active()) drawHud();

        if (shopOpen) drawShop();

        if (inventoryOpen) drawInventory();
        if (rewardOpen) drawReward();
        stageTransition.draw(spriteBatch, uiFont, screenLeft());

        spriteBatch.end();
        if (showScreenControls && !viewingMap && gameMode == GameMode.PLAYING && !shopOpen && !inventoryOpen && !rewardOpen && !stageTransition.active()) {
            gameControls.draw(player, spriteBatch, shapeRenderer, uiFont, uiTheme);
            spriteBatch.setProjectionMatrix(uiCamera.combined);
            shapeRenderer.setProjectionMatrix(uiCamera.combined);
        }

    }

    private void drawBackground() {
        float left=camera.position.x-VIEW_WIDTH/2,bottom=camera.position.y-VIEW_HEIGHT/2;
        float px=MathUtils.clamp(left/Math.max(1,getCurrentWorldWidth()-VIEW_WIDTH),0,1);
        float py=MathUtils.clamp((bottom-StageLayout.bottom(currentStage))/Math.max(1,StageLayout.top(currentStage)-StageLayout.bottom(currentStage)-VIEW_HEIGHT),0,1);
        float w=VIEW_WIDTH*1.4f,h=VIEW_HEIGHT*1.5f;
        spriteBatch.draw(backgroundTexture,left-(w-VIEW_WIDTH)*px,bottom-(h-VIEW_HEIGHT)*py,w,h);
    }

    private void drawMoonAtmosphere() {
        float left=camera.position.x-VIEW_WIDTH/2f,bottom=camera.position.y-VIEW_HEIGHT/2f;
        for(int i=0;i<28;i++){
            float x=left+((i*137.3f+moonTime*(2+i%3))%VIEW_WIDTH);
            float y=bottom+72+(i*61.7f)%240+(float)Math.sin(moonTime*.5f+i)*7;
            uiTheme.rect(spriteBatch,x,y,i%5==0?1.1f:.55f,i%5==0?1.1f:.55f,UiTheme.TEXT,.14f+.10f*(float)Math.sin(i+moonTime));
        }
        for(int i=0;i<12;i++){
            float alpha=(1-i/12f)*.16f;
            uiTheme.rect(spriteBatch,left+i*3,bottom,3,VIEW_HEIGHT,com.badlogic.gdx.graphics.Color.BLACK,alpha);
            uiTheme.rect(spriteBatch,left+VIEW_WIDTH-(i+1)*3,bottom,3,VIEW_HEIGHT,com.badlogic.gdx.graphics.Color.BLACK,alpha);
        }
    }

    private void drawGroundTiles() {
        TerrainTile tile=terrainTiles.get(groundTileTexture);
        float left=camera.position.x-VIEW_WIDTH/2-64,right=left+VIEW_WIDTH+128;
        for(Rectangle floor:StageLayout.floors(currentStage)){
            float top=floor.y+floor.height;
            if(top<camera.position.y-VIEW_HEIGHT/2-96||floor.y>camera.position.y+VIEW_HEIGHT/2)continue;
            for(float x=floor.x;x<floor.x+floor.width;x+=GROUND_TILE_SIZE){
                if(x+GROUND_TILE_SIZE<left||x>right)continue;
                tile.draw(spriteBatch,x,top,Math.min(GROUND_TILE_SIZE,floor.x+floor.width-x),Math.min(96,floor.height));
            }
        }
    }

    private void drawPlatformTiles() {
        TerrainTile tile = terrainTiles.get(platformTileTexture);
        for (Rectangle platform : platforms) {
            float top = platform.y + platform.height;
            float endX = platform.x + platform.width;
            for (float tileX = platform.x; tileX < endX; tileX += PLATFORM_TILE_WIDTH) {
                tile.draw(spriteBatch, tileX, top, Math.min(PLATFORM_TILE_WIDTH, endX - tileX), PLATFORM_TILE_HEIGHT);
            }
        }
        spriteBatch.normal();
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

        return 0f;

    }

    private float merchantX() {

        return getCurrentWorldWidth() - 250f;

    }

    private boolean isNearMerchant() {

        return currentStage != STAGE_TUTORIAL && currentStage != STAGE_GODDESS_CHURCH && player != null &&

                player.isAlive() &&

                Math.abs(player.getCenterX() - (merchantX() + GameBalance.character(48f))) <= 115f &&

                Math.abs(player.getY() - GROUND_Y) <= 70f;

    }

    private Rectangle merchantIconBounds() {

        return new Rectangle(merchantX() + GameBalance.character(48f) - 19f,
                GROUND_Y + GameBalance.character(121f), 38f, 38f);

    }

    private boolean isMouseInside(Rectangle bounds) {

        Vector2 cursor = uiViewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        return bounds.contains(cursor);

    }

    private boolean isMouseInsideWorld(Rectangle bounds) {
        Vector2 cursor = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        return bounds.contains(cursor);
    }
    private void drawMerchant() {

        if (currentStage == STAGE_TUTORIAL || currentStage == STAGE_GODDESS_CHURCH) return;

        if(moonRelics!=null) {
            moonRelics.draw(spriteBatch,(int)(merchantAnimationTime/.32f)%4,0,merchantX()+GameBalance.character(48),GROUND_Y,GameBalance.character(106),true);
        }
        else if (merchantFrames != null) {

            int frame = ((int) (merchantAnimationTime / MERCHANT_FRAME_TIME))

                    % merchantFrames.length;

            spriteBatch.draw(merchantFrames[frame], merchantX(), GROUND_Y - GameBalance.character(9f),
                    GameBalance.character(96f), GameBalance.character(128f));

        }

        if (isNearMerchant()) {

            Rectangle prompt = merchantIconBounds();

            if (merchantInteractIcon != null) {

                spriteBatch.draw(merchantInteractIcon, prompt.x, prompt.y,

                        prompt.width, prompt.height);

            }

            Rectangle keyLabel = new Rectangle(prompt.x + prompt.width / 2 - 62, prompt.y + prompt.height + 4, 124, 21);
            uiTheme.panel(spriteBatch, keyLabel.x, keyLabel.y, keyLabel.width, keyLabel.height);
            uiTheme.centered(spriteBatch, uiFont, "[" + KeyBindings.name(KeyBindings.Action.INTERACT) + "] 상점",
                    keyLabel, .7f, UiTheme.TEXT);

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
            showShopMessage("부족한 골드: " + (price - gold) + "");

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
        showShopMessage(new String[]{"공격력을 강화했습니다.", "체력을 강화했습니다.",
                "회복 포션을 구매했습니다.", "공격력 포션을 구매했습니다."}[row]);

    }

    private void useHealPotion() {
        if (healPotions <= 0) {
            showInventoryMessage("회복 포션이 없습니다. 상점에서 구매하세요.", false);
            return;
        }
        int before = player.getHealth();
        if (player.heal(GameBalance.healing(player.getMaxHealth()))) {
            healPotions--;
            runStats.potion();
            showInventoryMessage("회복량: " + (player.getHealth() - before) + " 체력", true);
        } else showInventoryMessage("체력이 가득 차 있습니다. 포션은 소비되지 않았습니다.", false);
    }

    private void showInventoryMessage(String message, boolean success) {
        inventoryMessage = message;
        inventoryMessageSuccess = success;
        inventoryMessageTimer = 3f;
    }

    private void useAttackPotion() {
        if (attackPotions <= 0) {
            showInventoryMessage("공격력 포션이 없습니다. 상점에서 구매하세요.", false);
        } else if (player.isAlive()) {
            attackPotions--;
            runStats.potion();
            player.drinkAttackPotion();
            showInventoryMessage("공격력 +" + GameBalance.POWER_BONUS + " / 지속 시간 "
                    + (int)GameBalance.POWER_DURATION + "초", true);
        }
    }

    private Rectangle inventoryUseBounds(int row) {
        return InventoryView.useBounds(screenLeft(), row);
    }

    private void updateInventoryInput() {
        if (KeyBindings.justPressed(KeyBindings.Action.INVENTORY) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            inventoryOpen = false;
            if (currentStage == STAGE_TUTORIAL) tutorial.inventoryClosed();
            player.clearActionInput();
            return;
        }
        if (KeyBindings.justPressed(KeyBindings.Action.HEAL)) useHealPotion();
        if (KeyBindings.justPressed(KeyBindings.Action.POWER)) useAttackPotion();
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        if (isMouseInside(InventoryView.closeBounds(screenLeft()))) {
            inventoryOpen = false;
            if (currentStage == STAGE_TUTORIAL) tutorial.inventoryClosed();
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
        gameHud.pointer(isMouseInside(GameHud.pauseBounds(screenLeft())),
                isMouseInside(GameHud.inventoryBounds(screenLeft())),Gdx.input.isButtonPressed(Input.Buttons.LEFT));
        gameHud.draw(spriteBatch, uiFont, screenLeft(), player, feedback.trailRatio(),
                healPotions, attackPotions, gold, healPotionIcon, attackPotionIcon, goldIcon);
        gameHud.drawBoss(spriteBatch, uiFont, screenLeft(), fallenGod);
        if(lamplighter!=null)lamplighter.drawHud(spriteBatch,uiTheme,uiFont,screenLeft());
        if(sideBoss!=null)sideBoss.drawHud(spriteBatch,uiTheme,uiFont,screenLeft());
        gameHud.drawActions(spriteBatch, uiFont, screenLeft(), player, showScreenControls,
                feedback.showsSkillInterrupted());
        int defeated = 0;
        for (Enemy enemy : enemies) if (enemy.getHealth() <= 0) defeated++;
        if (currentStage != STAGE_TUTORIAL) journeyView.progress(spriteBatch, uiFont, screenLeft(), currentStage, defeated, enemies.size(),
                player.getX(), getCurrentWorldWidth(), fallenGod, runStats, remainingGuardians());
        if (currentStage != STAGE_TUTORIAL) regionGuide.drawHint(spriteBatch, uiFont, uiTheme, screenLeft());
        if(currentStage>=1&&currentStage<=3){
            uiTheme.text(spriteBatch,uiFont,"Tab · 지도",535,273,.5f,UiTheme.MUTED,87);
            if(viewingMap)gimmicks.drawMap(spriteBatch,uiTheme,uiFont,currentStage,player);
        }
        if (currentStage == STAGE_TUTORIAL) journeyView.tutorial(spriteBatch, uiFont, screenLeft(), tutorial,
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
        if (gameMode == GameMode.MENU && menuPage == MenuPage.CONTROLS) { updateControlsInput(); return; }
        if (gameMode != GameMode.MENU) { updateResultScreenInput(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (menuPage == MenuPage.SETTINGS && settingsFromPause) {
                settingsFromPause = false; gameMode = GameMode.PAUSED;
            } else menuPage = MenuPage.MAIN;
            return;
        }
        if(menuPage==MenuPage.MAIN){
            if(Gdx.input.isKeyJustPressed(Input.Keys.UP)||Gdx.input.isKeyJustPressed(Input.Keys.W))lobbySelection=(lobbySelection+3)%4;
            if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)||Gdx.input.isKeyJustPressed(Input.Keys.S))lobbySelection=(lobbySelection+1)%4;
            if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){applyLobbyAction(lobbySelection);return;}
        }
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        if (menuPage == MenuPage.MAIN) {
            for(int row=0;row<4;row++)if(isMouseInside(mainMenuButtonBounds(row))){lobbySelection=row;applyLobbyAction(row);break;}
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
            } else if (isMouseInside(settingsButtonBounds(4))) { waitingForKey = null; menuPage = MenuPage.CONTROLS; }
            else if (isMouseInside(settingsButtonBounds(5))) {
                menuPage = MenuPage.MAIN;
                if (settingsFromPause) { settingsFromPause = false; gameMode = GameMode.PAUSED; }
            }
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

    private void applyLobbyAction(int row){
        if(row==0)restartGame();
        else if(row==1)menuPage=MenuPage.STAGE_SELECT;
        else if(row==2)menuPage=MenuPage.SETTINGS;
        else Gdx.app.exit();
    }

    private Rectangle mainMenuButtonBounds(int row) {
        return LobbyView.bounds(screenLeft(),row);
    }

    private Rectangle subMenuButtonBounds(int row) {
        return new Rectangle(screenLeft() + 55f, 222f - row * 48f, 210f, 40f);
    }

    private Rectangle settingsButtonBounds(int row) {
        return new Rectangle(55f, 244f - row * 40f, 260f, 36f);
    }

    private Rectangle resultButtonBounds(int row) {
        return ResultView.buttonBounds(screenLeft(), row);
    }

    private void drawMenuScreen() {

        uiViewport.apply();

        ScreenUtils.clear(0.03f, 0.03f, 0.06f, 1f);

        spriteBatch.setProjectionMatrix(uiCamera.combined);

        spriteBatch.begin();

        drawMenuBackground();

        if (gameMode == GameMode.MENU) {

            if (menuPage == MenuPage.MAIN) drawMainMenu();
            else if (menuPage == MenuPage.STAGE_SELECT) drawStageSelectMenu();
            else if (menuPage == MenuPage.SETTINGS) drawSettingsMenu();
            else drawControlsMenu();

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
        int mx=Gdx.input.getX(),my=Gdx.input.getY();
        if(mx!=lobbyMouseX||my!=lobbyMouseY){
            for(int i=0;i<4;i++)if(isMouseInside(mainMenuButtonBounds(i)))lobbySelection=i;
            lobbyMouseX=mx;lobbyMouseY=my;
        }
        lobbyView.draw(spriteBatch,uiFont,uiTheme,screenLeft(),lobbySelection,
                Gdx.input.isButtonPressed(Input.Buttons.LEFT),moonTime,clearRecords.read(1));
    }

    private void drawStageSelectMenu() {
        float left = screenLeft();
        uiTheme.rect(spriteBatch, left, 0, 640, 360, com.badlogic.gdx.graphics.Color.BLACK, .42f);
        uiTheme.text(spriteBatch, uiFont, "지역 선택", left + 55, 328, 1.15f, UiTheme.TEXT, 260);
        uiTheme.text(spriteBatch, uiFont, "연습할 지역을 선택하세요.", left + 55, 296, .65f, UiTheme.MUTED, 260);
        String[] names = {"신록의 폐허", "성벽", "잿빛 전장", "여신의 교회"};
        for (int row = 0; row < 4; row++) {
            if (isMouseInside(subMenuButtonBounds(row))) previewStage = row + 1;
            drawMenuButton(subMenuButtonBounds(row), "0" + (row + 1) + "  " + names[row]);
        }
        drawMenuButton(subMenuButtonBounds(4), "돌아가기");
        int index = previewStage - 1;
        uiTheme.panel(spriteBatch, left + 300, 40, 310, 248);
        uiTheme.text(spriteBatch, uiFont, "지역 " + previewStage, left + 314, 273, .6f, UiTheme.GOLD, 280);
        String[] difficulty = {"입문", "보통", "어려움", "보스"};
        uiTheme.text(spriteBatch, uiFont, "난이도: " + difficulty[index], left + 450, 273, .6f, UiTheme.MUTED, 146);
        Texture[] previews = {stage1BackgroundTexture, stage2BackgroundTexture, stage3BackgroundTexture,
                goddessChurchBackgroundTexture};
        float packed = spriteBatch.getPackedColor();
        spriteBatch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        spriteBatch.draw(previews[index], left + 314, 130, 282, 126);
        spriteBatch.setPackedColor(packed);
        uiTheme.text(spriteBatch, uiFont, names[index], left + 314, 116, 1f, UiTheme.TEXT, 282);
        String[] focus = {"슬라임 / 기본 조작 익히기", "기사 / 근접 전투",
                "검은 슬라임 / 거리와 타이밍", "보스 / 공격 높이에 주의"};
        uiTheme.text(spriteBatch, uiFont, focus[index], left + 314, 95, .65f, UiTheme.MUTED, 282);
        uiTheme.text(spriteBatch, uiFont, "기본 능력치로 새로 시작합니다.", left + 314, 70, .6f, UiTheme.GOLD, 282);
        ClearRecords.Summary record = clearRecords.read(previewStage);
        uiTheme.text(spriteBatch, uiFont, record.exists() ? "최고 기록 " + JourneyView.time(record.bestSeconds())
                + " / 클리어 " + record.clears() : "이 지역에서 시작한 클리어 기록이 없습니다.",
                left + 314, 53, .52f, UiTheme.MUTED, 282);
    }

    private Rectangle controlBounds(int index) {
        return new Rectangle(30f + (index / 6) * 298f, 248f - (index % 6) * 34f, 282f, 30f);
    }

    private void updateControlsInput() {
        if (waitingForKey != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                waitingForKey = null;
                keyMessage = "키 변경을 취소했습니다.";
                return;
            }
            for (int key = 1; key <= Input.Keys.MAX_KEYCODE; key++) {
                if (!Gdx.input.isKeyJustPressed(key)) continue;
                if (!KeyBindings.valid(key)) {
                    keyMessage = "Esc, Enter, Tab은 메뉴 전용 키입니다.";
                    return;
                }
                KeyBindings.bind(waitingForKey, key, true);
                keyMessage = waitingForKey.label + ": " + KeyBindings.name(waitingForKey) + " (저장됨)";
                waitingForKey = null;
                return;
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            menuPage = MenuPage.SETTINGS;
            return;
        }
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        for (KeyBindings.Action action : KeyBindings.Action.values()) {
            if (isMouseInside(controlBounds(action.ordinal()))) {
                waitingForKey = action;
                keyMessage = action.label + "에 사용할 키를 누르세요. (Esc 취소)";
                return;
            }
        }
        if (isMouseInside(new Rectangle(160f, 15f, 150f, 36f))) {
            KeyBindings.reset(true);
            keyMessage = "기본 키 설정으로 복원했습니다.";
        } else if (isMouseInside(new Rectangle(330f, 15f, 150f, 36f))) menuPage = MenuPage.SETTINGS;
    }

    private void drawControlsMenu() {
        uiTheme.panel(spriteBatch, 16, 8, 608, 338);
        uiTheme.centered(spriteBatch, uiFont, "키 설정", new Rectangle(160, 309, 320, 27), 1.1f, UiTheme.GOLD);
        uiTheme.centered(spriteBatch, uiFont, "중복 키는 서로 교환됩니다. Esc / Enter / Tab은 변경할 수 없습니다.",
                new Rectangle(30, 285, 580, 18), .61f, UiTheme.MUTED);
        for (KeyBindings.Action action : KeyBindings.Action.values()) {
            Rectangle b = controlBounds(action.ordinal());
            boolean active = waitingForKey == action || isMouseInside(b);
            uiTheme.button(spriteBatch, uiFont, b, "", true, active, false, .8f);
            uiTheme.label(spriteBatch, uiFont, action.label, new Rectangle(b.x + 4, b.y, 138, b.height),
                    .8f, UiTheme.TEXT, com.badlogic.gdx.utils.Align.left);
            String name = waitingForKey == action ? "입력 대기..." : KeyBindings.name(action);
            uiTheme.label(spriteBatch, uiFont, name, new Rectangle(b.x + 142, b.y, 136, b.height),
                    .78f, active ? UiTheme.GOLD : UiTheme.TEXT, com.badlogic.gdx.utils.Align.right);
        }
        uiTheme.label(spriteBatch, uiFont, keyMessage, new Rectangle(24, 52, 592, 20),
                .65f, UiTheme.MUTED, com.badlogic.gdx.utils.Align.left);
        drawMenuButton(new Rectangle(160f, 15f, 150f, 36f), "기본값 복원");
        drawMenuButton(new Rectangle(330f, 15f, 150f, 36f), "돌아가기");
    }


    private void drawSettingsMenu() {
        float left = screenLeft();
        uiTheme.rect(spriteBatch, left, 0, 640, 360, com.badlogic.gdx.graphics.Color.BLACK, .35f);
        uiTheme.panel(spriteBatch, left + 40, 16, 290, 328);
        uiTheme.text(spriteBatch, uiFont, "설정", left + 55, 328, 1.15f, UiTheme.TEXT, 260);
        uiTheme.text(spriteBatch, uiFont, "음량은 - / +로 25%씩 조절합니다.", left + 55, 296, .65f, UiTheme.MUTED, 260);
        for (int row = 0; row < 2; row++) {
            Rectangle area = settingsButtonBounds(row);
            float volume = row == 0 ? (musicMuted ? 0 : musicVolume) : effectsVolume;
            uiTheme.panel(spriteBatch, area.x, area.y, area.width, area.height);
            uiTheme.centered(spriteBatch, uiFont, (row == 0 ? "배경음 " : "효과음 ")
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
        drawMenuButton(settingsButtonBounds(2), "화면 조작 버튼 " + (showScreenControls ? "켜짐" : "꺼짐"));
        drawMenuButton(settingsButtonBounds(3), Gdx.graphics.isFullscreen() ? "전체 화면" : "창 모드");
        drawMenuButton(settingsButtonBounds(4), "키 설정");
        drawMenuButton(settingsButtonBounds(5), "돌아가기");
        uiTheme.text(spriteBatch, uiFont, "설정은 자동으로 저장됩니다.", left + 55, 32, .58f, UiTheme.MUTED, 260);
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
        uiTheme.centered(spriteBatch, uiFont, text, bounds, 1.1f, UiTheme.GOLD);
    }

    @Override

    public void resize(

            int width,

            int height) {

        uiViewport.update(width, height, false);
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

        if(moonRelics!=null) moonRelics.close();
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
        fontGenerator.dispose();
        if (tutorialBackground != null) tutorialBackground.dispose();
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
