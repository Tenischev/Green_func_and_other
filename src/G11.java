import java.math.BigDecimal;

/**
 * Created by kris13 on 30.04.16.
 */
public abstract class G11<T extends Number> extends Gij<T> {

    public G11(T[] x, T[] c, int eps) {
        super(x, c, eps);
    }

    public G11(T[] x, T[] c) {
        this(x, c, 50);
    }
}
