package io.github.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.tetris_battle.*;
public class RoomScreen extends ScreenAdapter implements HandleMessageScreen{

    private Main main;

    private Stage stage;
    private Skin skin;
    private Label roomIdLabel;
    private Table joinRequestsTable; // Table to hold all the join requests with their accept buttons
    private Label statusLabel;

    private String roomId;

    public RoomScreen(Main main, String roomId) {
        this.main = main;
        this.roomId = roomId;  // Store the room ID passed to this screen
    }

    @Override
    public void show() {
        // Ensure that the Stage is created on the correct thread
        Gdx.app.postRunnable(() -> {
            stage = new Stage(new ScreenViewport());
            skin = new Skin(Gdx.files.internal("assets\\uiskin.json")); // Make sure you have a uiskin!

            Gdx.input.setInputProcessor(stage);

            Table table = new Table();
            table.setFillParent(true);
            table.center();
            TextButton startBtn = new TextButton("Start Game", skin);

            roomIdLabel = new Label("Room ID: " + roomId, skin);  // Display the room ID
            statusLabel = new Label("", skin);
            joinRequestsTable = new Table();  // Table to hold the join requests and their accept buttons

            table.add(roomIdLabel).colspan(2).padBottom(20).row();
            table.add(new Label("Pending Join Requests", skin)).colspan(2).padBottom(10).row();
            table.add(joinRequestsTable).colspan(2).width(300).height(200).pad(10).row();
            table.add(startBtn).colspan(2).pad(10).row();
            table.add(statusLabel).colspan(2).padTop(20);

            startBtn.addListener(e -> {
                if (startBtn.isPressed() && !startBtn.isDisabled()) {
                    startBtn.setDisabled(true);
                    Main.client.send("start");
                }
                return true;
            });

            stage.addActor(table);
        });
    }

    // Method to add a join request to the table with an accept button
    public void addJoinRequest(String request) {
        Gdx.app.postRunnable(() -> {
            // Create a new row for the request and its "Accept" button
            Table requestRow = new Table();
            String[] parts = request.split(":");
            Label requestLabel = new Label(parts[0], skin);
            TextButton acceptBtn = new TextButton("Accept", skin);

            // Add listener for the accept button
            acceptBtn.addListener(e -> {
                if (acceptBtn.isPressed() && !acceptBtn.isDisabled()) {
                    acceptBtn.setDisabled(true);
                    Main.client.send("accept:" + parts[1]);
                    statusLabel.setText("Accepted request: " + parts[0]);

                    // Remove the request from the table
                    joinRequestsTable.removeActor(requestRow);
                }
                return true;
            });

            // Add the request text and accept button to the row
            requestRow.add(requestLabel).padRight(10);
            requestRow.add(acceptBtn).padLeft(10);

            // Add the row to the table
            joinRequestsTable.add(requestRow).fillX().padBottom(10).row();
        });
    }

    @Override
    public void render(float delta) {
        if(stage == null) return;
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
        if (msg.startsWith("join_request:")) {
            String joinId = msg.substring("join_request:".length());
            addJoinRequest(joinId);
        } else if (msg.startsWith("game_start:")) {
            Gdx.app.postRunnable(() -> {
                main.setScreen(new MultiPlayerGameScreen(main, new HealthBar(), roomId));
            });
        }
    }
}
