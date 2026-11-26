package com.example.pixelgame.lwjgl3;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Uses the game's own title illustration and typeface for the desktop entry screen. */
final class ServerConnectionView extends JPanel {
    private static final Color TEXT = new Color(247, 251, 255);
    private static final Color MUTED = new Color(193, 211, 226);
    private static final Color ACCENT = new Color(173, 214, 237);
    private static final Color BORDER = new Color(69, 97, 125);
    private static final Color ERROR = new Color(255, 151, 139);
    private final BufferedImage illustration = loadIllustration();
    private final Font typeface = loadTypeface();
    private final JLabel stateTitle;
    private final JTextArea stateDescription;
    private boolean busy;
    private boolean error;

    final JTextField address;
    final JButton connect;
    final JButton offline;
    final JButton cancel;

    ServerConnectionView(String savedAddress) {
        setLayout(null);
        setPreferredSize(new Dimension(960, 600));
        setBackground(new Color(6, 11, 17));

        JLabel brand = label("DEADLIGHT", 21, ACCENT);
        brand.setFont(brand.getFont().deriveFont(Map.of(TextAttribute.TRACKING, .28f)));
        place(brand, 64, 57, 460, 30);
        place(label("여정에 합류하기", 34, TEXT), 62, 102, 472, 49);

        place(label("서버 주소", 16, TEXT), 64, 225, 472, 24);
        address = new AddressField(savedAddress);
        address.setFont(typeface.deriveFont(16f));
        address.setForeground(TEXT);
        address.setCaretColor(ACCENT);
        address.setSelectionColor(new Color(49, 81, 107));
        address.setSelectedTextColor(Color.WHITE);
        address.setBackground(new Color(12, 22, 33));
        address.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        address.getAccessibleContext().setAccessibleName("서버 주소");
        address.getAccessibleContext().setAccessibleDescription("서버 담당자에게 받은 HTTPS 주소를 입력하세요.");
        address.setToolTipText("서버 담당자에게 받은 https:// 주소를 붙여 넣으세요.");
        place(address, 64, 259, 480, 50);
        place(label("전달받은 주소를 붙여 넣어 주세요.  Ctrl + V", 13, MUTED), 64, 316, 480, 25);

        stateTitle = label("연결할 준비가 되었나요?", 16, ACCENT);
        place(stateTitle, 91, 369, 430, 25);
        stateDescription = new JTextArea();
        stateDescription.setEditable(false);
        stateDescription.setFocusable(false);
        stateDescription.setLineWrap(true);
        stateDescription.setWrapStyleWord(true);
        stateDescription.setOpaque(false);
        stateDescription.setBorder(BorderFactory.createEmptyBorder());
        stateDescription.setFont(typeface.deriveFont(14f));
        stateDescription.setForeground(MUTED);
        stateDescription.getAccessibleContext().setAccessibleName("연결 상태");
        place(stateDescription, 91, 401, 431, 43);

        connect = new MenuButton("연결 후 실행", true);
        offline = new MenuButton("오프라인 실행", false);
        cancel = new MenuButton("닫기", false);
        place(connect, 64, 467, 232, 49);
        place(offline, 312, 467, 232, 49);
        place(cancel, 467, 537, 77, 29);
        place(label("Enter 연결  ·  Esc 닫기", 12, MUTED), 64, 539, 300, 25);

        showReady();
    }

    void showReady() {
        setStatus("연결할 준비가 되었나요?", "서버를 확인한 뒤 게임을 시작합니다.", false);
    }

    void showError(String message) {
        setStatus("연결을 확인해 주세요", message, true);
    }

    void setBusy(boolean checking) {
        busy = checking;
        address.setEnabled(!checking);
        connect.setEnabled(!checking);
        connect.setText(checking ? "연결 확인 중…" : "연결 후 실행");
        if (checking) setStatus("기록의 문을 여는 중…", "서버에 연결하고 있습니다. 잠시만 기다려 주세요.", false);
        repaint();
    }

    private void setStatus(String title, String description, boolean failed) {
        error = failed;
        stateTitle.setText(title);
        stateTitle.setForeground(failed ? ERROR : ACCENT);
        stateDescription.setText(description);
        stateDescription.getAccessibleContext().setAccessibleDescription(title + ". " + description);
        repaint();
    }

