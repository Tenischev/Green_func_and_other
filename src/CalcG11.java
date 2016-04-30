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
public abstract class CalcG11<T extends Number> implements Callable<G11<T>> {
    protected final int eps;
    protected final int dimension;
    protected final boolean print;
    protected final boolean log;
    protected int THREADS;
    protected T[] c;
    protected T[] x;
    protected int step;
    protected ExecutorService pool;
    protected AtomicInteger counter = new AtomicInteger(0);
    protected final Object lock = new Object();
    protected T[] rootNew;
    protected T[] a;
    protected T[] b;

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
}
