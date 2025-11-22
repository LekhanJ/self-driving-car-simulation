import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Sensor {
    private Car car;
    private int rayCount = 5;
    private double rayLength = 250;
    private double raySpread = Math.PI / 3;
    private List<Ray> rays = new ArrayList<>();
    private List<Point> readings = new ArrayList<>();

    public Sensor(Car car) {
        this.car = car;
    }

    public void update(List<List<Point>> roadBorders, List<Car> traffic) {
        castRays();
        readings.clear();
        for (Ray ray : rays) {
            readings.add(getReading(ray, roadBorders, traffic));
        }
    }

    private Point getReading(Ray ray, List<List<Point>> roadBorders, List<Car> traffic) {
        List<Point> touches = new ArrayList<>();
        Point A = new Point((int) ray.startX, (int) ray.startY);
        Point B = new Point((int) ray.endX, (int) ray.endY);

        for (List<Point> border : roadBorders) {
            Point C = border.get(0);
            Point D = border.get(1);
            Point p = Utils.getIntersection(A, B, C, D);
            if (p != null) {
                touches.add(p);
            }
        }

        for (Car car : traffic) {
            List<Point> poly = car.getPolygon();
            for (int i = 0; i < poly.size(); i++) {
                Point C = poly.get(i);
                Point D = poly.get((i + 1) % poly.size());
                Point p = Utils.getIntersection(A, B, C, D);
                if (p != null) {
                    touches.add(p);
                }
            }
        }

        if (touches.isEmpty()) {
            return null;
        }

        Point closest = touches.get(0);
        double minDist = ray.distanceTo(closest);
        for (Point p : touches) {
            double dist = ray.distanceTo(p);
            if (dist < minDist) {
                minDist = dist;
                closest = p;
            }
        }

        return closest;
    }

    private void castRays() {
        rays.clear();
        for (int i = 0; i < rayCount; i++) {
            double rayAngle = Utils.lerp(raySpread / 2, -raySpread / 2,
                    rayCount == 1 ? 0.5 : (double) i / (rayCount - 1));
            rayAngle -= car.getAngle();

            double startX = car.getX();
            double startY = car.getY();
            double endX = car.getX() - Math.sin(rayAngle) * rayLength;
            double endY = car.getY() - Math.cos(rayAngle) * rayLength;

            rays.add(new Ray(startX, startY, endX, endY));
        }
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < rays.size(); i++) {
            Ray ray = rays.get(i);
            Point end = (i < readings.size() && readings.get(i) != null)
                    ? readings.get(i)
                    : new Point((int) ray.endX, (int) ray.endY);

            g.setColor(Color.YELLOW);
            g.setStroke(new BasicStroke(2));
            g.drawLine((int) ray.startX, (int) ray.startY, end.x, end.y);

            if (readings.get(i) != null) {
                g.setColor(Color.BLACK);
                g.drawLine(end.x, end.y, (int) ray.endX, (int) ray.endY);
            }
        }
    }

    public double getNormalizedOffset(int index) {
        if (readings.get(index) == null) {
            return 1.0;
        }

        Ray ray = rays.get(index);
        Point start = new Point((int) ray.startX, (int) ray.startY);
        Point end = readings.get(index);

        double distance = Math.hypot(end.x - start.x, end.y - start.y);
        return distance / rayLength;
    }

    private static class Ray {
        double startX, startY, endX, endY;

        Ray(double sx, double sy, double ex, double ey) {
            this.startX = sx;
            this.startY = sy;
            this.endX = ex;
            this.endY = ey;
        }

        double distanceTo(Point p) {
            return Math.hypot(p.x - startX, p.y - startY);
        }
    }

    public int getRayCount() {
        return rayCount;
    }

    public List<Point> getReadings() {
        return readings;
    }
}