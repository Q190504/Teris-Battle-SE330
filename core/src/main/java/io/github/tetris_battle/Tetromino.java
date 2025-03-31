package io.github.tetris_battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class Tetromino {
    private Array<int[]> shape;
    private int row, col, type;

    public static final int[][][] SHAPES = {
        {{1, 1, 1, 1}}, // I
        {{1, 1}, {1, 1}}, // O
        {{0, 1, 0}, {1, 1, 1}}, // T
        {{1, 1, 0}, {0, 1, 1}}, // S
        {{0, 1, 1}, {1, 1, 0}}, // Z
        {{1, 1, 1}, {1, 0, 0}}, // L
        {{1, 1, 1}, {0, 0, 1}}  // J
    };

    public static Color getColorByType(int type) {
        switch (type) {
            case 0: return Color.CYAN;   // I
            case 1: return Color.YELLOW; // O
            case 2: return Color.GREEN;  // S
            case 3: return Color.RED;    // Z
            case 4: return Color.MAGENTA; // T (Purple isn't available in AWT)
            case 5: return Color.BLUE;   // J
            case 6: return Color.ORANGE; // L
            default: return Color.WHITE;
        }
    }


    public Tetromino(int type) {
        this.type = type;
        this.shape = convertToArray(SHAPES[type]);
        this.row = 0;
        this.col = 4 - shape.get(0).length / 2;
    }

    public Tetromino(Array<int[]> shape, int row, int col) {
        this.shape = shape;
        this.row = row;
        this.col = col;
    }

    public Tetromino clonePiece() {
        return new Tetromino(new Array<>(shape), row, col);
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

    public int getType() {
        return type;
    }

    public void move(int dir) {
        this.col += dir;
    }

    public void drop() {
        this.row++;
    }

    public void rotate() {
        int rows = shape.size;
        int cols = shape.get(0).length;
        Array<int[]> rotated = new Array<>();

        for (int j = 0; j < cols; j++) {
            int[] newRow = new int[rows];
            for (int i = 0; i < rows; i++) {
                newRow[i] = shape.get(rows - 1 - i)[j];
            }
            rotated.add(newRow);
        }

        shape = rotated;
    }

    public void draw(ShapeRenderer shapeRenderer, int posX, int posY) {
        shapeRenderer.setColor(getColorByType(this.type));
        for (int i = 0; i < shape.size; i++) {
            for (int j = 0; j < shape.get(i).length; j++) {
                if (shape.get(i)[j] == 1) {
                    shapeRenderer.rect((col + j) * 30 + posX, (row + i) * 30 + posY, 30, 30);
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
}
