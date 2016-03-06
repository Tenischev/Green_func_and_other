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
    private static final int W = 50;
    private static final int dimension = 500;
    private static final double eps = 0.0000001;
    private static double[] a;
    private static double[] b;
    private static final Random random = new Random();
    private static final boolean print = false;

    public static void main(String[] args) throws InterruptedException {
        long t = System.currentTimeMillis();
        new Main();
        System.out.println(String.format("Work complete on %d sec", (System.currentTimeMillis() - t) / 1000));
    }

    private Main() throws InterruptedException {
        a = random.doubles(dimension + 1).toArray();//.map(d -> (d % (2/W)) - 1/W)
        b = random.doubles(dimension).toArray();

        double[] c = new double[dimension + 1];
        double[] root = new double[1];
        root[0] = a[dimension];
        c[0] = 1;
        for (int i = 1; i <= dimension; i++) {
            for (int j = 0; j < i; j++) {
                c[j] *= b[dimension - i]*b[dimension - i];
            }
            if (print) {
                System.out.print(String.format("Root of: x = %f", a[dimension - i]));
                for (int j = 0; j < i; j++) {
                    System.out.print(String.format(" + %f/(x - %f)", c[j], root[j]));
                }
                System.out.println();
            }
            double[] rootNew = findRoots(c, root, a[dimension - i], i);
            if (print)
                System.out.print("Is ");
            for (int j = 0; j <= i; j++) {
                if (print)
                    System.out.print(rootNew[j] + " ");
                c[j] = 1;
                for (double aRoot : root) {
                    c[j] *= (rootNew[j] - aRoot);
                }
                for (int k = 0; k < rootNew.length; k++) {
                    if (k != j)
                        c[j] /= (rootNew[j] - rootNew[k]);
                }
            }
            root = rootNew;
            if (print)
                System.out.println();
        }
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
}
