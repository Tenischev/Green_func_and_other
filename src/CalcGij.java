import com.sun.xml.internal.ws.message.Util;
import com.sun.xml.internal.ws.util.UtilException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.stream.DoubleStream;

/**
 * Calculate element g_ij - Green function for third diagonal hamiltonian
 *
 * WARN: assumption standard basis init hamiltonian
 *
 * Created by kris13 on 26.03.16.
 */
public class CalcGij<T extends Number> implements Callable<Gij>{
    private final TridiagonalMatrix<T> matrix;
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
    public Gij call() throws Exception {
        double[] xi = getVectorDefBasis(i1, dimension);
        double[] xj = getVectorDefBasis(j1, dimension);
        double[] temp = UtilsVector.add(xi, xj);
        double[] y1_0 = UtilsVector.multiplyToValue(temp, 1.0 / UtilsVector.getSecondNorm(temp));
        temp = UtilsVector.subtract(xi, xj);
        double[] y1_1 = UtilsVector.multiplyToValue(temp, 1.0 / UtilsVector.getSecondNorm(temp));
        TridiagonalMatrix<Double> h0 = changeBasis(matrix, y1_0);
        pool.execute(new RunCalcG11<>(new CalcG11<>(h0, eps, true), 0));
        TridiagonalMatrix<Double> h1 = changeBasis(matrix, y1_1);
        pool.execute(new RunCalcG11<>(new CalcG11<>(h1, eps, true), 1));
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
        return new Gij(g11_0, g11_1, eps);
    }

    private TridiagonalMatrix<Double> changeBasis(TridiagonalMatrix<T> matrix, double[] y1) {
        int dim = 0;
        /*System.out.print("First basis vector ");
        for (int j = 0; j < y1.length; j++) {
            System.out.print(y1[j] + " ");
        }
        System.out.println();*/
        Double[] a = new Double[y1.length];
        Double[] b = new Double[y1.length - 1];
        double[] y2;
        double[] temp;
        try {
            temp = UtilsVector.multiplyToMatrix(matrix, y1);
            a[0] = UtilsVector.multiply(y1, temp);
            dim++;
            temp = UtilsVector.subtract(temp, UtilsVector.multiplyToValue(y1, a[0]));
            b[0] = UtilsVector.getSecondNorm(temp);
            if (b[0] - 10E-15 <= 0)
                throw new IndexOutOfBoundsException("b is zero");
            y2 = UtilsVector.multiplyToValue(temp, 1.0 / b[0]);
            /*System.out.print("Next basis vector ");
            for (int i = 0; i < y2.length; i++) {
                System.out.print(y2[i] + " ");
            }
            System.out.println();*/

            for (int i = 1; i < y1.length - 1; i++) {
                temp = UtilsVector.multiplyToMatrix(matrix, y2);
                a[i] = UtilsVector.multiply(y2, temp);
                dim++;
                temp = UtilsVector.subtract(temp, UtilsVector.add(UtilsVector.multiplyToValue(y1, b[i - 1]), UtilsVector.multiplyToValue(y2, a[i])));
                b[i] = UtilsVector.getSecondNorm(temp);
                if (b[i] - 10E-15 <= 0)
                    throw new IndexOutOfBoundsException("b is zero");
                y1 = y2;
                y2 = UtilsVector.multiplyToValue(temp, 1.0 / b[i]);
                /*System.out.print("Next basis vector ");
                for (int j = 0; j < y2.length; j++) {
                    System.out.print(y2[j] + " ");
                }
                System.out.println();*/
            }
            //System.out.println();
            a[y1.length - 1] = UtilsVector.multiply(y2, UtilsVector.multiplyToMatrix(matrix, y2));
        } catch (IndexOutOfBoundsException e) {
            System.out.printf("Chain break on i = %d\n", dim);
            a = Arrays.copyOfRange(a, 0, dim);
            b = Arrays.copyOfRange(b, 0, dim - 1);
        }
        return new TridiagonalMatrix<>(a, b);
    }

    private double[] getVectorDefBasis(int i, int dimmension) {
        double[] vect = new double[dimmension];
        vect[i] = 1;
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
}
