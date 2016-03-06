import javafx.util.Pair;
import javafx.util.converter.IntegerStringConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Created by kris13 on 05.03.16.
 */
public class Main {
    private static final int THREADS = 3;
    private static final int W = 50;
    private static final int dimension = 1000;
    private static final double eps = 0.0000001;
    private static double[] a;
    private static double[] b;
    private static final Random random = new Random();
    private static final boolean print = false;
    private final Object lock = new Object();
    private int step;
    AtomicInteger couter = new AtomicInteger(1);

    double[] c;
    double[] root;
    double[] rootNew;

    public static void main(String[] args) throws InterruptedException {
        long t = System.currentTimeMillis();
        new Main();
        System.out.println(String.format("Work complete on %d sec", (System.currentTimeMillis() - t) / 1000));
    }

    private Main() throws InterruptedException {
        a = random.doubles(dimension + 1).toArray();//.map(d -> (d % (2/W)) - 1/W)
        b = random.doubles(dimension).toArray();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        c = new double[dimension + 1];
        root = new double[1];
        root[0] = a[dimension];
        c[0] = 1;
        for (step = 1; step <= dimension; step++) {
            synchronized (lock) {
                while (couter.get() != step)
                    lock.wait();
            }
            couter.set(0);
            for (int j = 0; j < step; j++) {
                c[j] *= b[dimension - step]*b[dimension - step];
            }
            if (print) {
                System.out.print(String.format("Root of: x = %f", a[dimension - step]));
                for (int j = 0; j < step; j++) {
                    System.out.print(String.format(" + %f/(x - %f)", c[j], root[j]));
                }
                System.out.println();
            }
            rootNew = findRoots(c, root, a[dimension - step], step);
            if (print)
                System.out.print("Is ");
            for (int j = 0; j <= step; j++) {
                if (step > 500) {
                    pool.execute(new CalcC(j));
                } else {
                    c[j] = 1;
                    for (double aRoot : root) {
                        c[j] *= (rootNew[j] - aRoot);
                    }
                    for (int k = 0; k < rootNew.length; k++) {
                        if (k != j)
                            c[j] /= (rootNew[j] - rootNew[k]);
                    }
                    if (couter.incrementAndGet() == step)
                        synchronized (lock) {
                            lock.notify();
                        }
                }
                if (print)
                    System.out.print(rootNew[j] + " ");
            }
            root = rootNew;
            if (print)
                System.out.println();
        }
        pool.shutdown();
    }

    private double[] findRoots(double[] c, double[] x, double a, int ind) {
        double[] roots = new double[ind + 1];
        Function<Double, Double> func = root -> {
            double ans = a;
            for (int i = 0; i < ind; i++) {
                ans += c[i]/(root - x[i]);
            }
            return ans;
        };
        for (int i = 0; i < ind + 1; i++) {
            if (i == 0)
                roots[i] = findRoot(x[i], 10, func);
            else if (i == ind)
                roots[i] = findRoot(-10, x[i - 1], func);
            else roots[i] = findRoot(x[i], x[i - 1], func);
        }
        return roots;
    }

    private double findRoot(double l, double r, Function<Double, Double> func) {
        while (r - l > eps) {
            double m = (l / 2) + (r / 2);
            if (m > func.apply(m))
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
            c[cur] = 1;
            for (int k = 0; k < root.length; k++) {
                c[cur] *= (rootNew[cur] - root[k]);
                if (k != cur)
                    c[cur] /= (rootNew[cur] - rootNew[k]);
            }
            if (couter.incrementAndGet() == step)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }
}
