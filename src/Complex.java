/**
 * Created by kris13 on 30.04.16.
 */
public class Complex extends Number{
    public static final Complex I = new Complex(0, 1);
    public static final Complex ONE = new Complex(1, 0);
    public static final Complex ZERO = new Complex(0, 0);

    private double real;
    private double image;

    public Complex(double re, double im) {
        real = re;
        image = im;
    }

    public double getImage() {
        return image;
    }

    public double getReal() {
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
        return Math.sqrt(real * real + image * image);
    }

    @Override
    public String toString() {
        return String.format("%f + %fi", real, image);
    }

    public Complex add(Complex complex) {
        return new Complex(this.real + complex.real, this.image + complex.image);
    }

    public Complex subtract(Complex complex) {
        return new Complex(this.real - complex.real, this.image - complex.image);
    }

    public Complex multiply(Complex complex) {
        return new Complex(this.real * complex.real - this.image * complex.image,
                this.real * complex.image + this.image * complex.real);
    }

    /*public Complex divide(Complex complex) {
        return new Complex((this.real * complex.real + this.image * complex.image) / (complex.real * complex.real + complex.image * complex.image),
                (this.real * complex.image - this.image * complex.real) / (complex.real * complex.real + complex.image * complex.image));
    }*/

    public Complex revert() {
        return new Complex(real / (real * real + image * image), -image / (real * real + image * image));
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Complex) && (this.real == ((Complex) obj).real && this.image == ((Complex) obj).image);
    }

    @Override
    public int hashCode() {
        return intValue();
    }

    public Complex negate() {
        return new Complex(-this.real, -this.image);
    }

    public boolean isNaN() {
        return Double.isNaN(real) || Double.isNaN(image);
    }

    public Complex onI() {
        return new Complex(-getImage(), getReal());
    }
}
