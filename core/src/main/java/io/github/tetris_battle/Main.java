package io.github.tetris_battle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import io.github.network.client.ClientConnection;
import io.github.network.server.NetworkManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Game {
    public static ClientConnection client;
    public static MatchScreen matchScreen;

    private Timer reconnectTimer;

    @Override
    public void create() {
        connectToServer();

        // Periodically check connection every 3 seconds
        reconnectTimer = new Timer();
        reconnectTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (client == null) {
                    System.out.println("Disconnected. Attempting to reconnect...");
                    reconnect();
                }
            }
        }, 3000, 3000);

        matchScreen = new MatchScreen(this);
        setScreen(matchScreen);
    }

    public void connectToServer() {
        try {
            client = new ClientConnection("localhost", 5000, message -> {
                System.out.println("From server: " + message);
                if (screen instanceof HandleMessageScreen) {
                    ((HandleMessageScreen) screen).HandleMessage(message);
                }
            });
            client.start();

            if (screen instanceof HandleMessageScreen) {
                ((HandleMessageScreen) screen).HandleMessage("conn");
            }

        } catch (IOException e) {
            client = null;
            if (screen instanceof HandleMessageScreen) {
                ((HandleMessageScreen) screen).HandleMessage("no_conn");
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
