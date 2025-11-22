import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controls extends KeyAdapter {
    private boolean forward, reverse, left, right;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> forward = true;
            case KeyEvent.VK_DOWN -> reverse = true;
            case KeyEvent.VK_LEFT -> left = true;
            case KeyEvent.VK_RIGHT -> right = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> forward = false;
            case KeyEvent.VK_DOWN -> reverse = false;
            case KeyEvent.VK_LEFT -> left = false;
            case KeyEvent.VK_RIGHT -> right = false;
        }
    }

    public boolean isForward() { return forward; }
    public boolean isReverse() { return reverse; }
    public boolean isLeft() { return left; }
    public boolean isRight() { return right; }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }
}
