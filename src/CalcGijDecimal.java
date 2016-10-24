import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Calculate element g_ij - Green function for third diagonal hamiltonian
 *
 * WARN: assumption standard basis init hamiltonian
 *
 * Created by kris13 on 26.03.16.
 */
public class CalcGijDecimal implements Callable<CalcGijDecimal.GijDecimal>{
    private final Matrix<BigDecimal> matrix;
    private final ExecutorService pool;
    private final int i1;
    private final int j1;
    private final int dimension;
    private final MathContext mathContext;
    private final int eps;
    private int THREADS;
    private final Object lock = new Object();
    private AtomicInteger counter = new AtomicInteger(0);

    private CalcG11.G11 g11_0;
    private CalcG11.G11 g11_1;

    public CalcGijDecimal(Matrix<BigDecimal> matrix, int i, int j, int accurancy, boolean useParallel) {
        this.matrix = matrix;
        this.dimension = matrix.getDimensional();
        this.eps = accurancy;
        this.mathContext = new MathContext(accurancy, RoundingMode.HALF_UP);
        this.i1 = i;
        this.j1 = j;
        if (useParallel)
            THREADS = Runtime.getRuntime().availableProcessors() + 1;
        else
            THREADS = 1;
        pool = Executors.newFixedThreadPool(THREADS);
    }

    @Override
    public GijDecimal call() throws Exception {
        BigDecimal[] xi = getVectorDefBasis(i1, dimension);
        BigDecimal[] xj = getVectorDefBasis(j1, dimension);
        BigDecimal[] temp = UtilsVector.add(xi, xj, mathContext);
        TridiagonalMatrix<BigDecimal> h0 = null;
        TridiagonalMatrix<BigDecimal> h1 = null;
        try {
            BigDecimal[] y1_0 = UtilsVector.multiplyToValue(temp, BigDecimal.ONE.divide(UtilsVector.getSecondNorm(temp, mathContext), mathContext), mathContext);
            h0 = changeBasis(matrix, y1_0);
        } catch (ArithmeticException e) {
            h0 = null;
        }
        if (i1 != j1) {
            temp = UtilsVector.subtract(xi, xj, mathContext);
            try {
                BigDecimal[] y1_1 = UtilsVector.multiplyToValue(temp, BigDecimal.ONE.divide(UtilsVector.getSecondNorm(temp, mathContext), mathContext), mathContext);
                h1 = changeBasis(matrix, y1_1);
            } catch (ArithmeticException e) {
                h1 = null;
            }
        }
/*        synchronized (lock) {
            while (counter.get() != 2) {
                lock.wait();
            }
            counter.set(0);
        }
        pool.shutdown();
        if (g11_0 == null)
            return g11_1;
        if (g11_1 == null)
            return g11_0;
        return new Gij(g11_0, g11_1, eps);*/
        return new GijDecimal(h0, h1, mathContext);
    }

