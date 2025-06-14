package io.github.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.logic.tetris_battle.GameScreen;
import io.github.logic.utils.AppColors;
import io.github.logic.utils.UIFactory;
import io.github.logic.utils.AudioManager;

public class EndGameScreen extends ScreenAdapter {

    private final Main main;

    private final boolean isWinner;
    private final float duration;
    private final int score;

    private Stage stage;
    private Table table;

    public EndGameScreen(Main main, boolean isWinner, float duration) {
        this.main = main;
        this.isWinner = isWinner;
        this.duration = duration;
        this.score = -1;
    }

    public EndGameScreen(Main main, boolean isWinner, float duration, int score) {
        this.main = main;
        this.isWinner = isWinner;
        this.duration = duration;
        this.score = score;
    }

    @Override
    public void show() {
        // Stop current music and play appropriate end game music
        AudioManager.getInstance().stopMusic();
        if (isWinner) {
            AudioManager.getInstance().playVictoryMusic();
        } else {
            AudioManager.getInstance().playDefeatMusic();
        }

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setFillParent(true);
        table.center().pad(40);
        table.defaults().pad(15);

        Label resultLabel = UIFactory.createLabel(isWinner ? "YOU WIN!" : "GAME OVER");
        resultLabel.setColor(isWinner ? AppColors.WIN_TITLE : AppColors.LOSE_TITLE);
        resultLabel.setFontScale(3.0f);

        Label timeLabel = UIFactory.createLabel("Play Time: " + formatDuration(duration));
        timeLabel.setFontScale(1.5f);
        timeLabel.setColor(AppColors.SECONDARY_TEXT);

        Label scoreLabel = UIFactory.createLabel("Score: " + score);
        scoreLabel.setFontScale(1.5f);
        scoreLabel.setColor(AppColors.SECONDARY_TEXT);

        TextButton playAgainBtn = UIFactory.createTextButton("Return", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Stop current music and play menu music when returning to match screen
                AudioManager.getInstance().stopMusic();
                AudioManager.getInstance().playMenuMusic();

                main.setScreen(new MatchScreen(main));
            }
        });

        playAgainBtn.setColor(AppColors.BUTTON_BG_YELLOW);
        playAgainBtn.getLabel().setColor(AppColors.BUTTON_TEXT);

        table.add(resultLabel).center().padBottom(30).row();
        table.add(timeLabel).padBottom(30).row();
        if (score >= 0) {
            table.add(scoreLabel).padBottom(30).row();
        }
        table.add(playAgainBtn).width(300).height(50).padBottom(20).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(AppColors.BACKGROUND.r, AppColors.BACKGROUND.g, AppColors.BACKGROUND.b, AppColors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    private String formatDuration(float seconds) {
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%02d:%02d", minutes, secs);
    }
}
