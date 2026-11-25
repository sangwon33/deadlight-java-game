package com.example.pixelgame.lwjgl3;

import java.net.URI;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/** Optional Windows dialog. The game and its existing artwork remain in libGDX. */
public final class LanLauncher {
    private LanLauncher() {}
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { }
        Preferences prefs=Preferences.userNodeForPackage(LanLauncher.class);
        String address=prefs.get("serverAddress","http://127.0.0.1:8080");
        while(true) {
            String input=(String)JOptionPane.showInputDialog(null,
                    "서버를 실행한 노트북의 주소를 입력하세요.\n예: http://192.168.0.10:8080\n같은 와이파이에 연결되어 있어야 합니다.",
                    "Deadlight · 서버 연결",JOptionPane.QUESTION_MESSAGE,null,null,address);
            if(input==null)return;
            address=input.trim().replaceAll("/+$","");
            try {
                URI uri=URI.create(address);
                if (!("http".equals(uri.getScheme())||"https".equals(uri.getScheme())) || uri.getHost()==null
                        || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null
                        || !(uri.getPath().isEmpty()||uri.getPath().equals("/"))) throw new IllegalArgumentException();
                System.setProperty("deadlight.serverUrl",address);
                prefs.put("serverAddress",address);
                break;
            } catch(IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null,"http://서버IP:8080 형식으로 입력하세요.","서버 주소 확인",JOptionPane.WARNING_MESSAGE);
            }
        }
        Lwjgl3Launcher.main(args);
    }
}
