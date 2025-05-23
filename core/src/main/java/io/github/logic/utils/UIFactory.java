package io.github.logic.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class UIFactory {
    private static final Skin skin = new Skin(Gdx.files.internal("assets/quantum/quantum-horizon-ui.json"));

    public static Label createLabel(String text) {
        return new Label(text, skin);
    }

    public static Label createTitle(String text) {
        return new Label(text, skin, "title");
    }

    public static TextTooltip createTextTooltip(String text) {
        return new TextTooltip(text, skin);
    }

    public static TextButton createTextButton(String text, ClickListener listener) {
        TextButton button = new TextButton(text, skin);
        button.pad(20);

        if (listener != null) {
            button.addListener(listener);
        }
        return button;
    }

    public static Dialog createDialog(String title, String message, String action, Runnable onOk) {
        Dialog dialog = new Dialog(title, skin) {
            @Override
            protected void result(Object object) {
                if (action.equals(object) && onOk != null) {
                    onOk.run();
                }
            }
        };

        // Title styling
        dialog.getTitleLabel().setColor(AppColors.TITLE);
        dialog.getTitleLabel().setFontScale(1.2f);

        // Message styling
        Label messageLabel = new Label(message, skin);
        messageLabel.setWrap(true);
        messageLabel.setFontScale(1.05f);
        messageLabel.setColor(AppColors.SECONDARY_TEXT); // Softer color

        // Set a preferred width for message wrapping
        dialog.getContentTable().add(messageLabel).width(500).pad(20).center();
        dialog.getContentTable().row();

        // Button styling
        TextButton button = new TextButton(action, skin);
        button.getLabel().setColor(AppColors.BUTTON_TEXT);
        button.setColor(AppColors.BUTTON_BG_CYAN);
        dialog.button(button, action);

        // Padding and layout
        dialog.getButtonTable().padTop(10).padBottom(20);
        dialog.setModal(true);
        dialog.setMovable(false);
        dialog.setResizable(false);
        dialog.setColor(AppColors.PANEL_BG_LIGHT);

        // Pack and center
        dialog.pack();
        dialog.setPosition(
            (Gdx.graphics.getWidth() - dialog.getWidth()) / 2f,
            (Gdx.graphics.getHeight() - dialog.getHeight()) / 2f
        );

        return dialog;
    }



    public static Skin getSkin() {
        return skin;
    }
}
