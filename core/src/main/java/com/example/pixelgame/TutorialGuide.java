package com.example.pixelgame;

/** Successful practice actions, including the imported skill and local inventory lesson. */
final class TutorialGuide {
    static final int LESSON_COUNT = 7;
    private int step, jumps, attacks, dashes, completedSkills, interruptedSkills;
    private float walkStart, skillCooldown, lookPractice;
    private int chargeStage;
    private boolean charging;
    private boolean targetRequired, targetDown, casting, interrupted;
    private boolean onLookPlatform,lookedBelow,droppedBelow;
    private int dropsAfterLook;
    int step() { return step; }
    boolean finished() { return step == LESSON_COUNT; }
    boolean visible() { return true; }
    boolean practicingLook() { return step==5; }
    boolean onLookPlatform() { return onLookPlatform; }
    void bind(Player p, int restoredStep) {
        step = Math.max(0, Math.min(LESSON_COUNT, restoredStep));
        targetRequired = targetDown = false; baseline(p);
    }
    void requireTarget() { targetRequired = true; }
    void targetDefeated() { targetDown = true; }
    void inventoryClosed() { if (step == 6) step++; }
    private void baseline(Player p) {
        lookPractice=0;
        onLookPlatform=lookedBelow=droppedBelow=false;dropsAfterLook=p.getDropCount();
        walkStart=p.getWalkDistance();jumps=p.getJumpCount();attacks=p.getAttackCount();dashes=p.getDashCount();
        completedSkills=p.getCompletedFullChargeSkillCount();interruptedSkills=p.getInterruptedSkillCount();
    }
    void skip(){step=LESSON_COUNT;}
    void update(Player p,float delta){
        skillCooldown=p.getSkillCooldown();casting=p.isUsingSkill();
        charging=p.isChargingSkill();chargeStage=p.getSkillChargeStage();
        interrupted=p.getInterruptedSkillCount()>interruptedSkills;
        if(finished()||!p.isAlive())return;
        if(step==5){
            onLookPlatform=p.isGrounded()&&StageLayout.tutorialLookPlatform(p.getSupport());
            if(!lookedBelow){
                lookPractice=onLookPlatform&&p.isLookingDown()?lookPractice+Math.max(0,delta):0;
                // Leave time for the camera to actually reveal the landing below.
                if(lookPractice>=.4f){lookedBelow=true;dropsAfterLook=p.getDropCount();}
            }
            droppedBelow=lookedBelow&&p.getDropCount()>dropsAfterLook;
        }
        boolean success=switch(step){
            case 0 -> p.getWalkDistance()-walkStart>=120;
            case 1 -> p.getJumpCount()>jumps && p.isGrounded();
            case 2 -> targetRequired ? targetDown : p.getAttackCount()>attacks;
            case 3 -> p.getDashCount()>dashes;
            case 4 -> p.getCompletedFullChargeSkillCount()>completedSkills;
            case 5 -> droppedBelow&&p.isGrounded()&&p.getY()<=StageLayout.TUTORIAL_LANDING_TOP+.1f;
            default -> false;
        };
        if(success){step++;baseline(p);}
    }
    String title(){return finished()?"훈련 완료  /  Enter로 모험 시작":"조작 훈련  "+(step+1)+" / "+LESSON_COUNT;}
    String instruction(){return switch(step){
        case 0 -> KeyBindings.name(KeyBindings.Action.LEFT)+" / "+KeyBindings.name(KeyBindings.Action.RIGHT)+"로 이동하세요.";
        case 1 -> KeyBindings.name(KeyBindings.Action.JUMP)+"로 점프한 뒤 착지하세요.";
        case 2 -> KeyBindings.name(KeyBindings.Action.ATTACK)+"를 연속으로 눌러 허수아비를 쓰러뜨리세요.";
        case 3 -> KeyBindings.name(KeyBindings.Action.DASH)+"로 대시하세요.";
        case 4 -> charging?"정신집중 "+chargeStage+"/4 · 끝까지 누르세요."
                :casting?"자동 돌진 중!":skillCooldown>0?"재사용까지 "+(int)Math.ceil(skillCooldown)+"초"
                :KeyBindings.name(KeyBindings.Action.SKILL)+"를 "+GameBalance.SKILL_CHARGE_LABEL+" 동안 누르세요.";
        case 5 -> droppedBelow?"아래 발판에 안전하게 착지하세요."
                :!onLookPlatform?"오른쪽 높은 전망 발판에 올라가세요."
                :lookedBelow?KeyBindings.name(KeyBindings.Action.LOOK_DOWN)+"를 뗀 뒤 짧게 눌렀다 놓으세요."
                :KeyBindings.name(KeyBindings.Action.LOOK_DOWN)+"를 "+GameSettings.current.label(GameSettings.Option.LOOK_DELAY)+" 길게 눌러 아래를 살펴보세요.";
        case 6 -> KeyBindings.name(KeyBindings.Action.INVENTORY)+"로 가방을 열었다 닫으세요.";
        default -> "Enter를 눌러 모험을 시작하세요.";
    };}
    String detail(){return switch(step){
        case 0 -> KeyBindings.name(KeyBindings.Action.LOOK_UP)+" / "+KeyBindings.name(KeyBindings.Action.LOOK_DOWN)+" "+GameSettings.current.label(GameSettings.Option.LOOK_DELAY)+" 유지: 위·아래 살펴보기";
        case 1 -> "짧게 / 길게 누르면 점프 높이가 달라집니다.";
        case 2 -> "공격 키를 다시 눌러 3타까지 이어갑니다.";
        case 3 -> "1·2타 → 대시 → 점프 / 공중 대시 1회";
        case 4 -> GameBalance.SKILL_CHARGE_LABEL+" 집중 후 자동 돌진 / 일찍 놓으면 취소";
        case 5 -> !onLookPlatform&&!droppedBelow?"점프를 길게 눌러 높은 발판으로 이동하세요."
                :lookedBelow?"길게 누르면 살펴보기 · 짧게 누르면 한 층 아래로"
                :"아래에 있는 은빛 착지 표식을 확인하세요.";
        case 6 -> "회복: "+KeyBindings.name(KeyBindings.Action.HEAL)+" / 공격력: "+KeyBindings.name(KeyBindings.Action.POWER);
        default -> "Esc: 일시정지";
    };}
}
