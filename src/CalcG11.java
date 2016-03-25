import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Calculate element g11 - Green function for third diagonal hamiltonian
 *
 * Created by kris13 on 12.03.16.
 */
public class CalcG11<T extends Number> implements Callable<CalcG11.G11> {
    private final int eps;
    private final int dimension;
    private final boolean print;
    private final boolean log;
    private int THREADS;
    private BigDecimal[] c;
    private double[] averageC;
    private BigDecimal[] x;
    private int step;
    private ExecutorService pool;
    private AtomicInteger counter = new AtomicInteger(0);
    private final Object lock = new Object();
    private BigDecimal[] rootNew;
    private T[] a;
    private T[] b;

    /**
     * Set value hamiltonian and other options
     *
     * @param matrix the symmetric matrix of hamiltonian
     * @param accuracy number of sign after dot
     * @param useParallel use multi-thread calculate or not
     */
    public CalcG11(TridiagonalMatrix<T> matrix, int accuracy, boolean useParallel) {
        this(matrix, accuracy, useParallel, false, false);
    }

    /**
     * Set value hamiltonian and other options
     *
     * @param matrix the symmetric matrix of hamiltonian
     * @param accuracy number of sign after dot
     * @param useParallel use multi-thread calculate or not
     * @param log log step calculation
     * @param detailLog detail information as current function and roots
     */
    public CalcG11(TridiagonalMatrix<T> matrix, int accuracy, boolean useParallel, boolean log, boolean detailLog) {
        if (!matrix.isSymmetric())
            throw new IllegalArgumentException("Matrix must be symmetric");
        this.a = matrix.getMainDiagonal();
        this.b = matrix.getOffDiagonal();
        this.dimension = matrix.getMainDiagonal().length - 1;
        this.eps = accuracy;
        this.print = detailLog;
        this.log = log;
        if (useParallel)
            THREADS = Runtime.getRuntime().availableProcessors() + 1;
        else
            THREADS = 1;
        pool = Executors.newFixedThreadPool(THREADS);
    }

    @Override
    public G11 call() throws Exception {
        c = new BigDecimal[dimension + 1];
        averageC = new double[dimension];
        x = new BigDecimal[1];
        x[0] = new BigDecimal(a[dimension].doubleValue());
        c[0] = BigDecimal.ONE;
        for (step = 1; step <= dimension; step++) {
            BigDecimal bb = new BigDecimal(b[dimension - step].doubleValue()*b[dimension - step].doubleValue());
            if (log) System.out.print("Next step - " + step);
            for (int j = 0; j < step; j++) {
                if (log && BigDecimal.ZERO.equals(c[j]))
                    System.out.print(", I'm found zero C");
                c[j] = c[j].multiply(bb);
            }
            if (print) {
                System.out.format("Root of: x = %f", a[dimension - step].doubleValue());
                for (int j = 0; j < step; j++) {
                    System.out.format(" + %f/(x - %f)", c[j], x[j]);
                }
                System.out.println();
            }
            findRoots(c, x, new BigDecimal(a[dimension - step].doubleValue()), step);
            if (log) System.out.println(", roots calculate");
            if (print) {
                System.out.print("Is ");
                for (int j = 0; j <= step; j++)
                    System.out.print(rootNew[j].doubleValue() + " ");
            }
            counter.set(0);
            for (int j = 0; j <= step; j++) {
                if (step >= 50) {
                    pool.execute(new CalcC(j));
                } else {
                    c[j] = BigDecimal.ONE;
                    for (int k = 0; k < x.length; k++) {
                        c[j] = c[j].multiply(rootNew[j].subtract(x[k]));
                        if (k != j)
                            c[j] = c[j].divide(rootNew[j].subtract(rootNew[k]), eps, BigDecimal.ROUND_HALF_UP);
                    }
                    if (x.length != j)
                        c[j] = c[j].divide(rootNew[j].subtract(rootNew[x.length]), eps, BigDecimal.ROUND_HALF_UP);
                    counter.incrementAndGet();
                }
            }
            synchronized (lock) {
                while (counter.get() != step + 1) {
                    lock.wait();
                }
                counter.set(0);
            }
            calcAverageC();
            x = rootNew;
            if (print)
                System.out.println();
        }
        return new G11(x, c, averageC);
    }

