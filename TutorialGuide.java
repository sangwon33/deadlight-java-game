package com.example.pixelgame;

/** Observe successful player actions, never raw key presses. Only the current lesson advances. */
final class TutorialGuide {
    static final int LESSON_COUNT = 5;
    private int step;
    private float walkStart, completedFor;
    private int jumps, attacks, dashes, completedSkills, interruptedSkills;
    private float skillCooldown;
    private boolean casting, interrupted;
    private String skillPhase = "";
    int step() { return step; }
    boolean finished() { return step == LESSON_COUNT; }
    boolean visible() { return !finished() || completedFor > 0; }
    void bind(Player player, int restoredStep) {
        step = Math.max(0, Math.min(LESSON_COUNT, restoredStep)); completedFor = 0;
        baseline(player);
        observeSkill(player);
    }
    private void baseline(Player p) {
        walkStart = p.getWalkDistance(); jumps = p.getJumpCount();
        attacks = p.getAttackCount(); dashes = p.getDashCount();
        completedSkills = p.getCompletedSkillCount(); interruptedSkills = p.getInterruptedSkillCount();
    }
    private void observeSkill(Player p) {
        skillCooldown = p.getSkillCooldown(); casting = p.isUsingSkill(); skillPhase = p.getSkillPhase();
        interrupted = p.getInterruptedSkillCount() > interruptedSkills;
    }
    void skip() { step = LESSON_COUNT; completedFor = 0; }
    void update(Player p, float delta) {
        completedFor = Math.max(0, completedFor - delta);
        observeSkill(p);
        if (finished() || !p.isAlive()) return;
        boolean success = switch (step) {
            case 0 -> p.getWalkDistance() - walkStart >= 48;
            case 1 -> p.getJumpCount() > jumps;
            case 2 -> p.getAttackCount() > attacks;
            case 3 -> p.getDashCount() > dashes;
            default -> p.getCompletedSkillCount() > completedSkills;
        };
        if (success) { step++; baseline(p); if (finished()) completedFor = 5f; }
    }
    String title() {
        return finished() ? "TRAINING COMPLETE" : step == 4 ? "SHOCK SLASH  5 / 5"
                : "LEARN THE CONTROLS  " + (step + 1) + " / " + LESSON_COUNT;
    }
    String instruction() {
        return switch (step) {
            case 0 -> "Move with A / D or the arrow buttons.";
            case 1 -> "Jump with SPACE or the jump button.";
            case 2 -> "Attack with J or the sword button.";
            case 3 -> "Dash with SHIFT or the dash button.";
            case 4 -> casting ? switch (skillPhase) {
                case "CHARGING" -> "Charging... avoid incoming attacks.";
                case "STRIKE" -> "Strike! Direction stays locked.";
                default -> "Recovering... watch the Q countdown.";
            } : skillCooldown > 0 ? (interrupted ? "Interrupted. Q ready in " : "Q ready in ")
                    + (int)Math.ceil(skillCooldown) + "s." : "Use Q or the gold skill button.";
            default -> "Shock Slash breaks knight shields.";
        };
    }
    String detail() {
        return step == 4 ? "Breaks knight shields. Cooldown: 8s."
                : finished() ? "Charge safely. Watch the cooldown." : "";
    }
}
