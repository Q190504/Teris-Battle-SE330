package io.github.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.logic.tetris_battle.score.HealthBar;
import io.github.logic.tetris_battle.skill.ExtraPointsSkill;
import io.github.logic.tetris_battle.skill.LockOpponentSkill;
import io.github.logic.tetris_battle.skill.SpeedBoostSkill;
import io.github.logic.utils.AppColors;
import io.github.logic.utils.Messages;
import io.github.logic.utils.UIFactory;

import java.util.HashSet;
import java.util.Set;

public class PrepareScreen extends ScreenAdapter implements HandleMessageScreen {

    private final Main main;
    private final boolean isOwner;
    private boolean isGuestReady = false;

    private final String roomId;

    private Stage stage;
    private Label player1Label, player2Label, statusLabel, ready1Label, ready2Label;
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

            ready1Label = UIFactory.createLabel("Not Ready");
            ready1Label.setColor(AppColors.BUTTON_TEXT);

            ready2Label = UIFactory.createLabel("Not Ready");
            ready2Label.setColor(AppColors.BUTTON_TEXT);

            // Status label
            statusLabel = UIFactory.createLabel("");
            statusLabel.setColor(AppColors.TEXT_FIELD);
            statusLabel.setWrap(true);

            // Skill buttons
            skillBtn1 = createSkillButton(LockOpponentSkill.getStaticName());
            skillBtn2 = createSkillButton(ExtraPointsSkill.getStaticName());
            skillBtn3 = createSkillButton(SpeedBoostSkill.getStaticName());

            Label instruction1 = UIFactory.createLabel(LockOpponentSkill.getStaticInstruction());
            Label instruction2 = UIFactory.createLabel(ExtraPointsSkill.getStaticInstruction());
            Label instruction3 = UIFactory.createLabel(SpeedBoostSkill.getStaticInstruction());
            instruction1.setWrap(true);
            instruction2.setWrap(true);
            instruction3.setWrap(true);

            // Buttons container for skills
            Table skill1Table = new Table();
            skill1Table.add(skillBtn1).width(230).height(40).row();
            skill1Table.add(instruction1).width(230).center().padTop(5);

            Table skill2Table = new Table();
            skill2Table.add(skillBtn2).width(230).height(40).row();
            skill2Table.add(instruction2).width(230).center().padTop(5);

            Table skill3Table = new Table();
            skill3Table.add(skillBtn3).width(230).height(40).row();
            skill3Table.add(instruction3).width(230).center().padTop(5);

            // Add to main skills table
            Table skillsTable = new Table();
            skillsTable.defaults().pad(10);
            skillsTable.add(skill1Table);
            skillsTable.add(skill2Table);
            skillsTable.add(skill3Table);

            // Start button (owner only)
            startButton = UIFactory.createTextButton("Start Game", new ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    if (isGuestReady) {
                        if (selectedSkills.size() < 2) {
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

            // Ready button (guest only)
            readyButton = UIFactory.createTextButton("Ready", new ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    isGuestReady = !isGuestReady;
                    if (isGuestReady) {
                        if (selectedSkills.size() < 2) {
                            statusLabel.setText("Not enough skills, get more skill.");
                            isGuestReady = false;
                            return;
                        }
                        Main.client.send(Messages.READY);
                        readyButton.setText("Unready");
                        statusLabel.setText("Ready! Waiting for other player...");
                        ready2Label.setText("Ready");
                    } else {
                        Main.client.send(Messages.UNREADY);
                        readyButton.setText("Ready");
                        statusLabel.setText("");
                        ready2Label.setText("Not Ready");
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
            root.add(player1Label).colspan(2).right();
            root.add(ready1Label).left();
            root.row();
            root.add(player2Label).colspan(2).right();
            root.add(ready2Label).left();
            root.row();

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
                if (isGuestReady && !isOwner) {
                    statusLabel.setText("You've locked your skills, unready for choosing skills.");
                    return;
                }

                if (selectedSkills.contains(skillName)) {
                    selectedSkills.remove(skillName);
                    if (isOwner) {
                        ready1Label.setText("Not Ready");
                        Main.client.send(Messages.UNREADY);
                    }
                    btn.setColor(AppColors.BUTTON_BG_CYAN);
                } else {
                    if (selectedSkills.size() >= MAX_SKILLS) {
                        statusLabel.setText("You can only select 2 skills.");
                        return;
                    }
                    selectedSkills.add(skillName);
                    btn.setColor(AppColors.BUTTON_BG_MAGENTA);
                }
                updateActionButtons();
            }
        });

        btn.setColor(AppColors.BUTTON_BG_CYAN);
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
            if (isOwner) {
                ready1Label.setText("Ready");
                Main.client.send(Messages.READY);
            }
            statusLabel.setText("You have selected 2 skills. " + (isOwner ? "Wait for other to ready." : "You can ready now."));
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
            if (isOwner) {
                this.isGuestReady = true;
                ready2Label.setText("Ready");
            }
            else
                ready1Label.setText("Ready");
            statusLabel.setText("");
        } else if (parts[0].equals(Messages.UNREADY)) {
            if (isOwner) {
                this.isGuestReady = false;
                ready2Label.setText("Not Ready");
            }
            else
                ready1Label.setText("Not Ready");
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
