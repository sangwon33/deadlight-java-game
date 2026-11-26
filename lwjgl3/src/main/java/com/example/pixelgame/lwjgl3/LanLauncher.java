package com.example.pixelgame.lwjgl3;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.swing.*;

/** Participant connection dialog; HTTP checks run off the Swing event thread. */
public final class LanLauncher {
    private LanLauncher() {}
    private record Connection(String url,boolean offline) {}

    public static void main(String[] args) throws Exception {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { }
        Preferences prefs=Preferences.userRoot().node("com/example/pixelgame/hosted");
        AtomicReference<Connection> selected=new AtomicReference<>();
        CountDownLatch finished=new CountDownLatch(1);
        SwingUtilities.invokeAndWait(()->showDialog(prefs,selected,finished));
        finished.await();
        Connection connection=selected.get();
        if(connection==null)return;
        System.setProperty("deadlight.serverUrl",connection.url());
        System.setProperty("deadlight.offline",Boolean.toString(connection.offline()));
        Lwjgl3Launcher.main(args);
    }

    private static void showDialog(Preferences prefs,AtomicReference<Connection> selected,CountDownLatch finished) {
        JFrame dialog=new JFrame("Deadlight · 여정에 합류하기");
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        String savedAddress=prefs.get("serverAddress","");
        // Old host/LAN defaults should not appear in the participant's invitation field.
        if(!savedAddress.regionMatches(true,0,"https://",0,8))savedAddress="";
        ServerConnectionView panel=new ServerConnectionView(savedAddress);
        JTextField address=panel.address;
        JButton connect=panel.connect,offline=panel.offline,cancel=panel.cancel;
        AtomicReference<SwingWorker<Void,Void>> pending=new AtomicReference<>();
        Runnable close=()->{
            SwingWorker<Void,Void> worker=pending.get(); if(worker!=null)worker.cancel(true);
            dialog.dispose();
        };
        cancel.addActionListener(e->close.run());
        dialog.addWindowListener(new WindowAdapter(){
            @Override public void windowClosing(WindowEvent e){close.run();}
            @Override public void windowClosed(WindowEvent e){finished.countDown();}
        });
        offline.addActionListener(e->{
            String url;
            try { url=ServerAddress.normalize(address.getText()); }
            catch(IllegalArgumentException ignored){url="http://127.0.0.1:8080";}
            selected.set(new Connection(url,true));close.run();
        });
        connect.addActionListener(e->{
            final String url;
            try { url=ServerAddress.normalize(address.getText()); }
            catch(IllegalArgumentException invalid){panel.showError(invalid.getMessage());address.requestFocusInWindow();return;}
            address.setText(url);panel.setBusy(true);
            SwingWorker<Void,Void> worker=new SwingWorker<>() {
                @Override protected Void doInBackground() throws Exception {ServerAddress.check(url);return null;}
                @Override protected void done() {
                    if(!dialog.isDisplayable()||isCancelled())return;
                    try {
                        get();
                        try {prefs.put("serverAddress",url);prefs.flush();}catch(Exception ignored){}
                        selected.set(new Connection(url,false));dialog.dispose();
                    } catch(InterruptedException interrupted) {Thread.currentThread().interrupt();}
                    catch(ExecutionException failed) {
                        Throwable cause=failed.getCause();
                        String message=cause instanceof java.net.http.HttpTimeoutException?"응답이 늦어지고 있습니다. 잠시 후 다시 연결해 주세요."
                                :cause instanceof java.net.ConnectException?"서버에 접속할 수 없습니다. 주소를 확인하거나 서버 담당자에게 문의해 주세요."
                                :cause instanceof java.io.IOException?cause.getMessage():"서버 연결에 실패했습니다.";
                        if(message==null||message.isBlank())message="서버 주소를 확인하거나 서버 담당자에게 문의해 주세요.";
                        panel.setBusy(false);panel.showError(message);address.requestFocusInWindow();
                    }
                }
            };
            pending.set(worker);worker.execute();
        });
        dialog.setContentPane(panel);dialog.getRootPane().setDefaultButton(connect);
        dialog.getRootPane().registerKeyboardAction(e->close.run(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();dialog.setResizable(false);dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        address.requestFocusInWindow();
    }
}
