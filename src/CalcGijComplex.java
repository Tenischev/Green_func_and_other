import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kris13 on 30.04.16.
 */
public class CalcGijComplex implements Callable<CalcGijComplex.GijComplex>{
    private final Matrix<Complex> matrix;
    private final ExecutorService pool;
    private final int i1;
    private final int j1;
    private final int dimension;
    private final int eps;
    private int THREADS;
    private final Object lock = new Object();
    private AtomicInteger counter = new AtomicInteger(0);

    private CalcG11.G11 g11_0;
    private CalcG11.G11 g11_1;

    public CalcGijComplex(Matrix<Complex> matrix, int i, int j, int accurancy, boolean useParallel) {
        this.matrix = matrix;
        this.dimension = matrix.getDimensional();
        this.eps = accurancy;
        this.i1 = i;
        this.j1 = j;
        if (useParallel)
            THREADS = Runtime.getRuntime().availableProcessors() + 1;
        else
            THREADS = 1;
        pool = Executors.newFixedThreadPool(THREADS);
    }

    @Override
    public GijComplex call() throws Exception {
        Complex[] xi = getVectorDefBasis(i1, dimension);
        Complex[] xj = getVectorDefBasis(j1, dimension);
        Complex[] temp = UtilsVector.add(xi, xj);
        Complex[] y1_0 = UtilsVector.multiplyToValue(temp, UtilsVector.getSecondNorm(temp).revert());
        temp = UtilsVector.subtract(xi, xj);
        Complex[] y1_1 = UtilsVector.multiplyToValue(temp, UtilsVector.getSecondNorm(temp).revert());
        TridiagonalMatrix<Complex> h0 = changeBasis(matrix, y1_0);
        TridiagonalMatrix<Complex> h1 = changeBasis(matrix, y1_1);
        pool.shutdown();
        return new GijComplex(h0, h1);
    }

    private TridiagonalMatrix<Complex> changeBasis(Matrix<Complex> matrix, Complex[] y1) {
        int dim = 0;
        Complex[] a = new Complex[y1.length];
        Complex[] b = new Complex[y1.length - 1];
        Complex[] y2;
        Complex[] temp;
        try {
            temp = UtilsVector.toZeros(UtilsVector.multiplyToMatrix(matrix, y1), 10E-15);
            a[0] = UtilsVector.multiply(y1, temp);
            if (a[0].isNaN())
                throw new IndexOutOfBoundsException("a is nan");
            dim++;
            temp = UtilsVector.toZeros(UtilsVector.subtract(temp, UtilsVector.multiplyToValue(y1, a[0])), 10E-15);
            b[0] = UtilsVector.getSecondNorm(temp);
            if (b[0].doubleValue() - 10E-15 <= 0 || b[0].isNaN())
                throw new IndexOutOfBoundsException("b is zero");
            y2 = UtilsVector.toZeros(UtilsVector.multiplyToValue(temp, b[0].revert()), 10E-15);

            for (int i = 1; i < y1.length - 1; i++) {
                temp = UtilsVector.toZeros(UtilsVector.multiplyToMatrix(matrix, y2), 10E-15);
                a[i] = UtilsVector.multiply(y2, temp);
                dim++;
                temp = UtilsVector.toZeros(UtilsVector.subtract(temp, UtilsVector.add(UtilsVector.multiplyToValue(y1, b[i - 1]), UtilsVector.multiplyToValue(y2, a[i]))), 10E-15);
                b[i] = UtilsVector.getSecondNorm(temp);
                if (b[i].doubleValue() - 10E-15 <= 0 || b[i].isNaN())
                    throw new IndexOutOfBoundsException("b is zero");
                y1 = y2;
                y2 = UtilsVector.toZeros(UtilsVector.multiplyToValue(temp, b[i].revert()), 10E-15);
            }
            a[y1.length - 1] = UtilsVector.multiply(y2, UtilsVector.multiplyToMatrix(matrix, y2));
        } catch (IndexOutOfBoundsException e) {
            //System.out.printf("Chain break on i = %d\n", dim);
            try {
                a = Arrays.copyOfRange(a, 0, dim);
                b = Arrays.copyOfRange(b, 0, dim - 1);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        //System.out.println("Min of V " + Arrays.stream(b).min((o1, o2) -> Double.compare(o1.doubleValue(), o2.doubleValue())).get().doubleValue());
        return new TridiagonalMatrix<>(a, b);
    }

    private Complex[] getVectorDefBasis(int i, int dimmension) {
        Complex[] vect = new Complex[dimmension];
        for (int j = 0; j < dimmension; j++) {
            vect[j] = Complex.ZERO;
        }
        vect[i] = Complex.ONE;
        return vect;
    }

    public class GijComplex {
        private TridiagonalMatrix<Complex> r1;
        private TridiagonalMatrix<Complex> r2;

        public GijComplex(TridiagonalMatrix<Complex> h0, TridiagonalMatrix<Complex> h1) {
            r1 = h0;
            r2 = h1;
        }

        public Complex getReal(Complex w) {
            Complex v1 = substitute(w, r1);
            Complex v2 = substitute(w, r2);
            if (r1 != null && r2 != null) {
                v1 = v1.multiply(new Complex(0.5, 0));
                v2 = v2.multiply(new Complex(0.5, 0));
            }
            return v1.subtract(v2);
        }

        private Complex substitute(Complex w, TridiagonalMatrix<Complex> matrix) {
            if (matrix == null)
                return Complex.ZERO;
            Complex[] a = matrix.getMainDiagonal();
            Complex[] b = matrix.getOffDiagonal();
            int i = b.length - 1;
            Complex ans = w.subtract(a[i + 1]);
            while (i >= 0) {
                ans = w.subtract(a[i]).subtract(b[i].multiply(b[i]).multiply(ans.revert()));
                i--;
            }
            return ans.revert();
        }
    }
}
