import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by kris13 on 30.04.16.
 */
public class CalcGijDecimal extends CalcGij<BigDecimal> {

    public CalcGijDecimal(TridiagonalMatrix<BigDecimal> matrix, int i, int j, int accurancy, boolean useParallel) {
        super(matrix, i, j, accurancy, useParallel);
    }

    @Override
    protected CalcG11<BigDecimal> getCalcG11ByImpl(TridiagonalMatrix<BigDecimal> h0, int eps, boolean b) {
        return new CalcG11Decimal(matrix, eps, b);
    }

    @Override
    protected Gij<BigDecimal> getGijByImpl(G11<BigDecimal> g11_0, G11<BigDecimal> g11_1, int eps) {
        return GijDecimal.mergreG11(g11_0, g11_1, eps);
    }

    @Override
    protected TridiagonalMatrix<BigDecimal> changeBasis(TridiagonalMatrix<BigDecimal> matrix, double[] y1) {
        int dim = 0;
        Double[] a = new Double[y1.length];
        Double[] b = new Double[y1.length - 1];
        double[] y2;
        double[] temp;
        try {
            temp = UtilsVector.toZeros(UtilsVector.multiplyToMatrix(matrix, y1), 10E-15);
            a[0] = UtilsVector.multiply(y1, temp);
            dim++;
            temp = UtilsVector.toZeros(UtilsVector.subtract(temp, UtilsVector.multiplyToValue(y1, a[0])), 10E-15);
            b[0] = UtilsVector.getSecondNorm(temp);
            if (Math.abs(b[0]) - 10E-15 <= 0)
                throw new IndexOutOfBoundsException("b is zero");
            y2 = UtilsVector.toZeros(UtilsVector.multiplyToValue(temp, 1.0 / b[0]), 10E-15);

            for (int i = 1; i < y1.length - 1; i++) {
                temp = UtilsVector.toZeros(UtilsVector.multiplyToMatrix(matrix, y2), 10E-15);
                a[i] = UtilsVector.multiply(y2, temp);
                dim++;
                temp = UtilsVector.toZeros(UtilsVector.subtract(temp, UtilsVector.add(UtilsVector.multiplyToValue(y1, b[i - 1]), UtilsVector.multiplyToValue(y2, a[i]))), 10E-15);
                b[i] = UtilsVector.getSecondNorm(temp);
                if (Math.abs(b[i]) - 10E-15 <= 0)
                    throw new IndexOutOfBoundsException("b is zero");
                y1 = y2;
                y2 = UtilsVector.toZeros(UtilsVector.multiplyToValue(temp, 1.0 / b[i]), 10E-15);
            }
            a[y1.length - 1] = UtilsVector.multiply(y2, UtilsVector.multiplyToMatrix(matrix, y2));
        } catch (IndexOutOfBoundsException e) {
            a = Arrays.copyOfRange(a, 0, dim);
            b = Arrays.copyOfRange(b, 0, dim - 1);
        }
        try {
            BigDecimal[] a2 = new BigDecimal[a.length];
            for (int i = 0; i < a.length; i++) {
                a2[i] = new BigDecimal(a[i]);
            }
            BigDecimal[] b2 = new BigDecimal[b.length];
            for (int i = 0; i < b.length; i++) {
                b2[i] = new BigDecimal(b[i]);
            }
            return new TridiagonalMatrix<>(a2, b2);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
