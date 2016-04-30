import java.util.function.Function;

/**
 * Created by kris13 on 30.04.16.
 */
public class CalcG11Complex extends CalcG11<Complex> {

    /**
     * {@inheritDoc}
     */
    public CalcG11Complex(TridiagonalMatrix<Complex> matrix, int accuracy, boolean useParallel) {
        super(matrix, accuracy, useParallel);
    }

    /**
     * {@inheritDoc}
     */
    public CalcG11Complex(TridiagonalMatrix<Complex> matrix, int accuracy, boolean useParallel, boolean log, boolean detailLog) {
        super(matrix, accuracy, useParallel, log, detailLog);
    }

    @Override
    public G11<Complex> call() throws Exception {
        c = new Complex[dimension + 1];
        x = new Complex[1];
        x[0] = a[0];
        c[0] = Complex.ONE;
        for (step = 1; step <= dimension; step++) {
            double bb = b[dimension - step].doubleValue() * b[dimension - step].doubleValue();
            if (log) System.out.print("Next step - " + step);
            for (int j = 0; j < step; j++) {
                if (log && Complex.ZERO.equals(c[j]))
                    System.out.print(", I'm found zero C");
                c[j] = c[j].multiply(new Complex(bb, 0));
            }
            if (print) {
                System.out.format("Root of: x = %s", a[dimension - step].toString());
                for (int j = 0; j < step; j++) {
                    System.out.format(" + %s/(x - %s)", c[j].toString(), x[j].toString());
                }
                System.out.println();
            }
            findRoots(c, x, a[dimension - step], step);
            if (log) System.out.println(", roots calculate");
            if (print) {
                System.out.print("Is ");
                for (int j = 0; j <= step; j++)
                    System.out.print(rootNew[j] + " ");
            }
            counter.set(0);
            for (int j = 0; j <= step; j++) {
                if (step >= 50) {
                    pool.execute(new CalcC(j));
                } else {
                    c[j] = Complex.ONE;
                    for (int k = 0; k < x.length; k++) {
                        c[j] = c[j].multiply(rootNew[j].subtract(x[k]));
                        if (k != j)
                            c[j] = c[j].divide(rootNew[j].subtract(rootNew[k]));
                    }
                    if (x.length != j)
                        c[j] = c[j].divide(rootNew[j].subtract(rootNew[x.length]));
                    counter.incrementAndGet();
                }
            }
            synchronized (lock) {
                while (counter.get() != step + 1) {
                    lock.wait();
                }
                counter.set(0);
            }
            x = rootNew;
            if (print)
                System.out.println();
        }
        return new G11Complex(x, c);
    }

    private void findRoots(Complex[] c, Complex[] x, Complex energy, int ind) throws InterruptedException {
        rootNew = new Complex[ind + 1];
        Function<Complex, Complex> func = root -> {
            Complex ans = energy;
            for (int i = 0; i < ind; i++) {
                ans = ans.add(c[i].divide(root.subtract(x[i])));
            }
            return ans;
        };
        counter.set(0);
        for (int i = 0; i < ind + 1; i++) {
            if (i == 0)
                pool.execute(new FindRood(i, x[i], new Complex(100, 0), func));
                //roots[i] = findRoot(x[i], new BigDecimal(100), func);
            else if (i == ind)
                pool.execute(new FindRood(i, new Complex(-100, 0), x[i - 1], func));
                //roots[i] = findRoot(new BigDecimal(-100), x[i - 1], func);
            else
                pool.execute(new FindRood(i, x[i], x[i - 1], func));
            //roots[i] = findRoot(x[i], x[i - 1], func);
        }
        synchronized (lock) {
            while (counter.get() != ind + 1) {
                lock.wait();
            }
            counter.set(0);
        }
    }

    private class FindRood implements Runnable {
        private final int cur;
        private final Function<Complex, Complex> func;
        private Complex r;
        private Complex l;
        private Complex two = Complex.ONE.multiply(new Complex(2, 0));

        public FindRood(int i, Complex l, Complex r, Function<Complex, Complex> func) {
            cur = i;
            this.l = l;
            this.r = r;
            this.func = func;
        }

        @Override
        public void run() {
            while (r.subtract(l).doubleValue() > Math.pow(10, -8)) {
                Complex m = r.add(l).divide(two);
                if (Double.compare(m.doubleValue(), (func.apply(m)).doubleValue()) > 0)
                    r = m;
                else
                    l = m;
            }
            rootNew[cur] = l;
            if (counter.incrementAndGet() == rootNew.length)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }

    private class CalcC implements Runnable {
        private final int cur;

        public CalcC(int j) {
            cur = j;
        }

        @Override
        public void run() {
            c[cur] = Complex.ONE;
            for (int k = 0; k < x.length; k++) {
                c[cur] = c[cur].multiply(rootNew[cur].subtract(x[k]));
                if (k != cur)
                    c[cur] = c[cur].divide(rootNew[cur].subtract(rootNew[k]));
            }
            if (x.length != cur)
                c[cur] = c[cur].divide(rootNew[cur].subtract(rootNew[x.length]));
            if (counter.incrementAndGet() == step + 1)
                synchronized (lock) {
                    lock.notify();
                }
        }
    }
}
