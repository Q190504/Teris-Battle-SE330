package io.github.client.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import io.github.client.ClientConnection;
import io.github.logic.utils.AudioManager;
import io.github.logic.utils.Messages;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Game {
    public static ClientConnection client;
    public static MatchScreen matchScreen;

    private Timer reconnectTimer;
    private long lastPongTime = 0;
    private static final long TIMEOUT_MS = 10000; // 6 seconds timeout

    private String userName = "";

    public String getUserName () {return userName;}
    public void setUserName(String userName) {this.userName = userName;}

    @Override
    public void create() {
        // Preload tất cả audio assets
        AudioManager.getInstance().preloadAllAudio();
        
        matchScreen = new MatchScreen(this);
        setScreen(matchScreen);

        connectToServer();

        reconnectTimer = new Timer();
        reconnectTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (client == null) {
                    System.out.println("Disconnected. Attempting to reconnect...");
                    reconnect();
                } else {
                    long now = System.currentTimeMillis();
                    if (now - lastPongTime > TIMEOUT_MS) {
                        System.out.println("Server unresponsive. Attempting to reconnect...");
                        client.close();
                        client = null;
                        reconnect();
                    } else {
                        client.send("ping");
                    }
                }
            }
        }, 3000, 3000);

        //Menu music
        AudioManager.getInstance().playMusic("menu_bg", true);
    }

    public void connectToServer() {
        try {
            client = new ClientConnection("localhost", 5000, message -> {
                System.out.println("From server: " + message);
                if (message.equals("pong")) {
                    lastPongTime = System.currentTimeMillis(); // Update last pong time
                    return;
                }

                if (screen instanceof HandleMessageScreen) {
                    ((HandleMessageScreen) screen).HandleMessage(message);
                }
            });
            client.start();

            // Immediately ping server
            client.send("ping");
            lastPongTime = System.currentTimeMillis();

            if (screen instanceof HandleMessageScreen) {
                ((HandleMessageScreen) screen).HandleMessage(Messages.CONN);
            }
            System.out.println("Connected to host.");

        } catch (IOException e) {
            client = null;
            if (screen instanceof HandleMessageScreen) {
                ((HandleMessageScreen) screen).HandleMessage(Messages.NO_CONN);
            }
            System.out.println("Failed to connect: " + e.getMessage());
        }
    }

    public void reconnect() {
        connectToServer();
    }

    public void SetScreen(Screen screen) {
        setScreen(screen);
    }

    @Override
    public void dispose() {
        reconnectTimer.cancel();
        if (client != null) client.close();
        super.dispose();
    }

    
}
