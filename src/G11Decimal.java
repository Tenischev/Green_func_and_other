import java.math.BigDecimal;

/**
 * Created by kris13 on 30.04.16.
 */
public class G11Decimal extends G11<BigDecimal> {
    public G11Decimal(BigDecimal[] x, BigDecimal[] c, int eps) {
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
}
