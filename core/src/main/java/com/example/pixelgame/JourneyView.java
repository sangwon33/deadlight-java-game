package com.example.pixelgame;

import java.util.Locale;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

final class JourneyView {
    private final UiTheme ui;
    static String regionName(int stage) { return StageLayout.name(stage); }
    JourneyView(UiTheme ui) { this.ui = ui; }
    static String time(float seconds) {
        long tenths = Math.max(0, Math.round(seconds * 10));
        return String.format(Locale.ROOT, "%02d:%02d.%d", tenths / 600, (tenths / 10) % 60, tenths % 10);
    }
    static Rectangle skipBounds(float left) { return new Rectangle(left + 177, 252, 35, 16); }

    void progress(SpriteBatch b, BitmapFont f, float left, int stage, int defeated, int total,
                  float position, float worldWidth, FallenGod boss, RunStats stats, int guardians) {
        ui.line(b,left+246,318,left+470,318,.5f,UiTheme.BORDER,.7f);
        ui.text(b, f, "0" + stage + "  " + StageLayout.name(stage), left + 246, 345, .64f, UiTheme.TEXT, 174);
        ui.text(b, f, time(stats.seconds), left + 425, 344, .54f, UiTheme.MUTED, 45);
        float progress = stage == 4 && boss != null ? 1f - (float)boss.getHealth() / boss.getMaxHealth()
                : MathUtils.clamp((position - 100) / (worldWidth - 200), 0, 1);
        String goal = stage == 4 ? "타락한 신을 쓰러뜨리세요"
                : guardians > 0 ? "수문장 "+guardians+"명 남음 / "+defeated+"/"+total+" 처치"
                : "봉인 해제 / 출구 진행 "+Math.round(progress*100)+"%";
        ui.text(b, f, goal, left + 246, 328, .52f, UiTheme.MUTED, 224);
        ui.rect(b, left + 246, 318, 224, 1, UiTheme.BORDER, 1);
        ui.rect(b, left + 246, 318, 224 * progress, 1, UiTheme.GOLD, 1);
    }

    void tutorial(SpriteBatch b, BitmapFont f, float left, TutorialGuide guide, boolean hovered, boolean pressed) {
        if (!guide.visible()) return;
        boolean detail = !guide.detail().isEmpty();
        ui.panel(b, left + 12, detail ? 216 : 230, 212, detail ? 64 : 50);
        ui.text(b, f, guide.title(), left + 22, 267, .6f, UiTheme.GOLD, 146);
        ui.text(b, f, guide.instruction(), left + 22, 250, .6f, UiTheme.TEXT, 192);
        ui.text(b, f, guide.detail(), left + 22, 237, .58f, UiTheme.MUTED, 192);
        float segment=(190-(TutorialGuide.LESSON_COUNT-1)*4f)/TutorialGuide.LESSON_COUNT;
        for (int i = 0; i < TutorialGuide.LESSON_COUNT; i++) ui.rect(b, left + 22 + i * (segment+4), detail ? 223 : 238, segment, 2,
                i < guide.step() ? UiTheme.SUCCESS : UiTheme.BORDER, 1);
        if (!guide.finished()) ui.button(b, f, skipBounds(left), "건너뛰기", true, hovered, pressed, .48f);
    }

    void exit(SpriteBatch b, BitmapFont f, float x, float ground, int guardians) {
        Color color = guardians > 0 ? UiTheme.MUTED : UiTheme.GOLD;
        ui.rect(b,x,ground,2,54,color,.8f);
        ui.panel(b,x-32,ground+54,102,22);
        ui.centered(b,f,guardians > 0 ? "봉인 "+guardians+" / 수문장" : "출구 >",
                new Rectangle(x-32,ground+54,102,22),.65f,color);
    }

    void records(SpriteBatch b, BitmapFont f, float left, ClearRecords.Summary record) {
        ui.panel(b, left + 338, 16, 282, 70);
        ui.text(b, f, "개편판 모험 기록", left + 350, 73, .65f, UiTheme.GOLD, 258);
        ui.text(b, f, record.exists() ? "최고 기록 " + time(record.bestSeconds()) + "   클리어 " + record.clears()
                : "아직 전체 클리어 기록이 없습니다.", left + 350, 55, .76f, UiTheme.TEXT, 258);
        ui.text(b, f, record.exists() ? "최소 받은 피해 " + record.leastDamage() + " 체력"
                : "모든 지역을 클리어하면 기록이 저장됩니다.", left + 350, 37, .6f, UiTheme.MUTED, 258);
    }
}
