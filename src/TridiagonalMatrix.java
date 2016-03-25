import java.lang.reflect.Array;

/**
 * View of tridiagonal matrix, require array of elements main diagonal
 * and array of elements off-diagonal
 *
 * Created by kris13 on 17.03.16.
 */
public class TridiagonalMatrix<T extends Number> {
    private final boolean symmetric;
    private final T[] b;
    private final T[] a;
    private final T[] c;

    /**
     * Create symmetric tridiagonal matrix
     *
     * @param mainDiag the elements main diagonal
     * @param offDiag the elements off diagonal
     */
    public TridiagonalMatrix(T[] mainDiag, T[] offDiag) {
        if (mainDiag.length != offDiag.length + 1)
            throw new IllegalArgumentException("Off diagonal must be less on one element then main diagonal");
        this.a = mainDiag;
        this.b = offDiag;
        this.c = b;
        this.symmetric = true;
    }


    /**
     * Create tridiagonal matrix
     *
     * @param mainDiag the elements main diagonal
     * @param upDiag the elements upper diagonal
     * @param downDiag the elements lower diagonal
     */
    public TridiagonalMatrix(T[] mainDiag, T[] upDiag, T[] downDiag) {
        if (mainDiag.length != upDiag.length + 1 || mainDiag.length != downDiag.length + 1)
            throw new IllegalArgumentException("Off diagonal must be less on one element then main diagonal");
        this.a = mainDiag;
        this.b = upDiag;
        this.c = downDiag;
        this.symmetric = false;
    }

    public boolean isSymmetric() {
        return symmetric;
    }

    public T[] getMainDiagonal() {
        return a;
    }

    public T[] getOffDiagonal() {
        if (!isSymmetric())
            throw new UnsupportedOperationException("This isn't symmetric matrix");
        return b;
    }

    public T[] getUpDiagonal() {
        if (isSymmetric())
            throw new UnsupportedOperationException("This is symmetric matrix");
        return b;
    }

    public T[] getDownDiagonal() {
        if (isSymmetric())
            throw new UnsupportedOperationException("This is symmetric matrix");
        return c;
    }
}
