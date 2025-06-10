package io.github.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.UUID;

import io.github.logic.tetris_battle.GameScreen;
import io.github.logic.tetris_battle.board.TetrominoSpawner;
import io.github.logic.tetris_battle.score.HealthBar;
import io.github.logic.utils.AppColors;
import io.github.logic.utils.Messages;
import io.github.logic.utils.UIFactory;
import io.github.logic.utils.AudioManager;

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
    private TextButton audioSettingsBtn;

    private Dialog dialog;
    private Dialog audioDialog;

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

        // Audio Settings Button
        audioSettingsBtn = UIFactory.createTextButton("Audio Settings", new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                showAudioSettingsPopup();
            }
        });
        audioSettingsBtn.setColor(AppColors.BUTTON_BG_CYAN);
        audioSettingsBtn.getLabel().setColor(AppColors.BUTTON_TEXT);

        // Status label
        statusLabel = UIFactory.createLabel("");
        statusLabel.setWrap(true);
        statusLabel.setColor(AppColors.TEXT_FIELD);

        // Main layout
        table.add(titleLabel).center().padBottom(40).row();
        table.add(nameField).width(300).height(40).padBottom(30).row();
        table.add(onlinePanel).padBottom(30).row();
        table.add(singlePanel).padBottom(20).row();
        table.add(audioSettingsBtn).width(300).height(40).padBottom(30).row();
        table.add(statusLabel).width(400).height(40);

        stage.addActor(table);
    }

    private void showAudioSettingsPopup() {
        if (audioDialog != null) {
            audioDialog.hide();
        }

        AudioManager audioManager = AudioManager.getInstance();
        
        audioDialog = new Dialog("Audio Settings", UIFactory.getSkin()) {
            @Override
            protected void result(Object object) {
                if ("CLOSE".equals(object)) {
                    audioManager.saveSettings();
                }
            }
        };

        // Title styling
        audioDialog.getTitleLabel().setColor(AppColors.TITLE);
        audioDialog.getTitleLabel().setFontScale(1.2f);

        Table contentTable = audioDialog.getContentTable();
        contentTable.pad(20);
        contentTable.defaults().pad(8);

        // Master Volume
        Label masterLabel = UIFactory.createLabel("MASTER VOLUME");
        masterLabel.setColor(AppColors.MULTIPLAYER_LABEL);
        masterLabel.setFontScale(0.9f);
        
        final Slider masterSlider = new Slider(0f, 1f, 0.01f, false, UIFactory.getSkin());
        masterSlider.setValue(audioManager.getMasterVolume());
        
        final Label masterValueLabel = UIFactory.createLabel(Math.round(audioManager.getMasterVolume() * 100) + "%");
        masterValueLabel.setColor(AppColors.TEXT_FIELD);
        
        final CheckBox masterMuteBox = new CheckBox(" MUTE", UIFactory.getSkin());
        masterMuteBox.setChecked(audioManager.isMasterMuted());
        masterMuteBox.getLabel().setColor(AppColors.BUTTON_TEXT);

        // Music Volume
        Label musicLabel = UIFactory.createLabel("MUSIC VOLUME");
        musicLabel.setColor(AppColors.MULTIPLAYER_LABEL);
        musicLabel.setFontScale(0.9f);
        
        final Slider musicSlider = new Slider(0f, 1f, 0.01f, false, UIFactory.getSkin());
        musicSlider.setValue(audioManager.getMusicVolume());
        
        final Label musicValueLabel = UIFactory.createLabel(Math.round(audioManager.getMusicVolume() * 100) + "%");
        musicValueLabel.setColor(AppColors.TEXT_FIELD);
        
        final CheckBox musicMuteBox = new CheckBox(" MUTE", UIFactory.getSkin());
        musicMuteBox.setChecked(audioManager.isMusicMuted());
        musicMuteBox.getLabel().setColor(AppColors.BUTTON_TEXT);

        // SFX Volume
        Label sfxLabel = UIFactory.createLabel("SFX VOLUME");
        sfxLabel.setColor(AppColors.MULTIPLAYER_LABEL);
        sfxLabel.setFontScale(0.9f);
        
        final Slider sfxSlider = new Slider(0f, 1f, 0.01f, false, UIFactory.getSkin());
        sfxSlider.setValue(audioManager.getSfxVolume());
        
        final Label sfxValueLabel = UIFactory.createLabel(Math.round(audioManager.getSfxVolume() * 100) + "%");
        sfxValueLabel.setColor(AppColors.TEXT_FIELD);
        
        final CheckBox sfxMuteBox = new CheckBox(" MUTE", UIFactory.getSkin());
        sfxMuteBox.setChecked(audioManager.isSfxMuted());
        sfxMuteBox.getLabel().setColor(AppColors.BUTTON_TEXT);

        // Add listeners for real-time updates
        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = masterSlider.getValue();
                audioManager.setMasterVolume(value);
                masterValueLabel.setText(Math.round(value * 100) + "%");
            }
        });

        masterMuteBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.setMasterMuted(masterMuteBox.isChecked());
            }
        });

        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = musicSlider.getValue();
                audioManager.setMusicVolume(value);
                musicValueLabel.setText(Math.round(value * 100) + "%");
            }
        });

        musicMuteBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.setMusicMuted(musicMuteBox.isChecked());
            }
        });

        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = sfxSlider.getValue();
                audioManager.setSfxVolume(value);
                sfxValueLabel.setText(Math.round(value * 100) + "%");
            }
        });

        sfxMuteBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.setSfxMuted(sfxMuteBox.isChecked());
            }
        });

        // Layout the controls
        contentTable.add(masterLabel).left().colspan(3).padBottom(5).row();
        contentTable.add(masterSlider).width(200).padRight(10);
        contentTable.add(masterValueLabel).width(50).padRight(10);
        contentTable.add(masterMuteBox).left().row();
        
        contentTable.add(new Label("", UIFactory.getSkin())).height(10).colspan(3).row(); // Spacer
        
        contentTable.add(musicLabel).left().colspan(3).padBottom(5).row();
        contentTable.add(musicSlider).width(200).padRight(10);
        contentTable.add(musicValueLabel).width(50).padRight(10);
        contentTable.add(musicMuteBox).left().row();
        
        contentTable.add(new Label("", UIFactory.getSkin())).height(10).colspan(3).row(); // Spacer
        
        contentTable.add(sfxLabel).left().colspan(3).padBottom(5).row();
        contentTable.add(sfxSlider).width(200).padRight(10);
        contentTable.add(sfxValueLabel).width(50).padRight(10);
        contentTable.add(sfxMuteBox).left().row();

        // Close button
        TextButton closeButton = UIFactory.createTextButton("CLOSE", null);
        closeButton.setColor(AppColors.BUTTON_BG_MAGENTA);
        closeButton.getLabel().setColor(AppColors.BUTTON_TEXT);
        
        audioDialog.button(closeButton, "CLOSE");

        // Dialog styling
        audioDialog.getButtonTable().padTop(20).padBottom(20);
        audioDialog.setModal(true);
        audioDialog.setMovable(false);
        audioDialog.setResizable(false);
        audioDialog.setColor(AppColors.PANEL_BG);

        // Pack and center
        audioDialog.pack();
        audioDialog.setPosition(
            (Gdx.graphics.getWidth() - audioDialog.getWidth()) / 2f,
            (Gdx.graphics.getHeight() - audioDialog.getHeight()) / 2f
        );

        audioDialog.show(stage);
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