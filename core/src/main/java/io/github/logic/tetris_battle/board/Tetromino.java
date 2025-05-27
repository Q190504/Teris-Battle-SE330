package io.github.logic.tetris_battle.board;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import io.github.logic.data.TetrominoDTO;

import java.util.ArrayList;
import java.util.List;

public class Tetromino {
    private Array<int[]> shape;
    private Array<int[]> initialShape;
    private int row, col, type;
    private int rotationState;

    Texture blackBlockTexture;
    Texture blueBlockTexture;
    Texture cyanBlockTexture;
    Texture greenBlockTexture;
    Texture orangeBlockTexture;
    Texture purpleBlockTexture;
    Texture redBlockTexture;
    Texture yellowBlockTexture;

    static Sprite ghostBlockSprite;
    static Sprite blueBlockSprite;
    static Sprite cyanBlockSprite;
    static Sprite greenBlockSprite;
    static Sprite orangeBlockSprite;
    static Sprite purpleBlockSprite;
    static Sprite redBlockSprite;
    static Sprite yellowBlockSprite;

    public static final int[][][] SHAPES = {
        {{1, 1, 1, 1}}, // I
        {{1, 1}, {1, 1}}, // O
        {{0, 1, 0}, {1, 1, 1}}, // T
        {{1, 1, 0}, {0, 1, 1}}, // S
        {{0, 1, 1}, {1, 1, 0}}, // Z
        {{1, 1, 1}, {1, 0, 0}}, // L
        {{1, 1, 1}, {0, 0, 1}}  // J
    };

    public static void loadAssets() {
        // Load textures
        Texture ghostBlockTexture = new Texture(Gdx.files.internal("sprites/Ghost.png"));
        Texture blueBlockTexture = new Texture(Gdx.files.internal("sprites/Blue.png"));
        Texture cyanBlockTexture = new Texture(Gdx.files.internal("sprites/Cyan.png"));
        Texture greenBlockTexture = new Texture(Gdx.files.internal("sprites/Green.png"));
        Texture orangeBlockTexture = new Texture(Gdx.files.internal("sprites/Orange.png"));
        Texture purpleBlockTexture = new Texture(Gdx.files.internal("sprites/Purple.png"));
        Texture redBlockTexture = new Texture(Gdx.files.internal("sprites/Red.png"));
        Texture yellowBlockTexture = new Texture(Gdx.files.internal("sprites/Yellow.png"));

        // Create sprites
        ghostBlockSprite = new Sprite(ghostBlockTexture);
        blueBlockSprite = new Sprite(blueBlockTexture);
        cyanBlockSprite = new Sprite(cyanBlockTexture);
        greenBlockSprite = new Sprite(greenBlockTexture);
        orangeBlockSprite = new Sprite(orangeBlockTexture);
        purpleBlockSprite = new Sprite(purpleBlockTexture);
        redBlockSprite = new Sprite(redBlockTexture);
        yellowBlockSprite = new Sprite(yellowBlockTexture);

        // Set size for all
        ghostBlockSprite.setSize(30, 30);
        blueBlockSprite.setSize(30, 30);
        cyanBlockSprite.setSize(30, 30);
        greenBlockSprite.setSize(30, 30);
        orangeBlockSprite.setSize(30, 30);
        purpleBlockSprite.setSize(30, 30);
        redBlockSprite.setSize(30, 30);
        yellowBlockSprite.setSize(30, 30);
    }

//    public static Color getColorByType(int type) {
//        switch (type) {
//            case 0: return Color.CYAN;   // I
//            case 1: return Color.YELLOW; // O
//            case 2: return Color.GREEN;  // S
//            case 3: return Color.RED;    // Z
//            case 4: return Color.MAGENTA; // T (Purple isn't available in AWT)
//            case 5: return Color.BLUE;   // J
//            case 6: return Color.ORANGE; // L
//            default: return Color.WHITE;
//        }
//    }

    public static Sprite getColorByType(int type) {
        switch (type) {
            case 0: return cyanBlockSprite;   // I
            case 1: return yellowBlockSprite; // O
            case 2: return greenBlockSprite;  // S
            case 3: return redBlockSprite;    // Z
            case 4: return purpleBlockSprite; // T
            case 5: return blueBlockSprite;   // J
            case 6: return orangeBlockSprite; // L
            default: return ghostBlockSprite;
        }
    }

