import java.math.BigDecimal;

/**
 * Created by kris13 on 30.04.16.
 */
public class GijComplex extends Gij<Complex> {

    public static GijComplex mergreG11(G11<Complex> g11_0, G11<Complex> g11_1, int eps) {
        Complex[] c_0 = g11_0.getC();
        Complex[] c_1 = g11_1.getC();
        Complex[] c = new Complex[c_0.length + c_1.length];
        Complex two = new Complex(2, 0);
        for (int i = 0; i < c_0.length; i++) {
            c[i] = c_0[i].divide(two);
        }
        for (int i = c_0.length; i < c.length; i++) {
            c[i] = c_1[i - c_0.length].divide(two).negate();
        }
        Complex[] x_0 = g11_0.getX();
        Complex[] x_1 = g11_1.getX();
        Complex[] x = new Complex[x_0.length + x_1.length];
        System.arraycopy(x_0, 0, x, 0, x_0.length);
        System.arraycopy(x_1, 0, x, x_0.length, x_1.length);
        return new GijComplex(x, c, eps);
    }

    public GijComplex(Complex[] x, Complex[] c, int eps) {
        super(x, c, eps);
    }

    @Override
    public Complex getValue(Complex w) {
        Complex[] c = getC();
        Complex[] x = getX();
        Complex ans = Complex.ZERO;
        for (int i = 0; i < c.length; i++) {
            ans = ans.add(c[i].divide(w.subtract(x[i])));
        }
        return ans;
    }
}
