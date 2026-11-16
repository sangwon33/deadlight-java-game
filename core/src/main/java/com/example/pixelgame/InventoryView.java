package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

final class InventoryView {
    private final UiTheme ui;
    InventoryView(UiTheme ui) { this.ui = ui; }
    static Rectangle useBounds(float left, int row) {
        return new Rectangle(left + 444, 198 - row * 78, 76, 32);
    }
    static Rectangle closeBounds(float left) { return new Rectangle(left + 500, 280, 24, 24); }

    void draw(SpriteBatch b, BitmapFont f, float left, Player player, int heals, int powers,
              Texture healIcon, Texture powerIcon, int hovered, boolean pressed, boolean closeHover,
              String message, boolean success) {
        ui.rect(b, left, 0, 640, 360, UiTheme.SURFACE, .64f);
        ui.panel(b, left + 100, 60, 440, 254);
        ui.text(b, f, "여행자의 가방", left + 118, 303, 1.05f, UiTheme.TEXT, 350);
        ui.text(b, f, "일시정지 | 돌진: "+GameBalance.SKILL_CHARGE_LABEL+" 집중 / 20 피해 · 빈틈 30", left + 118, 277, .6f, UiTheme.MUTED, 360);
        ui.button(b, f, closeBounds(left), "X", true, closeHover, pressed, .8f);
        ui.ornament(b,left+320,259,390);
        for (int row = 0; row < 2; row++) {
            int count = row == 0 ? heals : powers;
            float y = 178 - row * 78;
            boolean full = row == 0 && player.getHealth() >= player.getMaxHealth();
            boolean enabled = player.isAlive() && count > 0 && !full;
            ui.line(b,left+118,y,left+522,y,.5f,UiTheme.BORDER,.6f);
            ui.diamond(b,left+142,y+38,22,enabled?UiTheme.GOLD:UiTheme.BORDER,.4f);
            Texture icon = row == 0 ? healIcon : powerIcon;
            if (icon != null) {
                float packed = b.getPackedColor();
                b.setColor(1, 1, 1, count > 0 ? 1 : .35f);
                b.draw(icon, left + 128, y + 23, 30, 30); b.setPackedColor(packed);
            }
            ui.text(b, f, row == 0 ? "회복 포션" : "공격력 포션", left + 170, y + 53, .9f, UiTheme.TEXT, 250);
            ui.text(b, f, row == 0 ? "회복량 " + GameBalance.healing(player.getMaxHealth()) + " (최대 체력의 50%)"
                    : "+" + GameBalance.POWER_BONUS + " / 지속 시간 " + (int)GameBalance.POWER_DURATION + "초",
                    left + 170, y + 35, .62f, UiTheme.MUTED, 260);
            String owned = "보유 " + count;
            if (row == 1 && player.getAttackPotionTimer() > 0)
                owned += " / 효과 중 " + (int)Math.ceil(player.getAttackPotionTimer()) + "초";
            ui.text(b, f, owned, left + 170, y + 17, .58f, count > 0 ? UiTheme.GOLD : UiTheme.MUTED, 260);
            String label = count == 0 ? "없음" : full ? "체력 최대" : row == 0 ? "사용 [" + KeyBindings.name(KeyBindings.Action.HEAL) + "]"
                    : player.getAttackPotionTimer() > 0 ? "갱신 [" + KeyBindings.name(KeyBindings.Action.POWER) + "]" : "사용 [" + KeyBindings.name(KeyBindings.Action.POWER) + "]";
            ui.button(b, f, useBounds(left, row), label, enabled, hovered == row, pressed, .7f);
        }
        ui.text(b, f, message.isEmpty() ? "포션 단축키로 사용 / 인벤토리 키 또는 Esc로 닫기" : message,
                left + 118, 82, .64f, message.isEmpty() ? UiTheme.MUTED
                        : success ? UiTheme.SUCCESS : UiTheme.GOLD, 404);
    }
}
