import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

class Car {
    private double x, y;
    private int width, height;
    private double speed = 0;
    private double acceleration = 0.2;
    private double friction = 0.05;
    private double maxSpeed = 10;
    private double angle = 0;
    private Controls controls;
    private BufferedImage carImage;
    private Sensor sensor;
    private boolean damaged = false;
    private List<Point> polygon;
    private String type;
    private NeuralNetwork brain;
    private boolean useAI = false;

    public Car(int x, int y, int width, int height, String type, boolean useAI) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.controls = new Controls();

        this.useAI = useAI;

        if (!this.type.equals("DUMMY")) {
            this.sensor = new Sensor(this);
            this.brain = new NeuralNetwork(
                    new int[]{this.sensor.getRayCount(), 12, 4}
            );
        }

        try {
            carImage = ImageIO.read(new File("images/car.png"));
            carImage = resize(carImage, width, height);
        } catch (Exception e) {
            System.out.println("Could not load car image, using polygon rendering");
        }
    }

    public void update(List<List<Point>> roadBorders, List<Car> traffic) {
        if (!damaged) {
            if (type.equals("DUMMY")) {
                speed = maxSpeed / 2;
                y -= speed;
            } else {
                move();
            }
            polygon = createPolygon();
            damaged = assessDamage(roadBorders, polygon, traffic);
        }

        if (sensor != null) {
            sensor.update(roadBorders, traffic);
            double[] offsets = new double[sensor.getReadings().size()];
            for (int i = 0; i < offsets.length; i++) {
                Point s = sensor.getReadings().get(i);
                offsets[i] = (s == null) ? 0 : 1 - (sensor.getNormalizedOffset(i));
            }
            double[] outputs = NeuralNetwork.feedForward(offsets, this.brain);
            if (this.useAI) {
                this.controls.setForward(outputs[0] > 0.5);
                this.controls.setLeft(outputs[1] > 0.5);
                this.controls.setRight(outputs[2] > 0.5);
                this.controls.setReverse(outputs[3] > 0.5);
            }
        }
    }

    public void draw(Graphics g) {
        draw(g, true, 1.0f);
    }

    public void draw(Graphics g, boolean drawSensor, float alpha) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        Composite oldComposite = g2d.getComposite();

        float a = Math.max(0f, Math.min(1f, alpha));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));

        g2d.translate(x, y);
        g2d.rotate(angle);

        if (damaged) {
            g2d.setColor(Color.GRAY);
        } else {
            if (this.type.equals("DUMMY")) {
                g2d.setColor(new Color(237, 137, 137));
            } else {
                g2d.setColor(new Color(132, 207, 242));
            }
        }

        g2d.fillRect(-width / 2, -height / 2, width, height);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(-width / 2, -height / 2, width, height);

        g2d.setTransform(oldTransform);
        g2d.setComposite(oldComposite);

        if (drawSensor && sensor != null) {
            sensor.draw(g2d);
        }
    }

    public Controls getControls() {
        return controls;
    }

    private void move() {
        if (controls.isForward()) speed += acceleration;
        if (controls.isReverse()) speed -= acceleration;

        if (speed > maxSpeed) speed = maxSpeed;
        if (speed < -maxSpeed / 2) speed = -maxSpeed / 2;

        if (speed > 0) speed -= friction;
        if (speed < 0) speed += friction;
        if (Math.abs(speed) < friction) speed = 0;

        if (speed != 0) {
            double flip = speed > 0 ? 1 : -1;
            if (controls.isLeft()) angle -= 0.03 * flip;
            if (controls.isRight()) angle += 0.03 * flip;
        }

        x += speed * Math.sin(angle);
        y -= speed * Math.cos(angle);
    }

    private BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    private List<Point> createPolygon() {
        List<Point> points = new ArrayList<>();
        double rad = Math.hypot(width, height) / 2;
        double alpha = Math.atan2(width, height);

        points.add(new Point(
                (int) (x - Math.sin(angle - alpha) * rad),
                (int) (y - Math.cos(angle - alpha) * rad)
        ));
        points.add(new Point(
                (int) (x - Math.sin(angle + alpha) * rad),
                (int) (y - Math.cos(angle + alpha) * rad)
        ));
        points.add(new Point(
                (int) (x - Math.sin(Math.PI + angle - alpha) * rad),
                (int) (y - Math.cos(Math.PI + angle - alpha) * rad)
        ));
        points.add(new Point(
                (int) (x - Math.sin(Math.PI + angle + alpha) * rad),
                (int) (y - Math.cos(Math.PI + angle + alpha) * rad)
        ));

        return points;
    }

    private boolean assessDamage(List<List<Point>> roadBorders,
                                 List<Point> polygon, List<Car> traffic) {
        for (List<Point> border : roadBorders) {
            if (Utils.polysIntersect(polygon, border)) {
                return true;
            }
        }

        for (Car car : traffic) {
            if (Utils.polysIntersect(polygon, car.getPolygon())) {
                return true;
            }
        }

        return false;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public double getAcceleration() { return acceleration; }
    public void setAcceleration(double acceleration) { this.acceleration = acceleration; }
    public double getFriction() { return friction; }
    public void setFriction(double friction) { this.friction = friction; }
    public double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(double maxSpeed) { this.maxSpeed = maxSpeed; }
    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
    public void setControls(Controls controls) { this.controls = controls; }
    public BufferedImage getCarImage() { return carImage; }
    public void setCarImage(BufferedImage carImage) { this.carImage = carImage; }
    public List<Point> getPolygon() { return polygon; }
    public boolean isDamaged() { return damaged; }

    public NeuralNetwork getBrain() {
        return brain;
    }

    public void setBrain(NeuralNetwork brain) {
        this.brain = brain;
    }
}