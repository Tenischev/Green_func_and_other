import java.math.BigDecimal;

/**
 * Created by kris13 on 30.04.16.
 */
public class GijDecimal extends Gij<BigDecimal> {

    public static GijDecimal mergreG11(G11<BigDecimal> g11_0, G11<BigDecimal> g11_1, int eps) {
        BigDecimal[] c_0 = g11_0.getC();
        BigDecimal[] c_1 = g11_1.getC();
        BigDecimal[] c = new BigDecimal[c_0.length + c_1.length];
        BigDecimal two = new BigDecimal(2);
        for (int i = 0; i < c_0.length; i++) {
            c[i] = c_0[i].divide(two, eps, BigDecimal.ROUND_HALF_UP);
        }
        for (int i = c_0.length; i < c.length; i++) {
            c[i] = c_1[i - c_0.length].divide(two, eps, BigDecimal.ROUND_HALF_UP).negate();
        }
        BigDecimal[] x_0 = g11_0.getX();
        BigDecimal[] x_1 = g11_1.getX();
        BigDecimal[] x = new BigDecimal[x_0.length + x_1.length];
        System.arraycopy(x_0, 0, x, 0, x_0.length);
        System.arraycopy(x_1, 0, x, x_0.length, x_1.length);
        return new GijDecimal(x, c, eps);
    }

    public GijDecimal(BigDecimal[] x, BigDecimal[] c, int eps) {
        super(x, c, eps);
    }

    @Override
    public BigDecimal getValue(BigDecimal w) {
        BigDecimal[] c = getC();
        BigDecimal[] x = getX();
        BigDecimal ans = BigDecimal.ZERO;
        for (int i = 0; i < c.length; i++) {
            ans = ans.add(c[i].divide(w.subtract(x[i]), eps, BigDecimal.ROUND_HALF_UP));
        }
        return ans;
    }

    public double getValue(double w) {
        return getValue(new BigDecimal(w)).doubleValue();
    }
}
