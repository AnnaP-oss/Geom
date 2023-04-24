import app.Point;
import app.Task;
import app.Triangle;
import misc.CoordinateSystem2d;
import misc.Vector2d;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Класс тестирования
 */
public class UnitTest {
    /**
     * Первый тест
     */
    @Test
    public void test1() {
        //правильный треугольник со стороной 5
        Triangle right = new Triangle(new Point(new Vector2d(0.0,0.0)),
                new Point(new Vector2d(5.0, 0.0)),
                new Point(new Vector2d(2.5, Math.sqrt(5.0*5.0-2.5*2.5))));
        //треугольник со сторонами 5 и не 5
        Triangle notright = new Triangle(new Point(new Vector2d(0.0,0.0)),
                new Point(new Vector2d(5.0, 0.0)),
                new Point(new Vector2d(2.5, Math.sqrt(6.0))));
        //проверка равносторонности
        assert right.isRight();
        assert !notright.isRight();
    }

    /**
     * Второй тест
     */
    @Test
    public void test2() {
        ArrayList<Point> points = new ArrayList<>();
        points.add(new Point(new Vector2d(0.0,0.0)));
        points.add(new Point(new Vector2d(5.0,0.0)));
        points.add(new Point(new Vector2d(-6.05,9.25)));
        points.add(new Point(new Vector2d(-1.0,-1.0)));
        points.add(new Point(new Vector2d(-6.05,9.0)));
        //ни одна из точек не является вершиной равностороннего прямоугольника
        Task task = new Task(new CoordinateSystem2d(10, 10, 20, 20), points);
        task.solve();
        assert task.getTriangles().size() == 0;
        assert task.getMaxset().size() == 0;
    }

    /**
     * Третий тест
     */
    @Test
    public void test3() {
        ArrayList<Point> points = new ArrayList<>();
        points.add(new Point(new Vector2d(0.0,0.0)));
        points.add(new Point(new Vector2d(5.0,0.0)));
        points.add(new Point(new Vector2d(2.5,Math.sqrt(5.0*5.0-2.5*2.5))));
        points.add(new Point(new Vector2d(-1.0,-1.0)));
        points.add(new Point(new Vector2d(-6.05,9.0)));
        //есть один правильный треугольник
        Task task = new Task(new CoordinateSystem2d(10, 10, 20, 20), points);
        task.solve();
        assert task.getTriangles().size() == 1;
        assert task.getMaxset().size() == 0;
    }
    /**
     * Четвертый тест
     */
    @Test
    public void test4() {
        ArrayList<Point> points = new ArrayList<>();
        //6 точек в множестве
        points.add(new Point(new Vector2d(0.0,0.0)));
        points.add(new Point(new Vector2d(5.0,0.0)));
        points.add(new Point(new Vector2d(2.5,Math.sqrt(5.0*5.0-2.5*2.5))));
        points.add(new Point(new Vector2d(7.5,Math.sqrt(5.0*5.0-2.5*2.5))));
        points.add(new Point(new Vector2d(2.5,-Math.sqrt(5.0*5.0-2.5*2.5))));
        points.add(new Point(new Vector2d(7.5,-Math.sqrt(5.0*5.0-2.5*2.5))));
        //лишние точки
        points.add(new Point(new Vector2d(-8.0,Math.sqrt(5.0*5.0-2.5*2.5))));
        points.add(new Point(new Vector2d(6.0,6.0)));
        points.add(new Point(new Vector2d(-7.77,6.66)));
        //есть искомое множество и неподходящие точки
        Task task = new Task(new CoordinateSystem2d(10, 10, 20, 20), points);
        task.solve();
        assert task.getMaxset().size() == 6;
    }
    /**
     * Пятый тест
     */
    @Test
    public void test5() {
        ArrayList<Point> points = new ArrayList<>();
        //все точки в множестве
        points.add(new Point(new Vector2d(0.0,0.0)));
        points.add(new Point(new Vector2d(5.0,0.0)));
        points.add(new Point(new Vector2d(2.5,Math.sqrt(5.0*5.0-2.5*2.5))));
        points.add(new Point(new Vector2d(7.5,Math.sqrt(5.0*5.0-2.5*2.5))));
        points.add(new Point(new Vector2d(2.5,-Math.sqrt(5.0*5.0-2.5*2.5))));
        points.add(new Point(new Vector2d(7.5,-Math.sqrt(5.0*5.0-2.5*2.5))));
        points.add(new Point(new Vector2d(10.0,0.0)));
        Task task = new Task(new CoordinateSystem2d(10, 10, 20, 20), points);
        task.solve();
        for (Point point : points) {
            assert point.count >= 2;
        }
        assert task.getMaxset().size() == 7;
    }
}
