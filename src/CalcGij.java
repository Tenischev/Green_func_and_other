import java.util.Arrays;
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
public abstract class CalcGij<T extends Number> implements Callable<Gij<T>>{
    protected final TridiagonalMatrix<T> matrix;
    protected final ExecutorService pool;
    protected final int i1;
    protected final int j1;
    protected final int dimension;
    protected final int eps;
    protected int THREADS;
    protected final Object lock = new Object();
    protected AtomicInteger counter = new AtomicInteger(0);

    private G11<T> g11_0;
    private G11<T> g11_1;

    public CalcGij(TridiagonalMatrix<T> matrix, int i, int j, int accurancy, boolean useParallel) {
        this.matrix = matrix;
        this.dimension = matrix.getMainDiagonal().length;
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
    public Gij<T> call() throws Exception {
        double[] xi = getVectorDefBasis(i1, dimension);
        double[] xj = getVectorDefBasis(j1, dimension);
        double[] temp = UtilsVector.add(xi, xj);
        double[] y1_0 = UtilsVector.multiplyToValue(temp, 1.0 / UtilsVector.getSecondNorm(temp));
        temp = UtilsVector.subtract(xi, xj);
        double[] y1_1 = UtilsVector.multiplyToValue(temp, 1.0 / UtilsVector.getSecondNorm(temp));
        TridiagonalMatrix<T> h0 = changeBasis(matrix, y1_0);
        pool.execute(new RunCalcG11(getCalcG11ByImpl(h0, eps, true), 0));
        TridiagonalMatrix<T> h1 = changeBasis(matrix, y1_1);
        pool.execute(new RunCalcG11(getCalcG11ByImpl(h1, eps, true), 1));
        synchronized (lock) {
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
        return getGijByImpl(g11_0, g11_1, eps);
    }

    protected abstract CalcG11<T> getCalcG11ByImpl(TridiagonalMatrix<T> h0, int eps, boolean b);

    protected abstract Gij<T> getGijByImpl(G11<T> g11_0, G11<T> g11_1, int eps);

    protected abstract TridiagonalMatrix<T> changeBasis(TridiagonalMatrix<T> matrix, double[] y1_0);

    private double[] getVectorDefBasis(int i, int dimmension) {
        double[] vect = new double[dimmension];
        vect[i] = 1;
        return vect;
    }
    private class RunCalcG11 implements Runnable {
        private final CalcG11<T> task;
        private int ind;

        public RunCalcG11(CalcG11<T> g11, int ind) {
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
}
