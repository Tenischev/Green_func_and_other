import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Calculate invert matrix from given tridiagonal matrix
 *
 * Created by kris13 on 17.03.16.
 */
public class InvertTridiagonalMatrix<T extends Number> implements Callable<double[][]> {

    private final T[] a;
    private final T[] b;
    private final T[] c;
    private double[] tetha;
    private double[] phi;
    private int THREADS;
    private ExecutorService pool;
    private AtomicInteger step;
    private final Object lock = new Object();
    private double[][] invert;

    /**
     * cnstr
     *
     * @param matrix the matrix
     * @param useParallel enable or disable parallel mode
     */
    public InvertTridiagonalMatrix(TridiagonalMatrix<T> matrix, boolean useParallel) {
        this.a = matrix.getMainDiagonal();
        if (matrix.isSymmetric()) {
            this.b = matrix.getOffDiagonal();
            this.c = this.b;
        } else {
            this.b = matrix.getUpDiagonal();
            this.c = matrix.getDownDiagonal();
        }
        if (useParallel)
            THREADS = Runtime.getRuntime().availableProcessors() + 1;
        else
            THREADS = 1;
        pool = Executors.newFixedThreadPool(THREADS);
    }

    @Override
    public double[][] call() throws Exception {
        step = new AtomicInteger(0);
        pool.execute(new CalcTetha());
        pool.execute(new CalcPhi());
        invert = new double[a.length][a.length];
        synchronized (lock) {
            while (step.get() != 2)
                lock.wait();
            step.set(0);
        }
        for (int i = 0; i < a.length * a.length; i++) {
            pool.execute(new CalcElem(i));
        }
        synchronized (lock) {
            while (step.get() != a.length * a.length)
                lock.wait();
        }
        pool.shutdown();
        return invert;
    }

    private class CalcTetha implements Runnable {
        @Override
        public void run() {
            tetha = new double[a.length + 1];
            tetha[0] = 1;
            tetha[1] = a[0].doubleValue();
            for (int i = 2; i <= a.length; i++) {
                tetha[i] = a[i - 1].doubleValue() * tetha[i - 1] - b[i - 2].doubleValue() * c[i - 2].doubleValue() * tetha[i - 2];
            }
            if (step.incrementAndGet() == 2)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }

    private class CalcPhi implements Runnable {
        @Override
        public void run() {
            phi = new double[a.length + 1];
            phi[a.length] = 1;
            phi[a.length - 1] = a[a.length - 1].doubleValue();
            for (int i = a.length - 2; i >= 0; i--) {
                phi[i] = a[i].doubleValue() * phi[i + 1] - b[i].doubleValue() * c[i].doubleValue() * phi[i + 2];
            }
            if (step.incrementAndGet() == 2)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }

    private class CalcElem implements Runnable {
        private final int i;
        private final int j;

        public CalcElem(int n) {
            this.i = n / a.length;
            this.j = n % a.length;
        }

        @Override
        public void run() {
            if (i + j % 2 == 1)
                invert[i][j] = -1.0;
            else
                invert[i][j] = 1.0;
            if (i <= j) {
                for (int k = i; k < j; k++) {
                    invert[i][j] *= b[k].doubleValue();
                }
                invert[i][j] *= tetha[i];
                invert[i][j] *= phi[j + 1];
            } else {
                for (int k = j; k < i; k++) {
                    invert[i][j] *= c[k].doubleValue();
                }
                invert[i][j] *= tetha[j];
                invert[i][j] *= phi[i + 1];
            }
            invert[i][j] /= tetha[a.length];
            if (step.incrementAndGet() == a.length * a.length)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }
}
