import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.*;
import java.math.BigDecimal;
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
    private static final int dimension = 100;
    private static final int eps = 20;
    private static double[] a;
    private static double[] b;
    private static final Random random = new Random();
    private static final boolean print = false;
    private final Object lock = new Object();
    private int step;
    private AtomicInteger counter = new AtomicInteger(0);
    private ExecutorService pool = Executors.newFixedThreadPool(THREADS);

    BigDecimal[] c;
    BigDecimal[] root;
    BigDecimal[] rootNew;
    private double x = 0;
    private double y;
    private double[] averageC;

    public static void main(String[] args) throws InterruptedException {
        System.out.println(THREADS);
        new Main();
    }

    private Main() throws InterruptedException {
        DefaultXYDataset datasetRoots = new DefaultXYDataset();
        DefaultXYDataset datasetDistribute = new DefaultXYDataset();
        DefaultXYDataset datasetProbabilityDensity = new DefaultXYDataset();
        DefaultXYDataset datasetAverageC = new DefaultXYDataset();
        DefaultXYDataset datasetC = new DefaultXYDataset();
        DefaultXYDataset datasetXvsC = new DefaultXYDataset();
        DefaultXYDataset datasetHistogram = new DefaultXYDataset();
        JFrame[] frame = new JFrame[7];
        for (int i = 0; i < frame.length; i++) {
            frame[i] = new JFrame();
            switch (i){
                case 0 :
                    frame[i].getContentPane().add(new ChartPanel(ChartFactory.createScatterPlot("Roots", "index", "Value", datasetRoots)));
                    break;
                case 1 :
                    frame[i].getContentPane().add(new ChartPanel(ChartFactory.createXYLineChart("Distribution Roots", "x", "P(x)", datasetDistribute)));
                    break;
                case 2 :
                    frame[i].getContentPane().add(new ChartPanel(ChartFactory.createXYLineChart("Probability Density Roots", "x", "f(x)", datasetProbabilityDensity)));
                    break;
                case 3 :
                    frame[i].getContentPane().add(new ChartPanel(ChartFactory.createXYLineChart("Average C", "step", "C", datasetAverageC)));
                    break;
                case 4 :
                    frame[i].getContentPane().add(new ChartPanel(ChartFactory.createScatterPlot("C on last step", "index", "C", datasetC)));
                    break;
                case 5 :
                    frame[i].getContentPane().add(new ChartPanel(ChartFactory.createScatterPlot("X vs C", "X_i", "C_i", datasetXvsC)));
                    break;
                case 6 :
                    frame[i].getContentPane().add(new ChartPanel(ChartFactory.createScatterPlot("histogram", "x", "c", datasetHistogram)));
                    break;
            }
            frame[i].pack();
            frame[i].setVisible(true);
            frame[i].setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        double[][] hist = new double[2][100];
        for (int i = 0; i < 100; i++) {
            hist[0][i] = -5.0 + i * 0.05;
        }
        for (y = 0; y < 100 ; y++) {
            for (int i = 1; i < 2; i++) {
                long t = System.currentTimeMillis();
                x = i * 0.1;
                System.out.println("Start calculate");
                calculate();
                System.out.println(String.format("Work complete on %d sec", (System.currentTimeMillis() - t) / 1000));
                double[][] d = new double[2][root.length];
                for (int j = 0; j < root.length; j++) {
                    d[1][j] = root[j].doubleValue();
                    d[0][j] = j;
                }
                //double[] distribution = generateDistributionFunc(d[1]);
                double[][] p = new double[2][root.length];
                for (int j = 0; j < root.length; j++) {
                    p[1][j] = (double) (j + 1) / root.length;
                    p[0][j] = d[1][root.length - j - 1];
                }
                double[][] f = new double[2][root.length - 1];
                for (int j = 0; j < root.length - 1; j++) {
                    f[1][j] = (p[1][j + 1] - p[1][j]) / (p[0][j + 1] - p[0][j]);
                    f[0][j] = (p[0][j] + p[0][j + 1]) / 2.0;
                }
                double[][] avC = new double[2][averageC.length];
                for (int j = 0; j < averageC.length; j++) {
                    avC[1][j] = averageC[j];
                    avC[0][j] = j;
                }
                double[][] lastC = new double[2][c.length];
                for (int j = 0; j < c.length; j++) {
                    lastC[1][j] = c[j].doubleValue();
                    lastC[0][j] = j;
                }
                double[][] xVSc = new double[2][c.length];
                xVSc[0] = d[1];
                xVSc[1] = lastC[1];
                if (i == 1)
                    for (int j = 0; j < c.length; j++) {
                        hist[1][(int) Math.round((xVSc[0][j] + 5) * 20)] += xVSc[1][j];
                    }
                datasetRoots.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), d);
                datasetDistribute.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), p);
                datasetProbabilityDensity.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), f);
                datasetAverageC.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), avC);
                datasetC.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), lastC);
                datasetXvsC.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), xVSc);
            }
        }
        datasetHistogram.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), hist);
        pool.shutdown();
    }

    private double[] generateDistributionFunc(double[] vals) {
        double[] distribution = new double[(int) (vals[0] - vals[vals.length - 1]) * 1000];
        int ind = vals.length - 1;
        for (int i = 0; i < distribution.length; i++) {
            while (vals[ind] < vals[vals.length - 1] + i * 0.001 && ind > 0) ind--;
            distribution[i] = vals.length - ind;
        }
        return distribution;
    }

    private void calculate() throws InterruptedException {
        a = random.doubles(dimension + 1).map(d -> (-3.0 + x * d)).toArray();
        b = new double[dimension];Arrays.fill(b, 1.0);//random.doubles(dimension).toArray();
        c = new BigDecimal[dimension + 1];
        averageC = new double[dimension];
        root = new BigDecimal[1];
        root[0] = new BigDecimal(a[dimension]);
        c[0] = BigDecimal.ONE;
        for (step = 1; step <= dimension; step++) {
            BigDecimal bb = new BigDecimal(b[dimension - step]*b[dimension - step]);
            System.out.print("Next step - " + step);
            for (int j = 0; j < step; j++) {
                if (BigDecimal.ZERO.equals(c[j]))
                    System.out.print(", I'm found zero C");
                c[j] = c[j].multiply(bb);
            }
            if (print) {
                System.out.print(String.format("Root of: x = %f", a[dimension - step]));
                for (int j = 0; j < step; j++) {
                    System.out.print(String.format(" + %f/(x - %f)", c[j], root[j]));
                }
                System.out.println();
            }
            findRoots(c, root, new BigDecimal(a[dimension - step]), step);
            System.out.println(", roots calculate");
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
                    for (int k = 0; k < root.length; k++) {
                        c[j] = c[j].multiply(rootNew[j].subtract(root[k]));
                        if (k != j)
                            c[j] = c[j].divide(rootNew[j].subtract(rootNew[k]), eps, BigDecimal.ROUND_HALF_UP);
                    }
                    if (root.length != j)
                        c[j] = c[j].divide(rootNew[j].subtract(rootNew[root.length]), eps, BigDecimal.ROUND_HALF_UP);
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
            root = rootNew;
            if (print)
                System.out.println();
        }
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
            for (int k = 0; k < root.length; k++) {
                c[cur] = c[cur].multiply(rootNew[cur].subtract(root[k]));
                if (k != cur)
                    c[cur] = c[cur].divide(rootNew[cur].subtract(rootNew[k]), eps, BigDecimal.ROUND_HALF_UP);
            }
            if (root.length != cur)
                c[cur] = c[cur].divide(rootNew[cur].subtract(rootNew[root.length]), eps, BigDecimal.ROUND_HALF_UP);
            /*for (int k = 0; k < root.length; k++) {
                c[cur] *= (rootNew[cur] - root[k]);
                if (k != cur)
                    c[cur] /= (rootNew[cur] - rootNew[k]);
            }
            if (root.length != cur)
                c[cur] /= (rootNew[cur] - rootNew[root.length]);*/
            if (counter.incrementAndGet() == step + 1)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }
}
