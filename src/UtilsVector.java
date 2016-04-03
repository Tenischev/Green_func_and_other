import java.math.BigDecimal;

/**
 * Created by kris13 on 26.03.16.
 */
public class UtilsVector {
    public static double[] add(double[] a, double[] b) {
        double[] vect = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i] + b[i];
        }
        return vect;
    }

    public static BigDecimal[] add(BigDecimal[] a, BigDecimal[] b) {
        BigDecimal[] vect = new BigDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i].add(b[i]);
        }
        return vect;
    }

    public static double[] subtract(double[] a, double[] b) {
        double[] vect = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i] - b[i];
        }
        return vect;
    }

    public static BigDecimal[] subtract(BigDecimal[] a, BigDecimal[] b) {
        BigDecimal[] vect = new BigDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i].subtract(b[i]);
        }
        return vect;
    }

    public static double multiply(double[] a, double[] b) {
        double ans = 0;
        for (int i = 0; i < a.length; i++) {
            ans += a[i] * b[i];
        }
        return ans;
    }

    public static BigDecimal multiply(BigDecimal[] a, BigDecimal[] b) {
        BigDecimal ans = BigDecimal.ZERO;
        for (int i = 0; i < a.length; i++) {
            ans = ans.add(a[i].multiply(b[i]));
        }
        return ans;
    }

    public static double getSecondNorm(double[] vect) {
        double ans = 0;
        for (double aVect : vect) {
            ans += aVect * aVect;
        }
        return Math.sqrt(ans);
    }

    public static BigDecimal getSecondNorm(BigDecimal[] vect) {
        BigDecimal ans = BigDecimal.ZERO;
        for (BigDecimal aVect : vect) {
            ans = ans.add(aVect.multiply(aVect));
        }
        return new BigDecimal(Math.sqrt(ans.doubleValue()));
    }

    public static double[] multiplyToValue(double[] a, double val) {
        double[] vect = new double[a.length];
        for (int i = 0; i < vect.length; i++) {
            vect[i] = a[i]*val;
        }
        return vect;
    }

    public static BigDecimal[] multiplyToValue(BigDecimal[] a, double val) {
        BigDecimal[] vect = new BigDecimal[a.length];
        BigDecimal val1 = new BigDecimal(val);
        for (int i = 0; i < vect.length; i++) {
            vect[i] = a[i].multiply(val1);
        }
        return vect;
    }

    public static BigDecimal[] multiplyToValue(BigDecimal[] a, BigDecimal val) {
        BigDecimal[] vect = new BigDecimal[a.length];
        for (int i = 0; i < vect.length; i++) {
            vect[i] = a[i].multiply(val);
        }
        return vect;
    }

    public static <T extends Number> double[] multiplyToMatrix(TridiagonalMatrix<T> matrix, double[] v) {
        double[] ans = new double[v.length];
        Number[] a = matrix.getMainDiagonal();
        Number[] b;
        Number[] c;
        if (matrix.isSymmetric()) {
            b = matrix.getOffDiagonal();
            c = b;
        } else {
            b = matrix.getUpDiagonal();
            c = matrix.getDownDiagonal();
        }
        for (int i = 0; i < v.length; i++) {
            if (i - 1 >= 0)
                ans[i] += c[i - 1].doubleValue() * v[i - 1];
            ans[i] += a[i].doubleValue() * v[i];
            if (i + 1 < v.length)
                ans[i] += b[i].doubleValue() * v[i + 1];
        }
        return ans;
    }

    public static <T extends Number> BigDecimal[] multiplyToMatrix(TridiagonalMatrix<T> matrix, BigDecimal[] v) {
        BigDecimal[] ans = new BigDecimal[v.length];
        Number[] a = matrix.getMainDiagonal();
        Number[] b;
        Number[] c;
        if (matrix.isSymmetric()) {
            b = matrix.getOffDiagonal();
            c = b;
        } else {
            b = matrix.getUpDiagonal();
            c = matrix.getDownDiagonal();
        }
        for (int i = 0; i < v.length; i++) {
            ans[i] = BigDecimal.ZERO;
            if (i - 1 >= 0)
                ans[i] = ans[i].add(v[i - 1].multiply(new BigDecimal(c[i - 1].doubleValue())));
            ans[i] = ans[i].add(v[i].multiply(new BigDecimal(a[i].doubleValue())));
            if (i + 1 < v.length)
                ans[i] = ans[i].add(v[i + 1].multiply(new BigDecimal(b[i].doubleValue())));
        }
        return ans;
    }

    public static <T extends Number> double[] multiplyToMatrix(T[][] matrix, double[] v) {
        double[] ans = new double[v.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                ans[i] += matrix[i][j].doubleValue() * v[j];
            }
        }
        return ans;
    }

    public static <T extends Number> BigDecimal[] multiplyToMatrix(T[][] matrix, BigDecimal[] v) {
        BigDecimal[] ans = new BigDecimal[v.length];
        for (int i = 0; i < matrix.length; i++) {
            ans[i] = BigDecimal.ZERO;
            for (int j = 0; j < matrix.length; j++) {
                ans[i] = ans[i].add(v[j].multiply(new BigDecimal(matrix[i][j].doubleValue())));
            }
        }
        return ans;
    }
}
