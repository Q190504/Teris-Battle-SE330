package io.github.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.UUID;

import io.github.logic.tetris_battle.GameScreen;
import io.github.logic.tetris_battle.board.TetrominoSpawner;
import io.github.logic.tetris_battle.score.HealthBar;
import io.github.logic.utils.AppColors;
import io.github.logic.utils.Messages;
import io.github.logic.utils.UIFactory;

public class MatchScreen extends ScreenAdapter implements HandleMessageScreen {

    private final Main main;

    private Stage stage;
    private Table table;
    private TextField nameField;
    private TextField roomIdField;
    private Label statusLabel;

    private TextButton createBtn;
    private TextButton joinBtn;
    private TextButton singlePlayerBtn;

    private Dialog dialog;

    public MatchScreen(Main main) {
        this.main = main;
    }

    private String generateRandomName() {
        return "Player_" + UUID.randomUUID().toString().substring(0, 4);
    }

    public void setStatusLabel(String text) {
        statusLabel.setText(text);
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setFillParent(true);
        table.center().pad(40);
        table.defaults().pad(10);

        Label titleLabel = UIFactory.createTitle("TETRIS BATTLE");
        titleLabel.setColor(AppColors.TITLE);

        if (!main.getUserName().isEmpty()) {
            nameField = new TextField(main.getUserName(), UIFactory.getSkin());
        } else {
            nameField = new TextField(generateRandomName(), UIFactory.getSkin());
        }
        nameField.setMessageText("Enter Your Name");
        nameField.setColor(AppColors.TEXT_FIELD);

        // Multiplayer Panel
        Table onlinePanel = new Table(UIFactory.getSkin());
        onlinePanel.pad(20);
        onlinePanel.setColor(AppColors.PANEL_BG);
        onlinePanel.defaults().pad(8);

        Label multiplayerLabel = UIFactory.createLabel("Multiplayer");
        multiplayerLabel.setColor(AppColors.MULTIPLAYER_LABEL);
        multiplayerLabel.setFontScale(1.2f);
        onlinePanel.add(multiplayerLabel).colspan(2).center().padBottom(15).row();

        roomIdField = new TextField("", UIFactory.getSkin());
        roomIdField.setMessageText("Leave blank for free match");
        roomIdField.setColor(AppColors.TEXT_FIELD);

        joinBtn = UIFactory.createTextButton("Join a room", new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (joinBtn.isDisabled()) return;

                if (roomIdField.getText().length() > 0)
                    Main.client.send(Messages.JOIN + Messages.SEPARATOR + roomIdField.getText() + Messages.SEPARATOR + nameField.getText());
                else
                    Main.client.send(Messages.AUTO + Messages.SEPARATOR + nameField.getText());
            }
        });
        joinBtn.setColor(AppColors.BUTTON_BG_CYAN);
        joinBtn.getLabel().setColor(AppColors.BUTTON_TEXT);

        createBtn = UIFactory.createTextButton("Create Room", new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (createBtn.isDisabled()) return;

                Main.client.send(Messages.CREATE + Messages.SEPARATOR + nameField.getText());
                setStatusLabel("Creating room...");
            }
        });
        createBtn.setColor(AppColors.BUTTON_BG_MAGENTA);
        createBtn.getLabel().setColor(AppColors.BUTTON_TEXT);

        onlinePanel.add(roomIdField).width(400).height(40);
        onlinePanel.add(joinBtn).width(200).height(40).row();
        onlinePanel.add(createBtn).colspan(2).width(620).height(40).padTop(15).row();

        // Single Player Panel
        Table singlePanel = new Table(UIFactory.getSkin());
        singlePanel.pad(20);
        singlePanel.setColor(AppColors.PANEL_BG);
        singlePanel.defaults().pad(8);

        Label singleLabel = UIFactory.createLabel("Single Player");
        singleLabel.setColor(AppColors.SINGLE_LABEL);
        singleLabel.setFontScale(1.2f);

        singlePlayerBtn = UIFactory.createTextButton("Start Single Mode", new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (singlePlayerBtn.isDisabled()) return;

                main.setScreen(new SinglePlayerGameScreen(main, new TetrominoSpawner()));
            }
        });
        singlePlayerBtn.setColor(AppColors.BUTTON_BG_YELLOW);
        singlePlayerBtn.getLabel().setColor(AppColors.BUTTON_TEXT);

        singlePanel.add(singleLabel).center().row();
        singlePanel.add(singlePlayerBtn).width(300).height(40).padTop(10).row();

        // Status label
        statusLabel = UIFactory.createLabel("");
        statusLabel.setWrap(true);
        statusLabel.setColor(AppColors.TEXT_FIELD);

        // Main layout
        table.add(titleLabel).center().padBottom(40).row();
        table.add(nameField).width(300).height(40).padBottom(30).row();
        table.add(onlinePanel).padBottom(30).row();
        table.add(singlePanel).padBottom(30).row();
        table.add(statusLabel).width(400).height(40);

        stage.addActor(table);
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
        stage.dispose();
    }

    @Override
    public void HandleMessage(String msg) {
        System.out.println(msg);
        String[] parts = msg.split(Messages.SEPARATOR);
        if (parts[0].equals(Messages.APPROVED)) {
            String roomId = parts[1];
            String ownerName = parts[2];
            main.setUserName(nameField.getText());
            main.setScreen(new PrepareScreen(main, roomId, false, ownerName, nameField.getText()));
        } else if (parts[0].equals(Messages.ROOM_CREATED)) {
            String roomId = parts[1];
            main.setUserName(nameField.getText());
            main.setScreen(new RoomScreen(main, roomId, true, nameField.getText()));
        } else if (parts[0].equals(Messages.NO_CONN)) {
            setStatusLabel("Connection lost. Trying to reconnect...");
            disableAllButtons();
        } else if (parts[0].equals(Messages.CONN)) {
            setStatusLabel("");
            enableAllButtons();
        } else if (parts[0].equals(Messages.ROOM_FULL)) {
            showPopup("Room is full");
            enableAllButtons();
        } else if (parts[0].equals(Messages.REJECTED)) {
            showPopup("Your request is rejected, please find another room.");
            enableAllButtons();
        } else if (parts[0].equals(Messages.ROOM_CLOSED)) {
            showPopup("Room closed by owner, please find another room.");
            enableAllButtons();
        } else if (parts[0].equals(Messages.JOIN_REQUEST)) {
            String roomId = parts[1];
            showPopup("Requesting to join room " + roomId, "LEAVE", new Runnable() {
                @Override
                public void run() {
                    Main.client.send(Messages.LEAVE);
                    enableAllButtons();
                }
            });
            disableAllButtons();
        } else if (parts[0].equals(Messages.ROOM_NOT_FOUND)) {
            showPopup("Room not found");
            enableAllButtons();
        }
    }

    private void disableAllButtons() {
        createBtn.setDisabled(true);
        joinBtn.setDisabled(true);
        //singlePlayerBtn.setDisabled(true);
    }

    private void enableAllButtons() {
        createBtn.setDisabled(false);
        joinBtn.setDisabled(false);
        singlePlayerBtn.setDisabled(false);
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