    private void calcAverageC() {
        BigDecimal temp = new BigDecimal(0);
        for (int i = 0; i <= step; i++) {
            temp = temp.add(c[i]);
        }
        averageC[step - 1] = temp.divide(new BigDecimal(step + 1), eps, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private void findRoots(BigDecimal[] c, BigDecimal[] x, BigDecimal a, int ind) throws InterruptedException {
        rootNew = new BigDecimal[ind + 1];
        Function<BigDecimal, BigDecimal> func = root -> {
            BigDecimal ans = a;
            for (int i = 0; i < ind; i++) {
                ans = ans.add(c[i].divide(root.subtract(x[i]), eps, BigDecimal.ROUND_HALF_UP));
            }
            return ans;
        };
        counter.set(0);
        for (int i = 0; i < ind + 1; i++) {
            if (i == 0)
                pool.execute(new FindRood(i, x[i], new BigDecimal(100), func));
                //roots[i] = findRoot(x[i], new BigDecimal(100), func);
            else if (i == ind)
                pool.execute(new FindRood(i, new BigDecimal(-100), x[i - 1], func));
                //roots[i] = findRoot(new BigDecimal(-100), x[i - 1], func);
            else
                pool.execute(new FindRood(i, x[i], x[i - 1], func));
            //roots[i] = findRoot(x[i], x[i - 1], func);
        }
        synchronized (lock) {
            while (counter.get() != ind + 1) {
                lock.wait();
            }
            counter.set(0);
        }
    }

    private class FindRood implements Runnable {
        private final int cur;
        private final Function<BigDecimal, BigDecimal> func;
        private BigDecimal r;
        private BigDecimal l;

        public FindRood(int i, BigDecimal l, BigDecimal r, Function<BigDecimal, BigDecimal> func) {
            cur = i;
            this.l = l;
            this.r = r;
            this.func = func;
        }

        @Override
        public void run() {
            while (r.subtract(l).compareTo(BigDecimal.ONE.scaleByPowerOfTen(-eps)) > 0) {
                BigDecimal m = r.add(l).divide(BigDecimal.valueOf(2.0), eps, BigDecimal.ROUND_HALF_UP);
                if (m.compareTo(func.apply(m)) > 0)
                    r = m;
                else
                    l = m;
            }
            rootNew[cur] = l;
            if (counter.incrementAndGet() == rootNew.length)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }

    private BigDecimal findRoot(BigDecimal l, BigDecimal r, Function<BigDecimal, BigDecimal> func) {
        while (r.subtract(l).compareTo(BigDecimal.ONE.scaleByPowerOfTen(-eps)) > 0) {
            BigDecimal m = r.add(l).divide(BigDecimal.valueOf(2.0), eps, BigDecimal.ROUND_HALF_UP);
            if (m.compareTo(func.apply(m)) > 0)
                r = m;
            else
                l = m;
        }
        return l;
    }

    private class CalcC implements Runnable {
        private final int cur;

        public CalcC(int j) {
            cur = j;
        }

        @Override
        public void run() {
            c[cur] = BigDecimal.ONE;
            for (int k = 0; k < x.length; k++) {
                c[cur] = c[cur].multiply(rootNew[cur].subtract(x[k]));
                if (k != cur)
                    c[cur] = c[cur].divide(rootNew[cur].subtract(rootNew[k]), eps, BigDecimal.ROUND_HALF_UP);
            }
            if (x.length != cur)
                c[cur] = c[cur].divide(rootNew[cur].subtract(rootNew[x.length]), eps, BigDecimal.ROUND_HALF_UP);
            if (counter.incrementAndGet() == step + 1)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }

    public class G11 {
        private final BigDecimal[] x;
        private final BigDecimal[] c;
        private final double[] averC;

        public G11(BigDecimal[] x, BigDecimal[] c, double[] averageC) {
            this.x = x;
            this.c = c;
            this.averC = averageC;
        }

        public BigDecimal[] getC() {
            return c;
        }

        public double[] getCdouble() {
            return Arrays.stream(c).mapToDouble(BigDecimal::doubleValue).toArray();
        }

        public BigDecimal[] getX() {
            return x;
        }

        public double[] getXdouble() {
            return Arrays.stream(x).mapToDouble(BigDecimal::doubleValue).toArray();
        }

        public double[] getAverC() {
            return averC;
        }
    }
}
