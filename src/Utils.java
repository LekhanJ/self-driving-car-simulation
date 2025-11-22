import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static double lerp(double A, double B, double t) {
        return A + (B - A) * t;
    }

    public static Point getIntersection(Point A, Point B, Point C, Point D) {
        double tTop = (D.x - C.x) * (A.y - C.y) - (D.y - C.y) * (A.x - C.x);
        double uTop = (C.y - A.y) * (A.x - B.x) - (C.x - A.x) * (A.y - B.y);
        double bottom = (D.y - C.y) * (B.x - A.x) - (D.x - C.x) * (B.y - A.y);

        if (bottom != 0) {
            double t = tTop / bottom;
            double u = uTop / bottom;
            if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
                return new Point(
                        (int) (A.x + (B.x - A.x) * t),
                        (int) (A.y + (B.y - A.y) * t)
                );
            }
        }
        return null;
    }

    public static boolean polysIntersect(List<Point> poly1, List<Point> poly2) {
        for (int i = 0; i < poly1.size(); i++) {
            Point A = poly1.get(i);
            Point B = poly1.get((i + 1) % poly1.size());

            for (int j = 0; j < poly2.size(); j++) {
                Point C = poly2.get(j);
                Point D = poly2.get((j + 1) % poly2.size());

                if (getIntersection(A, B, C, D) != null) {
                    return true;
                }
            }
        }
        return false;
    }


}
