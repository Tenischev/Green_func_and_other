import java.math.BigDecimal;

/**
 * Created by kris13 on 30.04.16.
 */
public class GijDouble extends Gij<Double> {

    public static GijDouble mergreG11(G11<Double> g11_0, G11<Double> g11_1, int eps) {
        Double[] c_0 = g11_0.getC();
        Double[] c_1 = g11_1.getC();
        Double[] c = new Double[c_0.length + c_1.length];
        for (int i = 0; i < c_0.length; i++) {
            c[i] = c_0[i] / 2.0;
        }
        for (int i = c_0.length; i < c.length; i++) {
            c[i] = -(c_1[i - c_0.length] / 2.0);
        }
        Double[] x_0 = g11_0.getX();
        Double[] x_1 = g11_1.getX();
        Double[] x = new Double[x_0.length + x_1.length];
        System.arraycopy(x_0, 0, x, 0, x_0.length);
        System.arraycopy(x_1, 0, x, x_0.length, x_1.length);
        return new GijDouble(x, c, eps);
    }

    public GijDouble(Double[] x, Double[] c, int eps) {
        super(x, c, eps);
    }

    @Override
    public Double getValue(Double w) {
        Double[] c = getC();
        Double[] x = getX();
        double ans = 0;
        for (int i = 0; i < c.length; i++) {
            ans += c[i] / (w - x[i]);
        }
        return ans;
    }
}
