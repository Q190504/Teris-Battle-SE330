package io.github.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;
import java.util.Map;

import io.github.tetris_battle.*;

public class RoomScreen extends ScreenAdapter implements HandleMessageScreen {

    private final Main main;
    private final boolean isOwner;
    private final String roomId;
    private final String ownerName;

    private Stage stage;
    private Label roomIdLabel;
    private Table joinRequestsTable;
    private Label statusLabel;
    private Dialog dialog;

    private Map<String, Table> requestRows = new HashMap<>();

    public RoomScreen(Main main, String roomId, boolean isOwner, String ownerName) {
        this.main = main;
        this.roomId = roomId;
        this.isOwner = isOwner;
        this.ownerName = ownerName;
    }

    @Override
    public void show() {
        Gdx.app.postRunnable(() -> {
            stage = new Stage(new ScreenViewport());
            Gdx.input.setInputProcessor(stage);

            Table root = new Table();
            root.setFillParent(true);
            root.center().pad(30);
            root.defaults().pad(10);

            // Room ID label - bigger font and color
            roomIdLabel = UIFactory.createLabel("Room ID: " + roomId);
            roomIdLabel.setColor(AppColors.TITLE);
            roomIdLabel.setFontScale(1.5f);

            // Status label with wrapping and centered
            statusLabel = UIFactory.createLabel("");
            statusLabel.setWrap(true);
            statusLabel.setColor(AppColors.TEXT_FIELD);

            // Join requests table inside a scroll pane
            joinRequestsTable = new Table();
            joinRequestsTable.top().defaults().pad(5).fillX();

            ScrollPane scrollPane = new ScrollPane(joinRequestsTable, UIFactory.getSkin());
            scrollPane.setFadeScrollBars(false);
            scrollPane.setScrollingDisabled(false, false);
            scrollPane.setForceScroll(false, true);
            scrollPane.setOverscroll(false, false);
            scrollPane.setSmoothScrolling(true);

            TextButton leaveBtn = UIFactory.createTextButton("Leave Room", new ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    Main.client.send(Messages.LEAVE);
                    main.setScreen(new MatchScreen(main));
                }
            });
            leaveBtn.setColor(AppColors.BUTTON_BG);
            leaveBtn.getLabel().setColor(AppColors.BUTTON_TEXT);
            leaveBtn.getLabel().setFontScale(1f);
            leaveBtn.setSize(200, 45);

            // Section labels
            Label joinRequestsTitle = UIFactory.createLabel("Pending Join Requests");
            joinRequestsTitle.setColor(AppColors.SECONDARY_TEXT);
            joinRequestsTitle.setFontScale(1.2f);

            // Compose root table
            root.add(roomIdLabel).colspan(2).center().padBottom(25).row();

            root.add(joinRequestsTitle).colspan(2).left().padBottom(10).row();
            root.add(scrollPane).width(350).height(200).colspan(2).row();

            root.add(leaveBtn).width(200).height(45).padTop(20).row();

            root.add(statusLabel).colspan(2).width(350).padTop(30).row();

            stage.addActor(root);
        });
    }

    public void addJoinRequest(String request) {
        Gdx.app.postRunnable(() -> {
            Table requestRow = new Table();
            String[] parts = request.split(":");
            String playerName = parts[0];
            String playerId = parts[1];

            Label requestLabel = UIFactory.createLabel(playerName);
            requestLabel.setColor(AppColors.TEXT_FIELD);
            requestLabel.setFontScale(1.0f);

            TextButton acceptBtn = UIFactory.createTextButton("Accept", new ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    Main.client.send(Messages.ACCEPT + Messages.SEPARATOR + playerId);
                    statusLabel.setText("Accepted request: " + playerName);
                    joinRequestsTable.removeActor(requestRow);
                    requestRows.remove(playerId);
                    main.setScreen(new PrepareScreen(main, roomId, isOwner, ownerName, playerName));
                }
            });
            acceptBtn.setColor(AppColors.BUTTON_BG);
            acceptBtn.getLabel().setColor(AppColors.BUTTON_TEXT);
            acceptBtn.getLabel().setFontScale(0.9f);
            acceptBtn.setSize(120, 30);

            requestRow.add(requestLabel).expandX().left().padRight(15);
            requestRow.add(acceptBtn).right().width(120).height(30);
            joinRequestsTable.add(requestRow).fillX().padBottom(8).row();

            requestRows.put(request, requestRow);
        });
    }

    @Override
    public void render(float delta) {
        if (stage == null) return;
        Gdx.gl.glClearColor(AppColors.BACKGROUND.r, AppColors.BACKGROUND.g, AppColors.BACKGROUND.b, AppColors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }

    @Override
    public void HandleMessage(String msg) {
        String[] parts = msg.split(Messages.SEPARATOR);
        if (parts[0].equals(Messages.JOIN_REQUEST)) {
            String joinId = parts[1] + Messages.SEPARATOR + parts[2];
            addJoinRequest(joinId);
        } else if (parts[0].equals(Messages.ROOM_CLOSED)) {
            Gdx.app.postRunnable(() -> {
                showPopup("Room closed by owner.", () -> main.setScreen(new MatchScreen(main)));
            });
        } else if (parts[0].equals(Messages.PLAYER_LEFT)) {
            String playerId = parts[1] + Messages.SEPARATOR + parts[2];
            Gdx.app.postRunnable(() -> {
                Table row = requestRows.remove(playerId);
                if (row != null) {
                    joinRequestsTable.removeActor(row);
                    statusLabel.setText("Player " + playerId + " left the room.");
                } else {
                    statusLabel.setText("A player left the room.");
                }
            });
        } else if (parts[0].equals(Messages.NO_CONN)) {
            showPopup("Disconnected from the server.", new Runnable() {
                @Override
                public void run() {
                    main.setScreen(new MatchScreen(main));
                }
            });
        }
    }


    private void showPopup(String message) {
        showPopup(message, "OK", null);
    }

    private void showPopup(String message, String action) {
        showPopup(message, action, null);
    }

    private void showPopup(String message, Runnable onOk) {
        showPopup(message, "OK", onOk);
    }

    private void showPopup(String message, String action, Runnable onOk) {
        if (dialog != null)
            dialog.hide();
        dialog = UIFactory.createDialog("Notice", message, action, onOk);
        dialog.show(stage);
    }
}
