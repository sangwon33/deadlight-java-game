package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Select, review, then commit one free reward before the next region. */
final class RewardView {
    private final UiTheme ui;
    RewardView(UiTheme ui) { this.ui = ui; }
    static Rectangle cardBounds(float left, int index) { return new Rectangle(left + 44 + index * 190, 115, 172, 145); }
    static Rectangle continueBounds(float left) { return new Rectangle(left + 170, 64, 300, 34); }
    static String title(int index) { return new String[]{"휴식", "골드", "체력 증진"}[index]; }
    void draw(SpriteBatch b, BitmapFont f, float left, int stage, Player p, int gold,
              int selected, int hovered, boolean continueHover, boolean pressed, Texture[] icons) {
        ui.rect(b, left, 0, 640, 360, UiTheme.SURFACE, .64f);
        ui.panel(b, left + 32, 42, 576, 286);
        ui.text(b, f, "지역 " + stage + " 완료 / " + JourneyView.regionName(stage), left + 52, 309, .85f, UiTheme.GOLD, 536);
        ui.text(b, f, "보상 하나를 선택하고 확인한 뒤 다음 지역으로 이동하세요: " + (stage + 1) + ".",
                left + 52, 284, .67f, UiTheme.TEXT, 536);
        boolean full = p.getHealth() == p.getMaxHealth();
        String[] effects = {full ? "+1 회복 포션" : "체력 모두 회복", "+" + GameBalance.CLEAR_GOLD + " 골드", "+" + GameBalance.CLEAR_HEALTH + " 최대 체력"};
        String[] previews = {full ? "최대 체력 / 포션 1개 지급" : "체력 " + p.getHealth() + "/" + p.getMaxHealth() + " -> " + p.getMaxHealth() + "/" + p.getMaxHealth(),
                "골드 " + gold + " -> " + (gold + GameBalance.CLEAR_GOLD),
                "체력 " + p.getHealth() + "/" + p.getMaxHealth() + " -> " + (p.getHealth() + GameBalance.CLEAR_HEALTH) + "/" + (p.getMaxHealth() + GameBalance.CLEAR_HEALTH)};
        String[] details = {full ? "회복 포션 단축키로 사용할 수 있습니다." : "체력을 회복하고 다음 지역으로 이동합니다.", "다음 상점에서 사용할 수 있습니다.", "현재·최대 체력 +" + GameBalance.CLEAR_HEALTH + " / 이번 모험"};
        for (int i = 0; i < 3; i++) {
            Rectangle r = cardBounds(left, i);ui.panel(b, r.x, r.y, r.width, r.height);
            if (selected == i) {
                ui.diamond(b,r.x+r.width/2,r.y+r.height,4,UiTheme.GOLD,1);
                ui.line(b,r.x+12,r.y+r.height,r.x+r.width-12,r.y+r.height,.8f,UiTheme.GOLD,1);
            } else if (hovered == i) ui.rect(b, r.x + 1, r.y + 1, r.width - 2, r.height - 2, UiTheme.GOLD, .08f);
            ui.text(b, f, "[" + (i + 1) + "]", r.x + 140, 246, .7f, UiTheme.MUTED, 24);
            if (icons[i] != null) {float color=b.getPackedColor();b.setColor(Color.WHITE);b.draw(icons[i],r.x+14,212,28,28);b.setPackedColor(color);}
            ui.text(b, f, title(i), r.x + 14, 198, .94f, UiTheme.TEXT, 144);
            ui.text(b, f, effects[i], r.x + 14, 178, .8f, UiTheme.GOLD, 144);
            ui.text(b, f, previews[i], r.x + 14, 160, .6f, UiTheme.TEXT, 144);
            ui.text(b, f, details[i], r.x + 14, 144, .56f, UiTheme.MUTED, 144);
            ui.text(b, f, selected == i ? "선택됨" : "클릭 / 숫자 " + (i + 1), r.x + 14, 128, .56f,
                    selected == i ? UiTheme.GOLD : UiTheme.MUTED, 144);
        }
        ui.button(b, f, continueBounds(left), "선택: " + title(selected) + " / 계속 [Enter]", true, continueHover, pressed, .78f);
        ui.centered(b, f, "[1 / 2 / 3] 선택    [Enter] 확정    [Esc] 일시정지",
                new Rectangle(left + 52, 44, 536, 14), .52f, UiTheme.MUTED);
    }
}
