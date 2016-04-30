import java.math.BigDecimal;

/**
 * Created by kris13 on 30.04.16.
 */
public class G11Complex extends G11<Complex> {
    public G11Complex(Complex[] x, Complex[] c) {
        super(x, c);
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