    public Tetromino(int type) {
        this.type = type;
        this.shape = convertToArray(SHAPES[type]);
        this.initialShape = convertToArray(SHAPES[type]);
        this.row = 0;
        this.col = 0;
        this.rotationState = 0;
    }

    public Tetromino(Array<int[]> shape, int row, int col) {
        this.shape = shape;
        this.initialShape = shape;
        this.row = row;
        this.col = col;
    }

    public Tetromino clonePiece() {
        Tetromino clone = new Tetromino(type);
        clone.shape = convertToArray(shape.toArray(int[].class));
        //clone.initialShape = shape;
        clone.initialShape = convertToArray(initialShape.toArray(int[].class));
        clone.row = this.row;
        clone.col = this.col;
        clone.rotationState = this.rotationState;
        return clone;
    }

    public Array<int[]> getShape() {
        return shape;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getType() {
        return type;
    }

    public void move(int dir) {
        this.col += dir;
    }

    public void drop() {
        this.row--;
    }

    public void rotate() {
        rotationState = (rotationState + 1) % 4;
        int rows = initialShape.size;
        int cols = initialShape.get(0).length;
        Array<int[]> rotated = new Array<>();

        if (rotationState == 0) {
            rotated = convertToArray(initialShape.toArray(int[].class));
        } else if (rotationState == 1) {
            for (int j = 0; j < cols; j++) {
                int[] newRow = new int[rows];
                for (int i = 0; i < rows; i++) {
                    newRow[i] = shape.get(rows - 1 - i)[j];
                }
                rotated.add(newRow);
            }
        } else if (rotationState == 2) {
            for (int i = 0; i < rows; i++) {
                int[] newRow = new int[cols];
                for (int j = 0; j < cols; j++) {
                    newRow[j] = initialShape.get(rows - 1 - i)[cols - 1 - j];
                }
                rotated.add(newRow);
            }
        } else if (rotationState == 3) {
            for (int j = 0; j < cols; j++) {
                int[] newRow = new int[rows];
                for (int i = 0; i < rows; i++) {
                    newRow[i] = initialShape.get(i)[cols - 1 - j];
                }
                rotated.add(newRow);
            }
        }

        shape = rotated;
    }

    public void draw(SpriteBatch batch, int posX, int posY, int rows) {
        StringBuilder shapeLog = new StringBuilder("Drawing piece with shape:\n");
        for (int i = 0; i < shape.size; i++) {
            shapeLog.append("[");
            for (int j = 0; j < shape.get(i).length; j++) {
                shapeLog.append(shape.get(i)[j]);
                if (j < shape.get(i).length - 1) shapeLog.append(", ");
            }
            shapeLog.append("]\n");
        }
        //Gdx.app.log("Draw", shapeLog.toString());

        for (int i = 0; i < shape.size; i++) {
            for (int j = 0; j < shape.get(i).length; j++) {
                if (shape.get(i)[j] == 1) {
                    Sprite blockSprite = getColorByType(this.type);
                    blockSprite.setPosition((col + j) * 30 + posX, (row + i) * 30 + posY);
                    blockSprite.draw(batch);
                }
            }
        }
    }


    private Array<int[]> convertToArray(int[][] shapeArray) {
        Array<int[]> array = new Array<>();
        for (int[] row : shapeArray) {
            array.add(row.clone());
        }
        return array;
    }

    public TetrominoDTO toDTO() {
        TetrominoDTO dto = new TetrominoDTO();
        dto.type = this.type;
        dto.row = this.row;
        dto.col = this.col;
        dto.rotationState = this.rotationState;
        List<int[]> shapeArray = new ArrayList<>();
        for (int[] row : this.shape) {
            shapeArray.add(row);
        }
        dto.shape = shapeArray;
        return dto;
    }

    public static Tetromino fromDTO(TetrominoDTO dto) {
        Tetromino tetromino = new Tetromino(dto.type);
        tetromino.setRow(dto.row);
        tetromino.setCol(dto.col);
        tetromino.rotationState = dto.rotationState;
        Array<int[]> shapeArray = new Array<>();
        for (int[] row : dto.shape) {
            shapeArray.add(row);
        }
        tetromino.shape = shapeArray;
        return tetromino;
    }


}
