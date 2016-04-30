/**
 * Created by kris13 on 26.04.16.
 */
public class Temp {
    public static void main(String[] args) {
        for (long i = -10000; i < 10000; i++) {
            for (long j = -10000; j < 10000; j++) {
                //if (i - 48*i*j + j == 109)
                if (i*j == -4*i-3*j+12)
                    System.out.printf("%d %d\n", i, j);
            }
        }
    }
}
