package io.github.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.tetris_battle.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrepareScreen extends ScreenAdapter implements HandleMessageScreen {

    private final Main main;
    private final boolean isOwner;
    private boolean isReady = false;

    private final String roomId;

    private Stage stage;
    private Label player1Label, player2Label, statusLabel;
    private TextButton startButton, readyButton, leaveButton;
    private TextButton skillBtn1, skillBtn2, skillBtn3;

    private Set<String> selectedSkills = new HashSet<>();
    private static final int MAX_SKILLS = 2;

    private String player1Name, player2Name;
    private Dialog dialog;

    public PrepareScreen(Main main, String roomId, boolean isOwner, String player1Name, String player2Name) {
        this.main = main;
        this.roomId = roomId;
        this.isOwner = isOwner;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
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

        // Player names
        player1Label = UIFactory.createLabel("Player 1: " + player1Name);
        player1Label.setColor(AppColors.TITLE);
        player1Label.setFontScale(1.2f);

        player2Label = UIFactory.createLabel("Player 2: " + player2Name);
        player2Label.setColor(AppColors.TITLE);
        player2Label.setFontScale(1.2f);

        // Status label
        statusLabel = UIFactory.createLabel("");
        statusLabel.setColor(AppColors.TEXT_FIELD);
        statusLabel.setWrap(true);

        // Skill buttons
        skillBtn1 = createSkillButton(LockOpponentSkill.getStaticName());
        skillBtn2 = createSkillButton(ExtraPointsSkill.getStaticName());
        skillBtn3 = createSkillButton("Skill C");

        // Buttons container for skills
        Table skillsTable = new Table();
        skillsTable.defaults().pad(10);
        skillsTable.add(skillBtn1).width(120).height(40);
        skillsTable.add(skillBtn2).width(120).height(40);
        skillsTable.add(skillBtn3).width(120).height(40);

        // Start button (owner only)
        startButton = UIFactory.createTextButton("Start Game", new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (isReady) {
                    if (selectedSkills.size() < 2)
                    {
                        statusLabel.setText("Not enough skills, get more skill.");
                        return;
                    }
                    Main.client.send(Messages.START);
                    statusLabel.setText("Game starting...");
                } else {
                    statusLabel.setText("Wait for your opponent to ready.");
                }
            }
        });
        startButton.setColor(AppColors.BUTTON_BG);
        startButton.getLabel().setColor(AppColors.BUTTON_TEXT);
        startButton.getLabel().setFontScale(1f);
        startButton.setSize(200, 45);
        startButton.setDisabled(true);

        // Ready button (joiner only)
        readyButton = UIFactory.createTextButton("Ready", new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                isReady = !isReady;
                if (isReady) {
                    if (selectedSkills.size() < 2)
                    {
                        statusLabel.setText("Not enough skills, get more skill.");
                        isReady = false;
                        return;
                    }
                    Main.client.send(Messages.READY);
                    readyButton.setText("Unready");
                    statusLabel.setText("Ready! Waiting for other player...");
                } else {
                    Main.client.send(Messages.UNREADY);
                    readyButton.setText("Ready");
                    statusLabel.setText("");
                }
            }
        });

        readyButton.setColor(AppColors.BUTTON_BG);
        readyButton.getLabel().setColor(AppColors.BUTTON_TEXT);
        readyButton.getLabel().setFontScale(1f);
        readyButton.setSize(200, 45);
        readyButton.setDisabled(true);

        // Leave button (both)
        leaveButton = UIFactory.createTextButton("Leave Room", new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                Main.client.send(Messages.LEAVE);
                main.setScreen(new MatchScreen(main));
            }
        });
        leaveButton.setColor(AppColors.BUTTON_BG);
        leaveButton.getLabel().setColor(AppColors.BUTTON_TEXT);
        leaveButton.getLabel().setFontScale(1f);
        leaveButton.setSize(200, 45);

        // Compose layout
        root.add(player1Label).colspan(3).center().row();
        root.add(player2Label).colspan(3).center().row();

        root.add(new Label("Select 2 Skills:", UIFactory.getSkin())).colspan(3).center().padTop(20).row();
        root.add(skillsTable).colspan(3).center().row();

        // Buttons: start/ready + leave
        if (isOwner) {
            root.add(startButton).width(200).height(45).padTop(30).padRight(20);
            root.add(leaveButton).width(200).height(45).padTop(30).colspan(2).left();
        } else {
            root.add(readyButton).width(200).height(45).padTop(30).padRight(20);
            root.add(leaveButton).width(200).height(45).padTop(30).colspan(2).left();
        }

        root.row();
        root.add(statusLabel).colspan(3).width(400).padTop(30);

        stage.addActor(root);
        });
    }

    private TextButton createSkillButton(String skillName) {
        final TextButton[] btnHolder = new TextButton[1]; // mutable container

        TextButton btn = UIFactory.createTextButton(skillName, new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                TextButton btn = btnHolder[0];
                if (isReady) {
                    statusLabel.setText("You've locked your skills, unready for choosing skills.");
                    return;
                }

                if (selectedSkills.contains(skillName)) {
                    selectedSkills.remove(skillName);
                    btn.setColor(AppColors.BUTTON_BG);
                } else {
                    if (selectedSkills.size() >= MAX_SKILLS) {
                        statusLabel.setText("You can only select 2 skills.");
                        return;
                    }
                    selectedSkills.add(skillName);
                    btn.setColor(AppColors.BUTTON_TEXT);
                }
                updateActionButtons();
            }
        });

        btn.setColor(AppColors.BUTTON_BG);
        btn.getLabel().setColor(AppColors.BUTTON_TEXT);
        btn.getLabel().setFontScale(1f);

        btnHolder[0] = btn;
        return btn;
    }


    private void updateActionButtons() {
        boolean enabled = selectedSkills.size() == MAX_SKILLS;
        if (isOwner) {
            startButton.setDisabled(!enabled);
        } else {
            readyButton.setDisabled(!enabled);
        }
        if (enabled) {
            statusLabel.setText("You have selected 2 skills. You can " + (isOwner ? "start" : "ready") + " now.");
        } else {
            statusLabel.setText("Select exactly 2 skills to proceed.");
        }
    }

    @Override
    public void render(float delta) {
        if (stage == null) return;
        Gdx.gl.glClearColor(0, 0, 0, 1);
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
        System.out.println(msg);
        String[] parts = msg.split(Messages.SEPARATOR);
        if (parts[0].equals(Messages.READY)) {
            this.isReady = true;
        } else if (parts[0].equals(Messages.UNREADY)) {
            this.isReady = false;
        } else if (parts[0].equals(Messages.GAME_START)) {
            Gdx.app.postRunnable(() -> {
                main.setScreen(new MultiPlayerGameScreen(main, new HealthBar(), roomId, isOwner, selectedSkills));
            });
        } else if (parts[0].equals(Messages.PLAYER_LEFT)) {
            Main.client.send(Messages.LEAVE);
            showPopup("Opponent has left the game!", "LEAVE", () -> main.setScreen(new MatchScreen(main)));
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
