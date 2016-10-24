import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by kris13 on 30.04.16.
 */
public class Complex extends Number{
    public static final Complex I = new Complex(0, 1);
    public static final Complex ONE = new Complex(1, 0);
    public static final Complex ZERO = new Complex(0, 0);

    private BigDecimal real;
    private BigDecimal image;

    public Complex(double re, double im) {
        real = new BigDecimal(re);
        image = new BigDecimal(im);
    }

    public Complex(BigDecimal re, BigDecimal im) {
        real = re;
        image = im;
    }

    public double getImage() {
        return image.doubleValue();
    }

    public double getReal() {
        return real.doubleValue();
    }

    public BigDecimal getRealBig() {
        return real;
    }

    @Override
    public int intValue() {
        return (int) doubleValue();
    }

    @Override
    public long longValue() {
        return (long) doubleValue();
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    @Override
    public double doubleValue() {
        return this.bigValue().doubleValue();
    }

    public BigDecimal bigValue() {
        return UtilsVector.bigSqrt(real.multiply(real).add(image.multiply(image)));
    }

    @Override
    public String toString() {
        return String.format("%f + %fi", real, image);
    }

    public Complex add(Complex complex) {
        return new Complex(this.real.add(complex.real), this.image.add(complex.image));
    }

    public Complex subtract(Complex complex) {
        return new Complex(this.real.subtract(complex.real), this.image.subtract(complex.image));
    }

    public Complex multiply(Complex complex) {
        return new Complex(this.real.multiply(complex.real).subtract(this.image.multiply(complex.image)),
                this.real.multiply(complex.image).add(this.image.multiply(complex.real)));
    }

    /*public Complex divide(Complex complex) {
        return new Complex((this.real * complex.real + this.image * complex.image) / (complex.real * complex.real + complex.image * complex.image),
                (this.real * complex.image - this.image * complex.real) / (complex.real * complex.real + complex.image * complex.image));
    }*/

    public Complex revert() {
        return new Complex(real.divide(real.multiply(real).add(image.multiply(image))),
                            image.negate().divide(real.multiply(real).add(image.multiply(image))));
    }

    public Complex revert(int eps) {
        return new Complex(real.divide(real.multiply(real).add(image.multiply(image)), eps, RoundingMode.HALF_UP),
                            image.negate().divide(real.multiply(real).add(image.multiply(image)), eps, RoundingMode.HALF_UP));
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Complex) && (this.real.equals(((Complex) obj).real) && this.image.equals(((Complex) obj).image));
    }

    @Override
    public int hashCode() {
        return intValue();
    }

    public Complex negate() {
        return new Complex(this.real.negate(), this.image.negate());
    }

    public boolean isNaN() {
        return Double.isNaN(real.doubleValue()) || Double.isNaN(image.doubleValue());
    }

    public Complex onI() {
        return new Complex(-getImage(), getReal());
    }


}
