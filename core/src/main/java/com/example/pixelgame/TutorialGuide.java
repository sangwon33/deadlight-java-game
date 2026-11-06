package com.example.pixelgame;

/** Successful practice actions, including the imported skill and local inventory lesson. */
final class TutorialGuide {
    static final int LESSON_COUNT = 7;
    private int step, jumps, attacks, dashes, completedSkills, interruptedSkills,downs;
    private float walkStart, skillCooldown;
    private int chargeStage;
    private boolean charging;
    private boolean targetRequired, targetDown, casting, interrupted;
    int step() { return step; }
    boolean finished() { return step == LESSON_COUNT; }
    boolean visible() { return true; }
    void bind(Player p, int restoredStep) {
        step = Math.max(0, Math.min(LESSON_COUNT, restoredStep));
        targetRequired = targetDown = false; baseline(p);
    }
    void requireTarget() { targetRequired = true; }
    void targetDefeated() { targetDown = true; }
    void inventoryClosed() { if (step == 6) step++; }
    private void baseline(Player p) {
        downs=p.getDownCount();
        walkStart=p.getWalkDistance();jumps=p.getJumpCount();attacks=p.getAttackCount();dashes=p.getDashCount();
        completedSkills=p.getCompletedFullChargeSkillCount();interruptedSkills=p.getInterruptedSkillCount();
    }
    void skip(){step=LESSON_COUNT;}
    void update(Player p,float delta){
        skillCooldown=p.getSkillCooldown();casting=p.isUsingSkill();
        charging=p.isChargingSkill();chargeStage=p.getSkillChargeStage();
        interrupted=p.getInterruptedSkillCount()>interruptedSkills;
        if(finished()||!p.isAlive())return;
        boolean success=switch(step){
            case 0 -> p.getWalkDistance()-walkStart>=120;
            case 1 -> p.getJumpCount()>jumps && p.getY()<=60.1f;
            case 2 -> targetRequired ? targetDown : p.getAttackCount()>attacks;
            case 3 -> p.getDashCount()>dashes;
            case 4 -> p.getCompletedFullChargeSkillCount()>completedSkills;
            case 5 -> p.getDownCount()>downs;
            default -> false;
        };
        if(success){step++;baseline(p);}
    }
    String title(){return finished()?"훈련 완료  /  Enter로 모험 시작":"조작 훈련  "+(step+1)+" / "+LESSON_COUNT;}
    String instruction(){return switch(step){
        case 0 -> KeyBindings.name(KeyBindings.Action.LEFT)+" / "+KeyBindings.name(KeyBindings.Action.RIGHT)+" 또는 화면 버튼으로 이동하세요.";
        case 1 -> KeyBindings.name(KeyBindings.Action.JUMP)+"로 점프한 뒤 착지하세요. 길게 누르면 높게!";
        case 2 -> KeyBindings.name(KeyBindings.Action.ATTACK)+"를 연속으로 눌러 3타 베기. 연습용 슬라임을 쓰러뜨리세요.";
        case 3 -> KeyBindings.name(KeyBindings.Action.DASH)+"로 대시하세요. 1·2타 중에는 즉시 회피할 수 있습니다.";
        case 4 -> charging ? "정신집중 "+chargeStage+"/4 · "+GameBalance.SKILL_CHARGE_LABEL+"까지 계속 누르세요."
                : casting ? "자동 돌진! 검을 앞으로 찌릅니다."
                : skillCooldown>0 ? (interrupted?"취소되었습니다. ":"")+"재사용까지 "+(int)Math.ceil(skillCooldown)+"초"
                : KeyBindings.name(KeyBindings.Action.SKILL)+" 또는 스킬 버튼을 "+GameBalance.SKILL_CHARGE_LABEL+" 동안 누르세요.";
        case 5 -> KeyBindings.name(KeyBindings.Action.DOWN)+"를 눌러 웅크려 보세요.";
        case 6 -> KeyBindings.name(KeyBindings.Action.INVENTORY)+"로 인벤토리를 열고 같은 키 또는 Esc로 닫으세요.";
        default -> "Enter를 누르면 1지역으로 이동합니다. Tab으로 건너뛸 수도 있습니다.";
    };}
    String detail(){return step==1?"짧은 점프 / 긴 점프 / 발판 끝에서도 잠깐 점프 가능"
            :step==3?"재사용 0.42초 / 공중에서는 착지 전 한 번 사용"
            :step==4?GameBalance.SKILL_CHARGE_LABEL+" 집중 후 자동 돌진 / 일찍 놓으면 취소"
            :step==5?"지상: 웅크리기 / 얇은 발판: 아래로 내려가기"
            :step==6?"회복: "+KeyBindings.name(KeyBindings.Action.HEAL)+" / 공격력: "+KeyBindings.name(KeyBindings.Action.POWER)
            :"Tab: 훈련 건너뛰기 / Esc: 일시정지";}
}
