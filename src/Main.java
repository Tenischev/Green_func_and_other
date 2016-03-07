import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Created by kris13 on 05.03.16.
 */
public class Main {
    private static final int THREADS = Runtime.getRuntime().availableProcessors() + 1;
    private static final double W = 50;
    private static final int dimension = 40;
    private static final double eps = 0.00000000000001;
    private static double[] a;
    private static double[] b;
    private static final Random random = new Random();
    private static final boolean print = false;
    private final Object lock = new Object();
    private int step;
    private AtomicInteger couter;
    private ExecutorService pool = Executors.newFixedThreadPool(THREADS);

    double[] c;
    double[] root;
    double[] rootNew;
    private double x = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println(THREADS);
        new Main();
    }

    private Main() throws InterruptedException {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFrame frame = new JFrame();
        frame.getContentPane().add(new ChartPanel(ChartFactory.createXYLineChart("Roots", "ind", "Value", dataset)));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        for (int i = 0; i < 20; i++) {
            long t = System.currentTimeMillis();
            x = i;
            System.out.println("Start calculate");
            calculate();
            System.out.println(String.format("Work complete on %d sec", (System.currentTimeMillis() - t) / 1000));
            double[][] d = new double[2][root.length];
            for (int j = 0; j < root.length; j++) {
                d[1][j] = root[j];
                d[0][j] = j;
            }
            dataset.addSeries(i, d);
        }
        pool.shutdown();
    }

    private void calculate() throws InterruptedException {
        a = random.doubles(dimension + 1).map(d -> d * 3.0 + Math.signum(d) * (x / 2.0)).toArray();
        b = new double[dimension];Arrays.fill(b, 1.0);//random.doubles(dimension).toArray();
        couter = new AtomicInteger(0);
        c = new double[dimension + 1];
        root = new double[1];
        root[0] = a[dimension];
        c[0] = 1;
        for (step = 1; step <= dimension; step++) {
            System.out.print("Next step - " + step);
            for (int j = 0; j < step; j++) {
                if (c[j] == 0.0)
                    System.out.print(", I'm found zero C");
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
            System.out.println(", roots calculate");
            if (print) {
                System.out.print("Is ");
                for (int j = 0; j <= step; j++)
                    System.out.print(rootNew[j] + " ");
            }
            for (int j = 0; j <= step; j++) {
                if (step > 100) {
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
                    couter.incrementAndGet();
                }
            }
            root = rootNew;
            synchronized (lock) {
                while (couter.get() != step + 1) {
                    lock.wait();
                }
                couter.set(0);
            }
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
                roots[i] = findRoot(x[i], Double.MAX_VALUE, func);
            else if (i == ind)
                roots[i] = findRoot(-Double.MAX_VALUE, x[i - 1], func);
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
            for (double aRoot : root) {
                c[cur] *= (rootNew[cur] - aRoot);
            }
            for (int k = 0; k < rootNew.length; k++) {
                if (k != cur)
                    c[cur] /= (rootNew[cur] - rootNew[k]);
            }
            /*for (int k = 0; k < root.length; k++) {
                c[cur] *= (rootNew[cur] - root[k]);
                if (k != cur)
                    c[cur] /= (rootNew[cur] - rootNew[k]);
            }
            if (root.length != cur)
                c[cur] /= (rootNew[cur] - rootNew[root.length]);*/
            if (couter.incrementAndGet() == step + 1)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }
}
