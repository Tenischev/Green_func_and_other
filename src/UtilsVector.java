import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

    public static Double[] add(Double[] a, Double[] b) {
        Double[] vect = new Double[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i] + b[i];
        }
        return vect;
    }

    public static Complex[] add(Complex[] a, Complex[] b) {
        Complex[] vect = new Complex[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i].add(b[i]);
        }
        return vect;
    }

    public static BigDecimal[] add(BigDecimal[] a, BigDecimal[] b, MathContext context) {
        BigDecimal[] vect = new BigDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i].add(b[i], context);
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

    public static Double[] subtract(Double[] a, Double[] b) {
        Double[] vect = new Double[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i] - b[i];
        }
        return vect;
    }

    public static Complex[] subtract(Complex[] a, Complex[] b) {
        Complex[] vect = new Complex[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i].subtract(b[i]);
        }
        return vect;
    }

    public static BigDecimal[] subtract(BigDecimal[] a, BigDecimal[] b, MathContext context) {
        BigDecimal[] vect = new BigDecimal[a.length];
        for (int i = 0; i < a.length; i++) {
            vect[i] = a[i].subtract(b[i], context);
        }
        return vect;
    }

    public static Complex multiply(Complex[] a, Complex[] b) {
        Complex ans = Complex.ZERO;
        for (int i = 0; i < a.length; i++) {
            ans = ans.add(a[i].multiply(b[i]));
        }
        return ans;
    }

    public static <T extends Number> T multiply(T[] a, T[] b) {
        Number ans = 0d;
        for (int i = 0; i < a.length; i++) {
            ans = ans.doubleValue() + a[i].doubleValue() * b[i].doubleValue();
        }
        return (T) ans;
    }

    public static double multiply(double[] a, double[] b) {
        double ans = 0;
        for (int i = 0; i < a.length; i++) {
            ans += a[i] * b[i];
        }
        return ans;
    }

    public static BigDecimal multiply(BigDecimal[] a, BigDecimal[] b, MathContext context) {
        BigDecimal ans = BigDecimal.ZERO;
        for (int i = 0; i < a.length; i++) {
            ans = ans.add(a[i].multiply(b[i], context), context);
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

    public static Double getSecondNorm(Double[] vect) {
        Double ans = 0.0;
        for (Double aVect : vect) {
            ans += aVect * aVect;
        }
        return Math.sqrt(ans);
    }

    public static BigDecimal getSecondNorm(BigDecimal[] vect, MathContext mathContext) {
        BigDecimal ans = BigDecimal.ZERO;
        for (BigDecimal aVect : vect) {
            ans = ans.add(aVect.multiply(aVect, mathContext), mathContext);
        }
        return bigSqrt(ans);
    }

    public static Complex getSecondNorm(Complex[] vect) {
        Complex ans = Complex.ZERO;
        for (Complex aVect : vect) {
            ans = ans.add(aVect.multiply(aVect));
        }
        return new Complex(bigSqrt(ans.getImage() == 0d ? ans.getRealBig() : ans.bigValue()), BigDecimal.ZERO);
    }

    public static double[] multiplyToValue(double[] a, double val) {
        double[] vect = new double[a.length];
        for (int i = 0; i < vect.length; i++) {
            vect[i] = a[i]*val;
        }
        return vect;
    }

    public static Double[] multiplyToValue(Double[] a, Double val) {
        Double[] vect = new Double[a.length];
        for (int i = 0; i < vect.length; i++) {
            vect[i] = a[i] * val;
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

    public static BigDecimal[] multiplyToValue(BigDecimal[] a, BigDecimal val, MathContext context) {
        BigDecimal[] vect = new BigDecimal[a.length];
        for (int i = 0; i < vect.length; i++) {
            vect[i] = a[i].multiply(val, context);
        }
        return vect;
    }

    public static Complex[] multiplyToValue(Complex[] a, Complex val) {
        Complex[] vect = new Complex[a.length];
        for (int i = 0; i < vect.length; i++) {
            vect[i] = a[i].multiply(val);
        }
        return vect;
    }

    public static <T extends Number> T[] multiplyToMatrix(Matrix<T> matrix, T[] v) {
        if (matrix instanceof TridiagonalMatrix) {
            TridiagonalMatrix<T> tridiagonalMatrix = (TridiagonalMatrix<T>) matrix;
            return multiplyToMatrix(tridiagonalMatrix, v);
        } else if (matrix instanceof CheatMatrix) {
            CheatMatrix<T> cheatMatrix = (CheatMatrix<T>) matrix;
            return multiplyToMatrix(cheatMatrix, v);
        } else {
            throw new IllegalArgumentException("What is this the Matrix?");
        }
    }

    public static Complex[] multiplyToMatrix(TridiagonalMatrix<Complex> matrix, Complex[] v) {
        Complex[] ans = new Complex[v.length];
        Complex[] a = matrix.getMainDiagonal();
        Complex[] b;
        Complex[] c;
        if (matrix.isSymmetric()) {
            b = matrix.getOffDiagonal();
            c = b;
        } else {
            b = matrix.getUpDiagonal();
            c = matrix.getDownDiagonal();
        }
        for (int i = 0; i < v.length; i++) {
            ans[i] = Complex.ZERO;
            if (i - 1 >= 0)
                ans[i] = ans[i].add(c[i - 1].multiply(v[i - 1]));
            ans[i] = ans[i].add(a[i].multiply(v[i]));
            if (i + 1 < v.length)
                ans[i] = ans[i].add(b[i].multiply(v[i + 1]));
        }
        return ans;
    }

    public static Complex[] multiplyToMatrix(CheatMatrix<Complex> matrix, Complex[] v) {
        Complex[] ans = new Complex[v.length];
        for (int i = 0; i < matrix.getDimensional(); i++) {
            ans[i] = Complex.ZERO;
            List<Complex> row = matrix.getRow(i);
            List<Integer> pos = matrix.getPositions(i);
            for (int j = 0; j < pos.size(); j++) {
                ans[i] = ans[i].add(row.get(j).multiply(v[pos.get(j)]));
            }
        }
        return ans;
    }

    public static BigDecimal[] multiplyToMatrix(CheatMatrix<BigDecimal> matrix, BigDecimal[] v) {
        BigDecimal[] ans = new BigDecimal[v.length];
        for (int i = 0; i < matrix.getDimensional(); i++) {
            ans[i] = BigDecimal.ZERO;
            List<BigDecimal> row = matrix.getRow(i);
            List<Integer> pos = matrix.getPositions(i);
            for (int j = 0; j < pos.size(); j++) {
                ans[i] = ans[i].add(row.get(j).multiply(v[pos.get(j)]));
            }
        }
        return ans;
    }

    public static <T extends Number> T[] multiplyToMatrix(CheatMatrix<T> matrix, T[] v) {
        if (v instanceof Complex[]) {
            CheatMatrix<Complex> complexCheatMatrix = (CheatMatrix<Complex>) matrix;
            Complex[] complexV = (Complex[]) v;
            return (T[]) multiplyToMatrix(complexCheatMatrix, complexV);
        }
        if (v instanceof BigDecimal[]) {
            CheatMatrix<BigDecimal> decimalCheatMatrix = (CheatMatrix<BigDecimal>) matrix;
            BigDecimal[] decimals = (BigDecimal[]) v;
            return (T[]) multiplyToMatrix(decimalCheatMatrix, decimals);
        }
        Double[] ans = new Double[v.length];
        for (int i = 0; i < v.length; i++) {
            ans[i] = 0d;
            List<T> row = matrix.getRow(i);
            List<Integer> pos = matrix.getPositions(i);
            for (int j = 0; j < pos.size(); j++) {
                ans[i] += row.get(j).doubleValue() * v[pos.get(j)].doubleValue();
            }
        }
        return (T[]) ans;
    }

    public static <T extends Number> T[] multiplyToMatrix(TridiagonalMatrix<T> matrix, T[] v) {
        if (v instanceof Complex[]) {
            TridiagonalMatrix<Complex> complexTridiagonalMatrix = (TridiagonalMatrix<Complex>) matrix;
            Complex[] complexV = (Complex[]) v;
            return (T[]) multiplyToMatrix(complexTridiagonalMatrix, complexV);
        }
        if (v instanceof BigDecimal[]) {
            BigDecimal[] decV = (BigDecimal[]) v;
            return (T[]) multiplyToMatrix(matrix, decV);
        }
        Double[] ans = new Double[v.length];
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
            ans[i] = 0d;
            if (i - 1 >= 0)
                ans[i] += c[i - 1].doubleValue() * v[i - 1].doubleValue();
            ans[i] += a[i].doubleValue() * v[i].doubleValue();
            if (i + 1 < v.length)
                ans[i] += b[i].doubleValue() * v[i + 1].doubleValue();
        }
        return (T[]) ans;
    }

    public static BigDecimal[] multiplyToMatrix(Matrix<BigDecimal> matrix, BigDecimal[] y1, MathContext context) {
        if (matrix instanceof TridiagonalMatrix) {
            TridiagonalMatrix<BigDecimal> tridiagonalMatrix = (TridiagonalMatrix<BigDecimal>) matrix;
            return multiplyToMatrix(tridiagonalMatrix, y1, context);
        }
        if (matrix instanceof CheatMatrix) {
            CheatMatrix<BigDecimal> tridiagonalMatrix = (CheatMatrix<BigDecimal>) matrix;
            return multiplyToMatrix(tridiagonalMatrix, y1, context);
        }
        throw new IllegalArgumentException("Wrong matrix type");
    }

    public static BigDecimal[] multiplyToMatrix(TridiagonalMatrix<BigDecimal> matrix, BigDecimal[] v, MathContext context) {
        BigDecimal[] ans = new BigDecimal[v.length];
        BigDecimal[] a = matrix.getMainDiagonal();
        BigDecimal[] b;
        BigDecimal[] c;
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
                ans[i] = ans[i].add(v[i - 1].multiply(c[i - 1], context), context);
            ans[i] = ans[i].add(v[i].multiply(a[i], context), context);
            if (i + 1 < v.length)
                ans[i] = ans[i].add(v[i + 1].multiply(b[i], context), context);
        }
        return ans;
    }

    public static BigDecimal[] multiplyToMatrix(CheatMatrix<BigDecimal> matrix, BigDecimal[] v, MathContext context) {
        BigDecimal[] ans = new BigDecimal[v.length];
        for (int i = 0; i < matrix.getDimensional(); i++) {
            ans[i] = BigDecimal.ZERO;
            List<BigDecimal> row = matrix.getRow(i);
            List<Integer> pos = matrix.getPositions(i);
            for (int j = 0; j < pos.size(); j++) {
                ans[i] = ans[i].add(row.get(j).multiply(v[pos.get(j)], context), context);
            }
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

    public static Double[] toZeros(Double[] vect, double eps) {
        Double[] ans = new Double[vect.length];
        for (int i = 0; i < vect.length; i++) {
            if (Math.abs(vect[i]) < eps) {
                ans[i] = 0.0;
            } else {
                ans[i] = vect[i];
            }
        }
        return ans;
    }

    public static Complex[] toZeros(Complex[] vect, double eps) {
        Complex[] ans = new Complex[vect.length];
        for (int i = 0; i < vect.length; i++) {
            if (Math.abs(vect[i].doubleValue()) < eps) {
                ans[i] = Complex.ZERO;
            } else {
                ans[i] = vect[i];
            }
        }
        return ans;
    }

    public static Complex[] onI(Complex[] x) {
        Complex[] ans = new Complex[x.length];
        for (int i = 0; i < x.length; i++) {
            ans[i] = x[i].onI();
        }
        return ans;
    }

    public static BigDecimal[] toZeros(BigDecimal[] vect, MathContext context) {
        for (int i = 0; i < vect.length; i++) {
            if (vect[i].scale() > context.getPrecision()) {
                vect[i] = vect[i].setScale(context.getPrecision(), context.getRoundingMode());
            }
        }
        return vect;
    }

    private static final BigDecimal SQRT_DIG = new BigDecimal(500);
    private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());

    /**
     * Private utility method used to compute the square root of a BigDecimal.
     *
     * @author Luciano Culacciatti
     * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
     */
    private static BigDecimal sqrtNewtonRaphson  (BigDecimal c, BigDecimal xn, BigDecimal precision){
        BigDecimal fx = xn.pow(2).add(c.negate());
        BigDecimal fpx = xn.multiply(new BigDecimal(2));
        BigDecimal xn1 = fx.divide(fpx,2*SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
        xn1 = xn.add(xn1.negate());
        BigDecimal currentSquare = xn1.pow(2);
        BigDecimal currentPrecision = currentSquare.subtract(c);
        currentPrecision = currentPrecision.abs();
        if (currentPrecision.compareTo(precision) <= -1){
            return xn1;
        }
        return sqrtNewtonRaphson(c, xn1, precision);
    }

    /**
     * Uses Newton Raphson to compute the square root of a BigDecimal.
     *
     * @author Luciano Culacciatti
     * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
     */
    public static BigDecimal bigSqrt(BigDecimal c){
        return sqrtNewtonRaphson(c,new BigDecimal(1),new BigDecimal(1).divide(SQRT_PRE));
    }
}
