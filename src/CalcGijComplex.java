import java.util.Arrays;

/**
 * Created by kris13 on 30.04.16.
 */
public class CalcGijComplex extends CalcGij<Complex> {
    public CalcGijComplex(TridiagonalMatrix<Complex> matrix, int i, int j, int accurancy, boolean useParallel) {
        super(matrix, i, j, accurancy, useParallel);
    }

    @Override
    protected CalcG11<Complex> getCalcG11ByImpl(TridiagonalMatrix<Complex> h0, int eps, boolean b) {
        return new CalcG11Complex(h0, eps, b);
    }

    @Override
    protected Gij<Complex> getGijByImpl(G11<Complex> g11_0, G11<Complex> g11_1, int eps) {
        return GijComplex.mergreG11(g11_0, g11_1, eps);
    }

    @Override
    protected TridiagonalMatrix<Complex> changeBasis(TridiagonalMatrix<Complex> matrix, double[] y) {
        Complex[] y1 = new Complex[y.length];
        for (int i = 0; i < y.length; i++) {
            y1[i] = new Complex(y[i], 0);
        }
        int dim = 0;
        Complex[] a = new Complex[y1.length];
        double[] b = new double[y1.length - 1];
        Complex[] y2;
        Complex[] temp;
        try {
            temp = UtilsVector.multiplyToMatrix(matrix, y1);
            a[0] = UtilsVector.multiply(y1, temp);
            dim++;
            temp = UtilsVector.subtract(temp, UtilsVector.multiplyToValue(y1, a[0]));
            b[0] = UtilsVector.getSecondNorm(temp);
            if (Math.abs(b[0]) - 10E-15 <= 0)
                throw new IndexOutOfBoundsException("b is zero");
            y2 = UtilsVector.multiplyToValue(temp, 1.0 / b[0]);

            for (int i = 1; i < y1.length - 1; i++) {
                temp = UtilsVector.multiplyToMatrix(matrix, y2);
                a[i] = UtilsVector.multiply(y2, temp);
                dim++;
                temp = UtilsVector.subtract(temp,
                        UtilsVector.add(UtilsVector.multiplyToValue(y1, b[i - 1]), UtilsVector.multiplyToValue(y2, a[i])));
                b[i] = UtilsVector.getSecondNorm(temp);
                if (Math.abs(b[i]) - 10E-15 <= 0)
                    throw new IndexOutOfBoundsException("b is zero");
                y1 = y2;
                y2 = UtilsVector.multiplyToValue(temp, 1.0 / b[i]);
            }
            a[y1.length - 1] = UtilsVector.multiply(y2, UtilsVector.multiplyToMatrix(matrix, y2));
        } catch (IndexOutOfBoundsException e) {
            a = Arrays.copyOfRange(a, 0, dim);
            b = Arrays.copyOfRange(b, 0, dim - 1);
        }
        Complex[] b2 = new Complex[b.length];
        for (int i = 0; i < b.length; i++) {
            b2[i] = new Complex(b[i], 0);
        }
        return new TridiagonalMatrix<>(a, b2);
    }
}
