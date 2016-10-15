import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by kris13 on 23.06.16.
 */
public class CheatMatrix<T extends Number> implements Matrix<T> {

    private final List<T>[] rows;

    private final List<Integer>[] pos;

    public CheatMatrix(List<Integer>[] position, List<T>[] rows) {
        this.rows = rows;
        this.pos = position;
    }

    public static <T extends Number> CheatMatrix<T> createCheatMatrix(int dim, int size, T e, T w, T v) {
        if (dim == 1) {
            return createCheatOneDimMatrix(size, e, w, v);
        } else if (dim == 2) {
            return createCheatTwoDimMatrix(size, e, w, v);
        }
        throw new UnsupportedOperationException("Support only one and two dims");
    }

    private static <T extends Number> CheatMatrix<T> createCheatTwoDimMatrix(int size, T e, T w, T v) {
        Random random = new Random();
        List<T>[] rows = new List[size * size];
        List<Integer>[] pos = new ArrayList[size * size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                ArrayList<T> curentRow = new ArrayList<T>();
                ArrayList<Integer> curentPos = new ArrayList<>();
                int point = i * size + j;
                rows[point] = curentRow;
                pos[point] = curentPos;
                // Up element
                if (i != 0) {
                    curentRow.add(v);
                    curentPos.add(point - size);
                }
                // Left element
                if (j != 0) {
                    curentRow.add(v);
                    curentPos.add(point - 1);
                }
                // Diagonal
                curentRow.add(createDiagElement(random.nextDouble(), e, w));
                curentPos.add(point);
                // Right element
                if (j != size - 1) {
                    curentRow.add(v);
                    curentPos.add(point + 1);
                }
                // Down element
                if (i != size - 1) {
                    curentRow.add(v);
                    curentPos.add(point + size);
                }
            }
        }
        /*for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                List<T> el = rows[i * size + j];
                List<Integer> p = pos[i * size + j];
                int ind = 0;
                for (int k = 0; k < size * size; k++) {
                    if (ind < p.size() && k == p.get(ind)) {
                        System.out.print(el.get(ind).doubleValue() + " \t");
                        ind++;
                    } else {
                        System.out.print("0 \t");
                    }
                }
                System.out.println();
            }
        }*/
        return new CheatMatrix<T>(pos, rows);
    }

    private static <T extends Number> CheatMatrix<T> createCheatOneDimMatrix(int size, T e, T w, T v) {
        Random random = new Random();
        List<T>[] rows = new List[size];
        List<Integer>[] pos = new ArrayList[size];
        for (int i = 0; i < size; i++) {
            rows[i] = new ArrayList<T>();
            pos[i] = new ArrayList<>();
            if (i != 0) {
                rows[i].add(v);
                pos[i].add(i - 1);
            }
            rows[i].add(createDiagElement(random.nextDouble(), e, w));
            pos[i].add(i);
            if (i != size - 1) {
                rows[i].add(v);
                pos[i].add(i + 1);
            }
        }
        return new CheatMatrix<T>(pos, rows);
    }

    private static <T extends Number> T createDiagElement(double random, T e, T w) {
        double rand = 0.5 - random;
        if (e instanceof Double) {
            Double eD = (Double) e;
            Double wD = (Double) w;
            return (T) new Double(eD + rand * wD);
        } else if (e instanceof Complex) {
            Complex eC = (Complex) e;
            Complex wC = (Complex) w;
            return (T) new Complex(eC.getReal() + rand * wC.getReal(), eC.getImage() + rand * wC.getImage());
        } else if (e instanceof BigDecimal) {
            BigDecimal eB = (BigDecimal) e;
            BigDecimal wB = (BigDecimal) w;
            return (T) wB.multiply(new BigDecimal(rand)).add(eB);
        }
        return null;
    }

    public List<T>[] getRows() {
        return rows;
    }

    public List<T> getRow(int i) {
        return rows[i];
    }

    @Override
    public int getDimensional() {
        return getRows().length;
    }

    public List<Integer> getPositions(int i) {
        return pos[i];
    }
}
