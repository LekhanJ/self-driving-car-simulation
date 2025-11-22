import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Road extends JPanel {
    private int x, width, laneCount;
    private int left, right, top, bottom;
    private List<List<Point>> borders;
    private List<Car> cars;
    private List<Car> traffic;
    private NeuralNetworkVisualization nnPanel;
    private Car bestCar;

    public Road(int x, int width, int laneCount) {
        this.x = x;
        this.width = width;
        this.laneCount = laneCount;
        this.left = x - width / 2;
        this.right = x + width / 2;

        int infinity = 1000000;
        this.top = -infinity;
        this.bottom = infinity;

        borders = new ArrayList<>();
        ArrayList<Point> leftBorder = new ArrayList<>();
        leftBorder.add(new Point(left, top));
        leftBorder.add(new Point(left, bottom));

        ArrayList<Point> rightBorder = new ArrayList<>();
        rightBorder.add(new Point(right, top));
        rightBorder.add(new Point(right, bottom));

        borders.add(leftBorder);
        borders.add(rightBorder);

        this.cars = generateCars(1);
        this.bestCar = cars.get(0);
        if (BrainStorage.load() != null) {
            for (int i=0; i<cars.size(); i++) {
                cars.get(i).setBrain(BrainStorage.load());
                if (i != 0) {
                    NeuralNetwork.mutate(cars.get(i).getBrain(), 0.1);
                }
            }
        }

        this.traffic = new ArrayList<>();
        this.traffic.add(new Car(getLaneCenter(0), 300, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(1), 300, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(3), 300, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(1), -100, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(2), -100, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(4), -100, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(1), -400, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(2), -400, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(3), -400, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(0), -900, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(1), -900, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(4), -900, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(3), -1200, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(4), -1200, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(0), -1500, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(1), -1500, 40, 80, "DUMMY", false));
        this.traffic.add(new Car(getLaneCenter(2), -1500, 40, 80, "DUMMY", false));

        setFocusable(true);
        for (Car car : cars) {
            addKeyListener(car.getControls());
        }

        Timer timer = new Timer(16, e -> {
            for (Car t : traffic) {
                t.update(borders, new ArrayList<>());
            }

            for (Car car : cars) {
                car.update(borders, traffic);
            }

            if (nnPanel != null && !cars.isEmpty()) {
                Car best = cars.get(0);
                for (Car c : cars) {
                    if (c.getY() < best.getY()) {
                        best = c;
                    }
                }
                nnPanel.setNetwork(best.getBrain());
                nnPanel.repaint();
            }

            repaint();
        });

        timer.start();
    }

    public int getLaneCenter(int laneIndex) {
        int laneWidth = width / laneCount;
        return left + laneWidth / 2 + Math.min(laneIndex, laneCount - 1) * laneWidth;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        bestCar = cars.get(0);
        for (Car car : cars) {
            if (car.getY() < bestCar.getY()) {
                bestCar = car;
            }
        }
        g2.translate(0, -bestCar.getY() + getHeight() * 0.7);

        g2.setColor(Color.WHITE);
        for (int i = 1; i <= laneCount - 1; i++) {
            double t = (double) i / laneCount;
            int x = (int) Utils.lerp(left, right, t);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10, new float[]{20, 20}, 0));
            g2.drawLine(x, top, x, bottom);
        }

        g2.setStroke(new BasicStroke(5));
        for (List<Point> border : borders) {
            g2.drawLine(border.get(0).x, border.get(0).y,
                    border.get(1).x, border.get(1).y);
        }

        for (Car trafficCar : traffic) {
            trafficCar.draw(g2);
        }

        for (Car car : cars) {
            if (car != bestCar) {
                car.draw(g2, false, 0.35f);
            }
        }

        bestCar.draw(g2, true, 1.0f);

        g2.dispose();
    }

    public List<Car> generateCars(int n) {
        List<Car> cars = new ArrayList<>();
        for (int i=0; i< n; i++) {
            cars.add(new Car(getLaneCenter(laneCount / 2), 500, 40, 80, "MAIN", true));
        }
        return cars;
    }

    public Car getCar() {
        return cars.get(0);
    }

    public void setNeuralNetworkPanel(NeuralNetworkVisualization panel) {
        this.nnPanel = panel;
    }

    public Car getBestCar() {
        return bestCar;
    }
}