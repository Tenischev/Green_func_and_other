import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by kris13 on 26.03.16.
 */
public abstract class Gij<T extends Number> {
    private final T[] c;
    private final T[] x;
    protected final int eps;

    public Gij(T[] x, T[] c) {
        this(x, c, 50);
    }

    public Gij(T[] x, T[] c, int eps) {
        this.eps = eps;
        this.x = x;
        this.c = c;
    }

    public T[] getC() {
        return c;
    }

    public T[] getX() {
        return x;
    }

    public abstract T getValue(T w);
}
