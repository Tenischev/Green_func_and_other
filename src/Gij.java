import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by kris13 on 26.03.16.
 */
public class Gij {
    private final BigDecimal[] c;
    private final BigDecimal[] x;
    private final int eps;

    public Gij(CalcG11.G11 g11_0, CalcG11.G11 g11_1, int eps) {
        this.eps = eps;
        BigDecimal[] c_0 = g11_0.getC();
        BigDecimal[] c_1 = g11_1.getC();
        c = new BigDecimal[c_0.length + c_1.length];
        BigDecimal two = new BigDecimal(2);
        for (int i = 0; i < c_0.length; i++) {
            c[i] = c_0[i].divide(two, eps, BigDecimal.ROUND_HALF_UP);
        }
        for (int i = c_0.length; i < c.length; i++) {
            c[i] = c_1[i - c_0.length].divide(two, eps, BigDecimal.ROUND_HALF_UP).negate();
        }
        BigDecimal[] x_0 = g11_0.getX();
        BigDecimal[] x_1 = g11_1.getX();
        x = new BigDecimal[x_0.length + x_1.length];
        System.arraycopy(x_0, 0, x, 0, x_0.length);
        System.arraycopy(x_1, 0, x, x_0.length, x_1.length);
    }

    public Gij(BigDecimal[] x, BigDecimal[] c) {
        this(x, c, 50);
    }

    public Gij(BigDecimal[] x, BigDecimal[] c, int eps) {
        this.eps = eps;
        this.x = x;
        this.c = c;
    }

    public BigDecimal[] getC() {
        return c;
    }

    public double[] getCdouble() {
        return Arrays.stream(c).mapToDouble(BigDecimal::doubleValue).toArray();
    }

    public BigDecimal[] getX() {
        return x;
    }

    public double[] getXdouble() {
        return Arrays.stream(x).mapToDouble(BigDecimal::doubleValue).toArray();
    }

    public BigDecimal getValue(BigDecimal w) {
        BigDecimal ans = BigDecimal.ZERO;
        for (int i = 0; i < c.length; i++) {
            ans = ans.add(c[i].divide(w.subtract(x[i]), eps, BigDecimal.ROUND_HALF_UP));
        }
        return ans;
    }

    public double getValue(double w) {
        return getValue(new BigDecimal(w)).doubleValue();
    }

    public String getRepresent() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < x.length; i++) {
            if (i > 0)
                stringBuilder.append('+');
            stringBuilder.append(String.format("%f/(E-%f)", c[i], x[i]));
        }
        return stringBuilder.toString();
    }

    public Complex getValue(Complex w) {
        Complex ans = Complex.ZERO;
        for (int i = 0; i < c.length; i++) {
            ans = ans.add((new Complex(c[i].doubleValue(), 0)).multiply(w.subtract(new Complex(x[i].doubleValue(), 0)).revert()));
        }
        return ans;
    }
}
