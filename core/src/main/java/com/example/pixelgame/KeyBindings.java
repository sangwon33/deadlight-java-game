package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

/** Shared by gameplay and UI, so prompts always display the active controls. */
public final class KeyBindings {
    public enum Action {
        LEFT("왼쪽 이동", Input.Keys.A), RIGHT("오른쪽 이동", Input.Keys.D),
        JUMP("점프", Input.Keys.SPACE), ATTACK("공격", Input.Keys.J),
        DASH("대시", Input.Keys.SHIFT_LEFT), SKILL("돌진 찌르기", Input.Keys.Q), INTERACT("상호작용", Input.Keys.E),
        INVENTORY("인벤토리", Input.Keys.I), HEAL("회복 포션", Input.Keys.NUM_1),
        POWER("공격력 포션", Input.Keys.NUM_2), MUTE("음악 음소거", Input.Keys.M), DOWN("웅크리기 / 내려가기", Input.Keys.S);
        public final String label;
        final int defaultKey;
        Action(String label, int defaultKey) { this.label = label; this.defaultKey = defaultKey; }
    }
    private static final int[] keys = new int[Action.values().length];
    static { reset(false); }
    public static void load() {
        Preferences prefs = Gdx.app.getPreferences("deadlight-controls");
        reset(false);
        for (Action action : Action.values()) {
            int key = prefs.getInteger(action.name(), action.defaultKey);
            if (prefs.contains(action.name()) && valid(key)) bind(action, key, false);
        }
    }
    public static int key(Action action) { return keys[action.ordinal()]; }
    public static String name(Action action) {
        int key = key(action);
        if (key == Input.Keys.SPACE) return "스페이스";
        if (key == Input.Keys.SHIFT_LEFT) return "왼쪽 Shift";
        if (key == Input.Keys.SHIFT_RIGHT) return "오른쪽 Shift";
        return Input.Keys.toString(key);
    }
    public static boolean valid(int key) {
        return key > Input.Keys.UNKNOWN && key <= Input.Keys.MAX_KEYCODE
                && key != Input.Keys.ESCAPE && key != Input.Keys.ENTER && key != Input.Keys.TAB;
    }
    public static boolean pressed(Action action) { return input(action, false); }
    public static boolean justPressed(Action action) { return input(action, true); }
    private static boolean input(Action action, boolean once) {
        int key = key(action);
        boolean down = once ? Gdx.input.isKeyJustPressed(key) : Gdx.input.isKeyPressed(key);
        // Default keyboard pairs remain convenient until that action is remapped.
        int alternate = key == action.defaultKey && action == Action.DASH ? Input.Keys.SHIFT_RIGHT
                : key == action.defaultKey && action == Action.HEAL ? Input.Keys.NUMPAD_1
                : key == action.defaultKey && action == Action.POWER ? Input.Keys.NUMPAD_2 : -1;
        // An explicitly assigned key takes precedence over a default alias.
        for (Action other : Action.values()) if (other != action && key(other) == alternate) alternate = -1;
        return down || alternate >= 0 && (once ? Gdx.input.isKeyJustPressed(alternate) : Gdx.input.isKeyPressed(alternate));
    }
    public static void bind(Action action, int key, boolean save) {
        if (!valid(key)) return;
        int previous = key(action);
        for (Action other : Action.values()) if (other != action && key(other) == key) keys[other.ordinal()] = previous;
        keys[action.ordinal()] = key;
        if (save) save();
    }
    public static void reset(boolean save) {
        for (Action action : Action.values()) keys[action.ordinal()] = action.defaultKey;
        if (save) save();
    }
    public static void save() {
        Preferences prefs = Gdx.app.getPreferences("deadlight-controls");
        for (Action action : Action.values()) prefs.putInteger(action.name(), key(action));
        prefs.flush();
    }
}