    private JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(typeface.deriveFont((float) size));
        label.setForeground(color);
        return label;
    }

    private void place(Component component, int x, int y, int width, int height) {
        component.setBounds(x, y, width, height);
        add(component);
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        smooth(g);
        if (illustration != null) {
            double scale = Math.max((double) getWidth() / illustration.getWidth(), (double) getHeight() / illustration.getHeight());
            int w = (int) Math.ceil(illustration.getWidth() * scale);
            int h = (int) Math.ceil(illustration.getHeight() * scale);
            g.drawImage(illustration, getWidth() - w, (getHeight() - h) / 2, w, h, null);
        }
        g.setPaint(new GradientPaint(0, 0, new Color(3, 8, 14, 180), 960, 0, new Color(3, 8, 14, 12)));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(5, 11, 18, 235));
        g.fillRect(28, 28, 552, 544);
        g.setColor(BORDER);
        g.drawRect(28, 28, 552, 544);
        diamond(g, 28, 28, 3);
        diamond(g, 580, 28, 3);
        diamond(g, 28, 572, 3);
        diamond(g, 580, 572, 3);
        g.drawLine(64, 207, 289, 207);
        g.drawLine(319, 207, 544, 207);
        g.setColor(ACCENT);
        diamond(g, 304, 207, 6);

        g.setColor(new Color(18, 33, 46, 185));
        g.fillRect(64, 358, 480, 89);
        g.setColor(error ? ERROR : ACCENT);
        if (busy) {
            g.drawOval(74, 379, 8, 8);
            g.drawLine(78, 381, 78, 383);
            g.drawLine(78, 383, 80, 383);
        } else if (error) {
            g.drawOval(74, 379, 8, 8);
            g.drawLine(78, 381, 78, 383);
            g.fillRect(78, 385, 1, 1);
        } else diamond(g, 78, 383, 4);
        g.dispose();
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    private static void diamond(Graphics2D g, int x, int y, int radius) {
        g.drawPolygon(new int[]{x, x + radius, x, x - radius}, new int[]{y - radius, y, y + radius, y}, 4);
    }

    private static BufferedImage loadIllustration() {
        try (InputStream stream = ServerConnectionView.class.getResourceAsStream("/art/title.png")) {
            return stream == null ? null : ImageIO.read(stream);
        } catch (Exception ignored) { return null; }
    }

    private static Font loadTypeface() {
        try (InputStream stream = ServerConnectionView.class.getResourceAsStream("/fonts/NanumGothic-Regular.ttf")) {
            if (stream != null) return Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception ignored) { }
        return new Font(Font.DIALOG, Font.PLAIN, 16);
    }

    private static final class AddressField extends JTextField {
        AddressField(String savedAddress) {
            super(savedAddress);
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty()) {
                Graphics2D g = (Graphics2D) graphics.create();
                smooth(g);
                g.setColor(new Color(155, 176, 194));
                g.setFont(getFont());
                g.drawString("https://…trycloudflare.com", 16, (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent());
                g.dispose();
            }
        }

        @Override protected void paintBorder(Graphics graphics) {
            graphics.setColor(hasFocus() ? ACCENT : BORDER);
            graphics.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            if (hasFocus()) {
                graphics.setColor(new Color(173, 214, 237, 65));
                graphics.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            }
        }
    }

    private final class MenuButton extends JButton {
        private final boolean primary;

        MenuButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(typeface.deriveFont(16f));
            setForeground(TEXT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            smooth(g);
            boolean hover = getModel().isRollover() || hasFocus();
            boolean pressed = getModel().isPressed();
            boolean small = getHeight() < 40;
            g.setColor(pressed ? new Color(20, 37, 53) : hover ? new Color(30, 52, 72)
                    : primary ? new Color(26, 45, 61) : new Color(9, 18, 28, small ? 0 : 220));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(isEnabled() ? hover || primary ? ACCENT : BORDER : BORDER);
            if (!small || hover) g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            if (primary || hasFocus()) {
                diamond(g, 19, getHeight() / 2, 3);
                diamond(g, getWidth() - 20, getHeight() / 2, 3);
            }
            if (hasFocus()) g.drawRect(3, 3, getWidth() - 7, getHeight() - 7);
            g.setColor(isEnabled() ? TEXT : new Color(145, 166, 184));
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}
