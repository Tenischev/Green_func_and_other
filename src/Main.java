import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.DefaultIntervalXYDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.DefaultXYZDataset;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
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
        //calcGij();
        //calcGijComplex();
        calcGijDecimal();
        //calcG11();
        //calcInvert();
        //calcTridiagonal();
        System.out.println("End");
    }

    private void calcGijComplex() throws Exception {
        Random random = new Random();
        int dimension = 10;
        Complex[] a = new Complex[dimension + 1];Arrays.fill(a, new Complex(-3, 0));//random.doubles(dimension + 1).map(d -> (d * (r - l) + l)).boxed().toArray(Complex[]::new);
        Complex[] b = new Complex[dimension];Arrays.fill(b, new Complex(1, 0));//random.doubles(dimension).toArray();
        TridiagonalMatrix<Complex> matrix = new TridiagonalMatrix<>(a, b);
        //CalcGij<Complex> taskGij = new CalcGijComplex(matrix, 5, 5, 50, true);
        CalcGij<Complex> taskGij = new CalcGijComplex(matrix, 5, 5, 50, true);
        Gij<Complex> gij = taskGij.call();
        System.out.println(gij.getValue(Complex.ZERO).toString());
    }

    private void calcGijDecimal() throws Exception {
        Random random = new Random();
        int dimension = 10;
        BigDecimal[] a = new BigDecimal[dimension + 1];Arrays.fill(a, new BigDecimal(-3));//random.doubles(dimension + 1).map(d -> (d * (r - l) + l)).boxed().toArray(BigDecimal[]::new);
        BigDecimal[] b = new BigDecimal[dimension];Arrays.fill(b, new BigDecimal(1));//random.doubles(dimension).toArray();
        TridiagonalMatrix<BigDecimal> matrix = new TridiagonalMatrix<>(a, b);
        //CalcGij<Complex> taskGij = new CalcGijComplex(matrix, 5, 5, 50, true);
        CalcGij<BigDecimal> taskGij = new CalcGijDecimal(matrix, 0, 0, 50, true);
        Gij<BigDecimal> gij = taskGij.call();
        System.out.println(gij.getValue(BigDecimal.ZERO));
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

    /*private void calcGij() throws Exception {
        DefaultXYDataset datasetRoots = new DefaultXYDataset();
        DefaultXYDataset datasetDistribute = new DefaultXYDataset();
        DefaultXYDataset datasetProbabilityDensity = new DefaultXYDataset();
        DefaultXYDataset datasetAverageC = new DefaultXYDataset();
        DefaultXYDataset datasetC = new DefaultXYDataset();
        DefaultXYDataset datasetXvsC = new DefaultXYDataset();
        DefaultXYDataset datasetG = new DefaultXYDataset();
        DefaultIntervalXYDataset datasetHistogram = new DefaultIntervalXYDataset();
        JFrame[] frame = new JFrame[8];
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
                case 7 :
                    frame[i].getContentPane().add(new ChartPanel(ChartFactory.createScatterPlot("G_1_i(w) with empty cell 3", "w", "value", datasetG)));
                    break;
            }
            frame[i].pack();
            frame[i].setVisible(true);
            frame[i].setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        double[][] hist = new double[6][120];
        for (int i = 0; i < hist[0].length; i++) {
            hist[0][i] = -6.0 + i * 0.05;
            hist[1][i] = hist[0][i] - 0.025;
            hist[2][i] = hist[0][i] + 0.025;
        }
        double x = 0;
        for (int y = 0; y < 1; y++) {
            for (int i = 0; i < 1; i++) {
                double[][] gValue = new double[2][12];
                long t = System.currentTimeMillis();
                x = 0.1;
                System.out.println("Start calculate");
                Gij g11 = calculate(300, -3 - x, -3 + x, 50 - (i / 2) - (i % 2), 50 + (i / 2), 1);
                //Gij g11_2 = calculate(100, -3 - x, -3 + x, i, 0);
                double[] root = g11.getXdouble();
                double[] c = g11.getCdouble();
                for (int j = 0; j < c.length; j++) {
                    if (j > 0)
                        System.out.printf("+");
                    System.out.printf("(%f/(x-(%f)))", c[j], root[j]);
                }
                System.out.println();
                //root = Arrays.copyOfRange(root, 0, root.length / 2);
                //c = Arrays.copyOfRange(c, 0, c.length / 2);
                System.out.println(String.format("Work complete on %d sec", (System.currentTimeMillis() - t) / 1000));
                for (int j = 0; j < 1; j++) {
                    gValue[0][j] = i;
                    gValue[1][j] = g11.getValue(j);
                    System.out.format("Value on w=%d is %f\n", j, gValue[1][j]);
                }
                datasetG.addSeries(String.format("G_1_%d(w)", i + 1), gValue);
                double[][] d = new double[2][root.length];
                for (int j = 0; j < root.length; j++) {
                    d[1][j] = root[j];
                    d[0][j] = j;
                }
                //double[] distribution = generateDistributionFunc(d[1]);
                *//*double[][] p = new double[2][root.length];
                for (int j = 0; j < root.length; j++) {
                    p[1][j] = (double) (j + 1) / root.length;
                    p[0][j] = d[1][root.length - j - 1];
                }
                double[][] f = new double[2][root.length - 1];
                for (int j = 0; j < root.length - 1; j++) {
                    f[1][j] = (p[1][j + 1] - p[1][j]) / (p[0][j + 1] - p[0][j]);
                    f[0][j] = (p[0][j] + p[0][j + 1]) / 2.0;
                }*//*
                *//*double[][] avC = new double[2][averageC.length];
                for (int j = 0; j < averageC.length; j++) {
                    avC[1][j] = averageC[j];
                    avC[0][j] = j;
                }*//*
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
                        hist[3][(int) Math.round((xVSc[0][j] + 6) * 20)] += xVSc[1][j];
                    }
                datasetRoots.addSeries(String.format("E∈(%f;%f) V=%d id%d", -3 - x, -3 + x, 1, i), d);
                //datasetDistribute.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), p);
                //datasetProbabilityDensity.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), f);
                //datasetAverageC.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), avC);
                datasetC.addSeries(String.format("E∈(%f;%f) V=%d id%d", -3 - x, -3 + x, 1, i), lastC);
                datasetXvsC.addSeries(String.format("E∈(%f;%f) V=%d id%d", -3 - x, -3 + x, 1, i), xVSc);
            }
        }
        datasetHistogram.addSeries(String.format("E∈(%f;%f) V=%d", -3 - x, -3 + x, 1), hist);
    }

    private Gij calculate(int dimension, double l, double r, int i, int j, double bb) {
        Random random = new Random();
        Double[] a = random.doubles(dimension + 1).map(d -> (d * (r - l) + l)).boxed().toArray(Double[]::new);
        Double[] a2 = new Double[a.length];
        Arrays.fill(a2, -3.0);
        Double[] b = new Double[dimension];Arrays.fill(b, bb);//random.doubles(dimension).toArray();
        //b[2] = 0.0;
        try {
            boolean f = true;
            for (int k = 0; k < 10; k++) {
                CalcGij gij = new CalcGij<>(new TridiagonalMatrix<>(a, b), dimension / 2 - (k / 2) - (k % 2), dimension / 2 + (k / 2), 25, true);
                CalcGij gij2 = new CalcGij<>(new TridiagonalMatrix<>(a2, b), dimension / 2 - (k / 2) - (k % 2), dimension / 2 + (k / 2), 25, true);
                double w1 = gij.call().getValue(0);
                double w2 = gij2.call().getValue(0);
                f = w1 > w2;
                System.out.format("L=%d \tg=%f \ttheor=%f\n", k, w1, w2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //new CalcG11<>(new TridiagonalMatrix<>(a, b), 25, true);
        *//*for (int k = 0; k < 3; k++) {
            System.out.format("Clear value on w=%d is %f\n", k, gij.sayAns(new BigDecimal(k)));
        }*//*
        CalcGij gij = new CalcGij<>(new TridiagonalMatrix<>(a, b), i, j, 25, true);
        try {
            return gij.call();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

}
