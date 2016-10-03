import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

/**
 * View of tridiagonal matrix, require array of elements main diagonal
 * and array of elements off-diagonal
 *
 * Created by kris13 on 17.03.16.
 */
public class TridiagonalMatrix<T extends Number> implements Matrix<T>{
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

    public static <T extends Number> TridiagonalMatrix<T> generateOneDimMatrix(int size, T e, T w, T v) {
        T[] a;
        Random random = new Random();
        if (e instanceof Double) {
            a = (T[]) new Double[size + 1];
        } else if (e instanceof Complex) {
            a = (T[]) new Complex[size + 1];
        } else if (e instanceof BigDecimal) {
            a = (T[]) new BigDecimal[size + 1];
        } else {
            a = (T[]) new Number[size + 1];
        }
        //Arrays.fill(a, e);
        for (int i = 0; i < a.length; i++) {
            if (w instanceof Double) {
                Double d = (Double) w;
                Double eD = (Double) e;
                double rand = 0.5 - random.nextDouble();
                a[i] = (T) new Double(eD + rand * d);
            } else if (w instanceof Complex) {
                Complex c = (Complex) w;
                Complex eC = (Complex) e;
                double rand = 0.5 - random.nextDouble();
                a[i] = (T) new Complex(eC.getReal() + rand * c.getReal(), eC.getImage() + rand * c.getImage());
            } else if (e instanceof BigDecimal) {
                BigDecimal bD = (BigDecimal) w;
                BigDecimal eBD = (BigDecimal) e;
                double rand = 0.5 - random.nextDouble();
                a[i] = (T) bD.multiply(new BigDecimal(rand)).add(eBD);
            } else {
                a[i] = e;
            }
        }
        T[] b;
        if (e instanceof Double) {
            b = (T[]) new Double[size];
        } else if (e instanceof Complex) {
            b = (T[]) new Complex[size];
        } else if (e instanceof BigDecimal) {
            b = (T[]) new BigDecimal[size];
        } else {
            b = (T[]) new Number[size];
        }
        Arrays.fill(b, v);
        return new TridiagonalMatrix<T>(a, b);
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

    @Override
    public int getDimensional() {
        return getMainDiagonal().length;
    }
}
