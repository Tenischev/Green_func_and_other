import java.util.Collection;

/**
 * Created by kris13 on 23.06.16.
 */
public class CheatMatrix<T extends Number> implements Matrix<T> {

    private final Collection<T>[] rows;

    private final int[] pos;

    public CheatMatrix(int[] positionDiagonal, Collection<T>[] rows) {
        this.rows = rows;
        this.pos = positionDiagonal;
    }

    public Collection<T>[] getRows() {
        return rows;
    }

    public Collection<T> getRow(int i) {
        return rows[i];
    }

    @Override
    public int getDimensional() {
        return getRows().length;
    }

    public int getPositionDiagonal(int i) {
        return pos[i];
    }
}
