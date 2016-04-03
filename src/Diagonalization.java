import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Transform matrix to third diagonal view
 * Use Lanczos algorithm
 *
 * Created by kris13 on 12.03.16.
 */
public class Diagonalization<T extends Number> implements Callable<TridiagonalMatrix<Double>> {
    private final T[][] matrix;

    public Diagonalization(T[][] matrix) {
        this.matrix = matrix;
    }

    /*
    If a good guess for the wanted eigenvector is available, use it.
    For instance, if, for a discretized partial differential equation,
    it is known that the wanted eigenvector is smooth with respect to
    the grid, one could start with a vector with all ones. In other
    cases choose a random direction, for instance, one consisting
    of normally distributed random numbers.
     */

    @Override
    public TridiagonalMatrix<Double> call() throws Exception {
        Random random = new Random();
        double[] a = new double[matrix.length];
        double[] v = new double[matrix.length];
        double[] b = new double[matrix.length];

        double[] q = random.doubles((long) matrix.length).toArray(); // new double[matrix.length]; Arrays.fill(q, 1.0);
        double norm = UtilsVector.getSecondNorm(q);
        System.out.println(norm);
        for (int j = 0; j < matrix.length; j++) {
            q[j] = q[j] / norm;
        }
        double[] r = UtilsVector.multiplyToMatrix(matrix, q);
        a[0] = UtilsVector.multiply(q, r);
        for (int j = 0; j < matrix.length; j++) {
            r[j] = r[j] - a[0] * q[j];
        }
        b[0] = UtilsVector.getSecondNorm(r);
        for (int i = 1; i < matrix.length; i++) {
            v = Arrays.copyOf(q, matrix.length);
            for (int j = 0; j < matrix.length; j++) {
                q[j] = r[j] / b[i - 1];
            }
            r = UtilsVector.multiplyToMatrix(matrix, q);
            for (int j = 0; j < matrix.length; j++) {
                r[j] = r[j] - b[i - 1] * v[j];
            }
            a[i] = UtilsVector.multiply(q, r);
            for (int j = 0; j < matrix.length; j++) {
                r[j] = r[j] - a[i] * q[j];
            }
            b[i] = UtilsVector.getSecondNorm(r);
            //System.out.println(b[i]);
        }
        return new TridiagonalMatrix<>(Arrays.stream(a).boxed().toArray(Double[]::new)
                , Arrays.stream(Arrays.copyOf(b, matrix.length - 1)).boxed().toArray(Double[]::new));
    }
}
