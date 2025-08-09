package chess;

import java.util.Objects;

public class ChessPosition {
    private final int row;
    private final int column;

    public ChessPosition (int row, int col) {
        this.row = row;
        this.column = col;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof ChessPosition that)) return false;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode(){
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "ChessPosition{" + "row=" + row + ", column=" + column + '}';
    }
}