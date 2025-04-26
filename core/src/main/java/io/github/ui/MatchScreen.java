package io.github.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.UUID;

import io.github.tetris_battle.*;

public class MatchScreen extends ScreenAdapter implements HandleMessageScreen {

    private Main main;

    private Stage stage;
    private Skin skin;

    private Table table;
    private TextField nameField;
    private TextField roomIdField;
    private Label statusLabel;

    private TextButton createBtn;
    private TextButton joinBtn;
    private TextButton autoBtn;
    private TextButton singlePlayerBtn;

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

        skin = new Skin(Gdx.files.internal("assets\\uiskin.json"));

        table = new Table();
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label("Tetris Battle - Matchmaking", skin);

        nameField = new TextField(generateRandomName(), skin);
        nameField.setMessageText("Enter Your Name");

        roomIdField = new TextField("", skin);
        roomIdField.setMessageText("Enter Room ID");

        createBtn = new TextButton("Create Room", skin);
        joinBtn = new TextButton("Join by ID", skin);
        autoBtn = new TextButton("Free Join", skin);
        singlePlayerBtn = new TextButton("Start Single Mode", skin);
        statusLabel = new Label("", skin);

        table.add(titleLabel).colspan(2).padBottom(20).row();
        table.add(nameField).colspan(2).width(200).pad(10).row();
        table.add(roomIdField).width(200).pad(10);
        table.add(joinBtn).pad(10).row();
        table.add(createBtn).colspan(2).pad(10).row();
        table.add(autoBtn).colspan(2).pad(10).row();
        table.add(singlePlayerBtn).colspan(2).pad(10).row();
        table.add(statusLabel).colspan(2).padTop(20);

        stage.addActor(table);

        createBtn.addListener(e -> {
            if (createBtn.isPressed() && !createBtn.isDisabled()) {
                createBtn.setDisabled(true);
                Main.client.send("create:" + nameField.getText());
                statusLabel.setText("Creating room...");
            }
            return true;
        });

        joinBtn.addListener(e -> {
            if (joinBtn.isPressed() && !joinBtn.isDisabled()) {
                joinBtn.setDisabled(true);
                Main.client.send("join:" + roomIdField.getText() + ":" + nameField.getText());
                statusLabel.setText("Requesting to join...");
            }
            return true;
        });

        autoBtn.addListener(e -> {
            if (autoBtn.isPressed() && !autoBtn.isDisabled()) {
                autoBtn.setDisabled(true);
                Main.client.send("auto:" + nameField.getText());
                statusLabel.setText("Searching for room...");
            }
            return true;
        });

        singlePlayerBtn.addListener(e -> {
            if (singlePlayerBtn.isPressed() && !singlePlayerBtn.isDisabled()) {
                singlePlayerBtn.setDisabled(true);
                main.setScreen(new GameScreen(main, new TetrominoSpawner(), new HealthBar()));
            }
            return true;
        });

        disableAllButtons();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void HandleMessage(String msg) {
        System.out.println(msg);
        if (msg.startsWith("approved:")) {
            String roomId = msg.split(":")[1];
            main.setScreen(new RoomScreen(main, roomId));
        } else if (msg.startsWith("room_created:")) {
            String roomId = msg.split(":")[1];
            main.setScreen(new RoomScreen(main, roomId));
        }  else if (msg.equals("no_conn")) {
            setStatusLabel("Connection lost. Trying to reconnect...");
            disableAllButtons();

        } else if (msg.equals("conn")) {
            enableAllButtons();

        } else {
            setStatusLabel(msg);
        }
    }

    private void disableAllButtons() {
        createBtn.setDisabled(true);
        joinBtn.setDisabled(true);
        autoBtn.setDisabled(true);
    }

    private void enableAllButtons() {
        createBtn.setDisabled(false);
        joinBtn.setDisabled(false);
        autoBtn.setDisabled(false);
        singlePlayerBtn.setDisabled(false);
    }
}
