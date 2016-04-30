import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.DefaultIntervalXYDataset;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.*;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

/**
 * Created by kris13 on 05.03.16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        new Main();
    }

    private Main() throws Exception {
        System.out.println("Start");
        calcGij();
        //calcG11();
        //calcInvert();
        //calcTridiagonal();
        System.out.println("End");
    }

    private Gij calcGij() throws Exception {
        Random random = new Random();
        int dimension = 100;
        Double[] a = new Double[dimension + 1];Arrays.fill(a, -3.0);//random.doubles(dimension + 1).map(d -> (d * (r - l) + l)).boxed().toArray(Double[]::new);
        Double[] b = new Double[dimension];Arrays.fill(b, 0.1);//random.doubles(dimension).toArray();
        TridiagonalMatrix<Double> matrix = new TridiagonalMatrix<>(a, b);
        CalcGij<Double> taskGij = new CalcGij<>(matrix, 50, 50, 25, true);
        Gij gij = taskGij.call();
        System.out.println(gij.getValue(0));
        return gij;
    }

    private void calcTridiagonal() throws Exception {
        Integer[][] matrix = {{4,1,-2,2},{1,2,0,1},{-2,0,3,-2},{2,1,-2,-1}};
        Diagonalization<Integer> diagonalization = new Diagonalization<>(matrix);
        TridiagonalMatrix<Double> tridiagonalMatrix = diagonalization.call();
        Double[] a = tridiagonalMatrix.getMainDiagonal();
        Double[] b = tridiagonalMatrix.getOffDiagonal();
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + " ");
        }
        System.out.println();
        for (int i = 0; i < b.length; i++) {
            System.out.print(b[i] + " ");
        }
        System.out.println();
    }

    private void calcInvert() throws Exception {
        Random random = new Random();
        int dimension = 2;
        Double[] a = random.ints(dimension + 1).map(i -> i % 100).asDoubleStream().boxed().toArray(Double[]::new);
        Double[] b = new Double[dimension];Arrays.fill(b, 1.0);//random.doubles(dimension).toArray();
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + " ");
        }
        System.out.println();
        for (int i = 0; i < b.length; i++) {
            System.out.print(b[i] + " ");
        }
        System.out.println();
        InvertTridiagonalMatrix<Double> invertMatrix = new InvertTridiagonalMatrix<>(new TridiagonalMatrix<Double>(a, b), true);
        double[][] invert = invertMatrix.call();
        for (int i = 0; i < invert.length; i++) {
            for (int j = 0; j < invert.length; j++) {
                System.out.print(invert[i][j] + " ");
            }
            System.out.println();
        }
    }

    private void calcG11() throws Exception {
        DefaultXYDataset datasetRoots = new DefaultXYDataset();
        DefaultXYDataset datasetDistribute = new DefaultXYDataset();
        DefaultXYDataset datasetProbabilityDensity = new DefaultXYDataset();
        DefaultXYDataset datasetAverageC = new DefaultXYDataset();
        DefaultXYDataset datasetC = new DefaultXYDataset();
        DefaultXYDataset datasetXvsC = new DefaultXYDataset();
        DefaultIntervalXYDataset datasetHistogram = new DefaultIntervalXYDataset();
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
                    frame[i].getContentPane().add(new ChartPanel(ChartFactory.createXYBarChart("histogram", "x", false, "c", datasetHistogram)));
                    break;
            }
            frame[i].pack();
            frame[i].setVisible(true);
            frame[i].setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        double[][] hist = new double[6][100];
        for (int i = 0; i < hist[0].length; i++) {
            hist[0][i] = -5.0 + i * 0.05;
            hist[1][i] = hist[0][i] - 0.025;
            hist[2][i] = hist[0][i] + 0.025;
        }
        double x = 0;
        for (int y = 0; y < 1 ; y++) {
            for (int i = 0; i < 2; i++) {
                long t = System.currentTimeMillis();
                x = i * 0.1;
                System.out.println("Start calculate");
                CalcG11.G11 g11 = calculate(100, -3 - x, -3 + x);
                double[] root = g11.getXdouble();
                double[] c = g11.getCdouble();
                System.out.println(String.format("Work complete on %d sec", (System.currentTimeMillis() - t) / 1000));
                double[][] d = new double[2][root.length];
                for (int j = 0; j < root.length; j++) {
                    d[1][j] = root[j];
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
                /*double[][] avC = new double[2][averageC.length];
                for (int j = 0; j < averageC.length; j++) {
                    avC[1][j] = averageC[j];
                    avC[0][j] = j;
                }*/
                double[][] lastC = new double[2][c.length];
                for (int j = 0; j < c.length; j++) {
                    lastC[1][j] = c[j];
                    lastC[0][j] = j;
                }
                double[][] xVSc = new double[2][c.length];
                xVSc[0] = d[1];
                xVSc[1] = lastC[1];
                if (i == 1)
                    for (int j = 0; j < c.length; j++) {
                        hist[3][(int) Math.round((xVSc[0][j] + 5) * 20)] += xVSc[1][j];
                    }
                datasetRoots.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), d);
                datasetDistribute.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), p);
                datasetProbabilityDensity.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), f);
                //datasetAverageC.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), avC);
                datasetC.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), lastC);
                datasetXvsC.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), xVSc);
            }
        }
        datasetHistogram.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), hist);
    }

    private CalcG11.G11 calculate(int dimension, double l, double r) throws Exception {
        Random random = new Random();
        Double[] a = random.doubles(dimension + 1).map(d -> (d * (r - l) + l)).boxed().toArray(Double[]::new);
        Double[] b = new Double[dimension];Arrays.fill(b, 1.0);//random.doubles(dimension).toArray();
        CalcG11 g11 = new CalcG11(new TridiagonalMatrix<>(a, b), 50, true);
        return g11.call();
    }

}
