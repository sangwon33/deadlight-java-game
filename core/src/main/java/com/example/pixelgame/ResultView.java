package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

final class ResultView implements Disposable {
    private final UiTheme ui = new UiTheme();
    static Rectangle buttonBounds(float left, int row) {
        return row == 0 ? new Rectangle(left + 154, 86, 332, 34)
                : new Rectangle(left + (row == 1 ? 154 : 326), 48, 160, 30);
    }
    static Rectangle tabBounds(float left, int page) { return new Rectangle(left + 118 + page * 204, 258, 200, 22); }
    private void metric(SpriteBatch b, BitmapFont f, float x, float y, String label, String value) {
        ui.line(b,x,y,x+126,y,.5f,UiTheme.BORDER,.6f);
        ui.text(b, f, label, x + 10, y + 32, .5f, UiTheme.MUTED, 106);
        ui.label(b, f, value, new Rectangle(x + 4, y + 3, 118, 20), .9f, UiTheme.TEXT,
                com.badlogic.gdx.utils.Align.left);
    }
    void draw(SpriteBatch b, BitmapFont f, float left, RunResult r, int hovered, boolean pressed) {
        draw(b, f, left, r, hovered, pressed, 0);
    }
    void draw(SpriteBatch b, BitmapFont f, float left, RunResult r, int hovered, boolean pressed, int page) {
        ui.rect(b, left, 0, 640, 360, UiTheme.SURFACE, .65f);
        ui.panel(b, left + 100, 14, 440, 334);
        ui.centered(b, f, r.cleared() ? "교회에 침묵이 찾아왔습니다" : "이번 여정은 여기까지입니다",
                new Rectangle(left + 118, 321, 404, 18), .6f, UiTheme.GOLD);
        ui.centered(b, f, r.cleared() ? "승리" : "게임 오버",
                new Rectangle(left + 118, 294, 404, 28), 1.7f, r.cleared() ? UiTheme.SUCCESS : UiTheme.TEXT);
        String scope = r.startStage() == 1 ? "전체 모험" : "시작 지역 " + r.startStage();
        ui.centered(b, f, scope + " / 도달 지역 " + r.stage(),
                new Rectangle(left + 118, 281, 404, 12), .6f, UiTheme.MUTED);
        for (int tab = 0; tab < 2; tab++) {
            Rectangle bounds = tabBounds(left, tab);
            ui.button(b, f, bounds, tab == 0 ? "요약" : "지역별 기록과 보상", true, tab == page, false, .65f);
            if (tab == page) ui.rect(b, bounds.x, bounds.y, bounds.width, 2, UiTheme.GOLD, 1);
        }
        if (page == 0) summary(b, f, left, r);
        else regions(b, f, left, r);
        String[] labels = r.cleared() ? new String[]{"다시 시작", "지역 선택", "메인 메뉴"}
                : new String[]{"현재 지역 재도전", "새 모험", "메인 메뉴"};
        for (int row = 0; row < 3; row++) ui.button(b, f, buttonBounds(left, row), labels[row],
                true, hovered == row, pressed, row == 0 ? 1f : .75f);
        ui.centered(b, f, r.cleared() ? "[Enter / R] 새 모험    [Tab] 상세    [Esc] 메뉴"
                        : "[Enter] 지역 재도전    [R] 새 모험    [Tab] 상세    [Esc] 메뉴",
                new Rectangle(left + 118, 24, 404, 18), .52f, UiTheme.MUTED);
    }

    private void summary(SpriteBatch b, BitmapFont f, float left, RunResult r) {
        metric(b, f, left + 118, 210, "플레이 시간", JourneyView.time(r.seconds()));
        metric(b, f, left + 257, 210, "처치 수", Integer.toString(r.defeated()));
        metric(b, f, left + 396, 210, "획득 / 남은 골드", r.goldEarned() + " / " + r.goldRemaining());
        metric(b, f, left + 118, 164, "받은 피해", r.damageTaken() + " 체력");
        metric(b, f, left + 257, 164, "포션 사용", Integer.toString(r.potionsUsed()));
        metric(b, f, left + 396, 164, "지역 재도전", Integer.toString(r.retries()));
        ui.text(b, f, "강화: 공격력 +" + r.attackLevels() + " / 상점 체력 +" + r.healthLevels() * GameBalance.HEALTH_UPGRADE
                + " / 보상 체력 +" + r.bonusHealth(), left + 118, 161, .6f, UiTheme.TEXT, 404);
        ui.text(b, f, "행동: 공격 " + r.attacks() + " / 돌진 찌르기 " + r.skills() + " / 대시 " + r.dashes(),
                left + 118, 145, .58f, UiTheme.MUTED, 404);
        String best = r.clearCount() == 0 ? "아직 클리어 기록이 없습니다."
                : (r.newBest() ? "최고 기록 갱신!  " : "최고 기록  ") + JourneyView.time(r.bestSeconds())
                        + " / 클리어 " + r.clearCount();
        ui.centered(b, f, r.cleared() ? best : "재도전 시 지역 진입 당시의 체력, 강화, 골드, 포션, 보상을 복원합니다.",
                new Rectangle(left + 118, 122, 404, 12), .53f, r.cleared() ? UiTheme.GOLD : UiTheme.MUTED);
    }

    private void regions(SpriteBatch b, BitmapFont f, float left, RunResult r) {
        int index = 0;
        for (RegionResult region : r.regions()) {
            float y = 224 - index++ * 28;
            ui.rect(b, left + 118, y, 404, 27, UiTheme.BORDER, .25f);
            ui.text(b, f, "0" + region.stage() + " " + JourneyView.regionName(region.stage()) + (region.cleared() ? " / 완료" : " / 실패"),
                    left + 125, y + 22, .56f, region.cleared() ? UiTheme.SUCCESS : UiTheme.TEXT, 225);
            ui.text(b, f, JourneyView.time(region.seconds()) + " / 피해 " + region.damage() + " / 재도전 " + region.retries(),
                    left + 365, y + 22, .56f, UiTheme.TEXT, 148);
            String reward = region.stage() == 4 && region.cleared() ? "승리" : "--";
            for (StageReward choice : r.rewards()) if (choice.stage() == region.stage()) reward = choice.detail();
            ui.text(b, f, "보상  " + reward, left + 125, y + 10, .5f, UiTheme.GOLD, 225);
            ui.text(b, f, "공격 " + region.attacks() + " / 스킬 " + region.skills() + " / 대시 " + region.dashes() + " / 포션 " + region.potions(),
                    left + 365, y + 10, .5f, UiTheme.MUTED, 148);
        }
        if (r.regions().isEmpty()) ui.text(b, f, "지역별 기록이 없습니다.", left + 130, 225, .7f, UiTheme.MUTED, 380);
        ui.centered(b, f, "재도전 횟수와 포션 사용 수입니다. 실패한 시도까지 합산합니다.",
                new Rectangle(left + 118, 122, 404, 12), .51f, UiTheme.MUTED);
    }
    @Override public void dispose() { ui.dispose(); }
}