    private TridiagonalMatrix<BigDecimal> changeBasis(Matrix<BigDecimal> matrix, BigDecimal[] y1) {
        int dim = 0;
        BigDecimal[] a = new BigDecimal[y1.length];
        BigDecimal[] b = new BigDecimal[y1.length - 1];
        BigDecimal[] y2;
        BigDecimal[] temp;
        List<BigDecimal[]> basis = new ArrayList<>();
        basis.add(y1);
        try {
            temp = UtilsVector.multiplyToMatrix(matrix, y1, mathContext);
            a[0] = UtilsVector.multiply(y1, temp, mathContext);
            dim++;
            temp = UtilsVector.subtract(temp, UtilsVector.multiplyToValue(y1, a[0], mathContext), mathContext);
            b[0] = UtilsVector.getSecondNorm(temp, mathContext);
            if (b[0].scale() < mathContext.getPrecision())
                throw new IndexOutOfBoundsException("b is zero");
            y2 = UtilsVector.multiplyToValue(temp, BigDecimal.ONE.divide(b[0], mathContext), mathContext);
            checkBasis(basis, y2);
            basis.add(y2);

            for (int i = 1; i < y1.length - 1; i++) {
                //System.out.println("Start calculate " + i + " basis vector");
                temp = UtilsVector.multiplyToMatrix(matrix, y2, mathContext);
                temp = UtilsVector.toZeros(temp, mathContext);
                a[i] = UtilsVector.multiply(y2, temp, mathContext);
                dim++;
                temp = UtilsVector.subtract(temp, UtilsVector.add(UtilsVector.multiplyToValue(y1, b[i - 1], mathContext), UtilsVector.multiplyToValue(y2, a[i], mathContext), mathContext), mathContext);
                b[i] = UtilsVector.getSecondNorm(temp, mathContext);
                if (b[i].scale() > mathContext.getPrecision())
                    throw new IndexOutOfBoundsException("b is zero");
                y1 = y2;
                y2 = UtilsVector.multiplyToValue(temp, BigDecimal.ONE.divide(b[i], mathContext), mathContext);
                checkBasis(basis, y2);
                basis.add(y2);
                y1 = UtilsVector.toZeros(y1, mathContext);
                y2 = UtilsVector.toZeros(y2, mathContext);
            }
            a[y1.length - 1] = UtilsVector.multiply(y2, UtilsVector.multiplyToMatrix(matrix, y2, mathContext), mathContext);
        } catch (IndexOutOfBoundsException | ArithmeticException e) {
            System.out.printf("Chain break on i = %d\n", dim);
            a = Arrays.copyOfRange(a, 0, dim);
            b = Arrays.copyOfRange(b, 0, dim - 1);
        }
        return new TridiagonalMatrix<>(a, b);
    }

    private void checkBasis(List<BigDecimal[]> basis, BigDecimal[] y2) {
        for (BigDecimal[] v : basis) {
            BigDecimal s  = UtilsVector.multiply(v, y2, mathContext);
            if (s.doubleValue() > 1e-2) {
                System.out.println("WARNING! Vectors not orthonormal on step " + basis.size());
                return;
            }
        }
    }

    private BigDecimal[] getVectorDefBasis(int i, int dimmension) {
        BigDecimal[] vect = new BigDecimal[dimmension];
        for (int j = 0; j < vect.length; j++) {
            vect[j] = new BigDecimal(0.0);
        }
        vect[i] = new BigDecimal(1.0);
        return vect;
    }

    private class RunCalcG11<E extends Number> implements Runnable {
        private final CalcG11<E> task;
        private int ind;

        public RunCalcG11(CalcG11<E> g11, int ind) {
            this.task = g11;
            this.ind = ind;
        }

        @Override
        public void run() {
            try {
                if (ind == 0)
                    g11_0 = task.call();
                else
                    g11_1 = task.call();
            } catch (Exception e) {
                System.out.printf("Can't change basis for vector %d\n", ind);
            }
            if (counter.incrementAndGet() == 2)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }

    public class GijDecimal {
        private final MathContext context;
        private TridiagonalMatrix<BigDecimal> r1;
        private TridiagonalMatrix<BigDecimal> r2;

        public GijDecimal(TridiagonalMatrix<BigDecimal> h0, TridiagonalMatrix<BigDecimal> h1, MathContext context) {
            r1 = h0;
            r2 = h1;
            this.context = context;
        }

        public BigDecimal getValue(BigDecimal w) {
            BigDecimal v1 = substitute(w, r1);
            BigDecimal v2 = substitute(w, r2);
            if (r1 != null && r2 != null) {
                v1 = v1.divide(new BigDecimal(2), context);
                v2 = v2.divide(new BigDecimal(2), context);
            }
            return v1.subtract(v2);
        }

        private BigDecimal substitute(BigDecimal w, TridiagonalMatrix<BigDecimal> matrix) {
            if (matrix == null)
                return BigDecimal.ZERO;
            BigDecimal[] a = matrix.getMainDiagonal();
            BigDecimal[] b = matrix.getOffDiagonal();
            int i = b.length - 1;
            BigDecimal ans = w.subtract(a[i + 1], context);
            while (i >= 0) {
                ans = w.subtract(a[i], context)
                        .subtract(b[i].multiply(b[i], context)
                                .multiply(BigDecimal.ONE.divide(ans, context), context), context);
                i--;
            }
            return BigDecimal.ONE.divide(ans, context);
        }
    }
}
