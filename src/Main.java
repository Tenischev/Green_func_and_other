import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.DefaultIntervalXYDataset;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.*;

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
        //moduleOfGreenFunc();
        calcGijComplex();
        //calcG11();
        //calcInvert();
        //calcTridiagonal();
        System.out.println("End");
    }

    private Gij calcGij() throws Exception {
        Random random = new Random();
        int dimension = 100;
        int[] l = {6, 6, 6, 4, 4, 4};
        int[] r = {7, 7, 7, 7, 7, 7};
        double[] V = {0.7, 0.6, 0.8, 0.7, 0.6, 0.8};
        PrintStream printStream = new PrintStream(new File(".", "Generators_for_Ira.txt"));
        for (int i = 0; i < 6; i++) {
            printStream.printf("W = %d..%d, V = %f\n", l[i], r[i], V[i]);
            for (int j = 0; j < 10; j++) {
                final int finalI = i;
                Double[] a = random.doubles(dimension + 1).map(d -> (d * (r[finalI] - l[finalI]) + l[finalI])).boxed().toArray(Double[]::new);//new Double[dimension + 1];Arrays.fill(a, -3.0);
                Double[] b = new Double[dimension];Arrays.fill(b, V[finalI]);//random.doubles(dimension).toArray();
                TridiagonalMatrix<Double> matrix = new TridiagonalMatrix<>(a, b);
                CalcG11<Double> taskGij = new CalcG11<>(matrix, 25, true);
                CalcG11.G11 g11 = taskGij.call();
                //System.out.println(gij.getValue(0));
                printStream.println(g11.getRepresent());
            }
            printStream.println();
        }
        printStream.close();
        return null;
    }

    private void calcGijComplex() throws Exception {
        DefaultXYDataset datasetValues = new DefaultXYDataset();
        JFrame frame = new JFrame();
        frame.getContentPane().add(new ChartPanel(ChartFactory.createScatterPlot("", "W/V", "Max(G_00)", datasetValues)));

        Random random = new Random();
        int dimension = 7;
        int l = 20;
        double[][] value2 = new double[2][31];
        double[][] value3 = new double[2][31];
        double repeat = 100;
        for (int i = 0; i < 15; i++) {
            double ave = 0;
            double f = 0;
            double max = 0;
            long start = System.currentTimeMillis();
            double value = 0.5 + 0.5 * i;
            double e = 0;
            double S = 1e-6;
            System.out.println("Run for value " + value);
            for (int j = 0; j < repeat; j++) {

                Matrix<Complex> matrix = CheatMatrix.createCheatMatrix(2, dimension, new Complex(e, S), new Complex(value, 0.0), Complex.ONE);
                //new TridiagonalMatrix<>(a, b);//new CheatMatrix<>(pos, rows);
                int point = (dimension * dimension) / 2;
                CalcGijComplex taskGij = new CalcGijComplex(matrix, point, point, 25, true);
                //CalcGijDecimal taskGij = new CalcGijDecimal(matrix, dimension / 2, dimension / 2, 25, true);
                CalcGijComplex.GijComplex gij = taskGij.call();

                /*CalcG11<Complex> calcG11_1 = new CalcG11<>(gij.r1, 25, true);
                CalcG11<Complex> calcG11_2 = new CalcG11<>(gij.r2, 25, true);
                CalcG11.G11 g11_1 = calcG11_1.call();*/

                Complex val = gij.getReal(Complex.ZERO);
                //System.out.println(val);
                double sq = val.doubleValue();
                ave += sq;
                max = Math.max(max, sq);
                f += Math.abs(sq / (-val.getImage() / Math.PI));
                //System.out.println(val.doubleValue());
                //Complex val = gij.getValue(Complex.ONE);

                //System.out.println(val);
                //value3[0][j] = j * 0.1;
                //double sq = val.doubleValue() * val.doubleValue();
                //value3[1][j] = value3[1][j] + (sq > 4 ? 4 : sq);
                //System.out.println("Step");
            }
            f /= repeat;
            ave /= repeat;
            ave *= 1e-2;
            max *= 1e-2;
            f *= S;
            f /= Math.PI;
            value2[0][i] = value;
            value3[0][i] = value;
            value2[1][i] = Math.min(ave, 4.0);
            value3[1][i] = Math.min(max, 4.0);
            //value3[1][i] = ave;
            System.out.println("Average = " + ave + "; F = " + f + "; Max = " + max);
            System.out.println("Time = " + (System.currentTimeMillis() - start));
        }
        /*double[] c = gij.getCdouble();
        double[] x = gij.getXdouble();
        double[][] value3 = new double[2][c.length];
        for (int i = 0; i < c.length; i++) {
            value3[0][i] = i;
            value3[1][i] = c[i];
            System.out.print(c[i] + " ");
        }
        System.out.println();
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < x.length; i++) {
            max = Math.max(max, x[i]);
            min = Math.min(min, x[i]);
        }
        System.out.printf("Max = %f, Min = %f\n", max, min);*/
        //System.out.printf("%s\n", gij.getReal(Complex.ONE));
        datasetValues.addSeries("Ave", value2);
        datasetValues.addSeries("Max", value3);
        //datasetValues.addSeries(String.format("Re(G)", j), value3);
        //datasetValues.addSeries(String.format("Im(G) %s", j==0?"":"pertr"), value2);
        //datasetValues.addSeries(String.format("C on %d", l), value3);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

     /*Collection<Complex>[] rows = new ArrayList[(dimension + 1) * (dimension + 1)];
                int[] pos = new int[rows.length];
                for (int i = 0; i < dimension + 1; i++) {
                    for (int k = 0; k < dimension + 1; k++) {
                        rows[i * (dimension + 1) + k] = new ArrayList<>();
                        if (i != 0) {
                            rows[i * (dimension + 1) + k].add(Complex.ONE);
                            pos[i * (dimension + 1) + k]++;
                        }
                        if (k != 0) {
                            rows[i * (dimension + 1) + k].add(Complex.ONE);
                            pos[i * (dimension + 1) + k]++;
                        }
                        rows[i * (dimension + 1) + k].add(new Complex((0.5 - random.nextDouble()) * (j * 0.1), 0));
                        if (i != dimension) {
                            rows[i * (dimension + 1) + k].add(Complex.ONE);
                        }
                        if (k != dimension) {
                            rows[i * (dimension + 1) + k].add(Complex.ONE);
                        }
                    }
                }*/
                /*Collection<Complex>[] rows = new ArrayList[(dimension + 1)];
                int[] pos = new int[rows.length];
                for (int i = 0; i < dimension + 1; i++) {
                    rows[i] = new ArrayList<>();
                    if (i != 0) {
                        rows[i].add(Complex.ONE);
                        pos[i]++;
                    }
                    rows[i].add(new Complex((0.5 - random.nextDouble()) * (j * 0.1), 0));
                    if (i != dimension) {
                        rows[i].add(Complex.ONE);
                    }
                }*/

    private void moduleOfGreenFunc() throws Exception {
        Random random = new Random();
        int dimension = 8;
        Complex[] a = new Complex[dimension + 1];//Arrays.fill(a, new Complex(-3.0, -0.001));
        for (int i = 0; i < a.length; i++) {
            a[i] = new Complex(-3.0 + (0.5 - random.nextDouble()) / 2.0, 0);
            System.out.printf("%f\t", a[i].doubleValue());
        }
        System.out.println("\n");
        Complex[] b = new Complex[dimension];
        Arrays.fill(b, Complex.ONE);

        TridiagonalMatrix<Complex> matrix = new TridiagonalMatrix<>(a, b);


        Complex[][] ans = new Complex[dimension + 1][dimension + 1];
        for (int i = 0; i <= dimension; i++) {
            for (int j = 0; j <= dimension; j++) {
                CalcGijComplex taskGij = new CalcGijComplex(matrix, i, j, 25, true);
                CalcGijComplex.GijComplex gij = taskGij.call();

                Complex val = gij.getReal(Complex.ONE);
                ans[i][j] = val;
                System.out.printf("%f\t", val.doubleValue());
            }
            System.out.println();/*
            double sum = 0;
            for (int j = 0; j < dimension; j++) {
                sum += ans[i][j].doubleValue();
            }
            System.out.println(sum);*/
        }
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
