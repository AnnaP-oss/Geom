package app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.humbleui.jwm.MouseButton;
import io.github.humbleui.skija.*;
import lombok.Getter;
import misc.*;
import panels.PanelLog;

import java.util.ArrayList;
import static app.Colors.*;

/**
 * Класс задачи
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Task {
    /**
     * Текст задачи
     */
    public static final String TASK_TEXT = """
            ПОСТАНОВКА ЗАДАЧИ:
            Дано множество точек на плоскости. Определить среди
            них подмножество точек наибольшего размера такое,
            что каждая точка этого множества является вершиной
            хотя бы двух равносторонних треугольников, вершины
            которого принадлежат этому подмножеству.""";
    /**
     * коэффициент колёсика мыши
     */
    private static final float WHEEL_SENSITIVE = 0.001f;
    /**
     * Вещественная система координат задачи
     */
    @Getter
    private final CoordinateSystem2d ownCS;
    /**
     * последняя СК окна
     */
    protected CoordinateSystem2i lastWindowCS;
    /**
     * Флаг, решена ли задача
     */
    @JsonIgnore
    private boolean solved;
    /**
     * Список точек
     */
    @Getter
    private final ArrayList<Point> points;
    /**
     * Список треугольников
     */
    @Getter
    @JsonIgnore
    private final ArrayList<Triangle> triangles;
    /**
     * Размер точки
     */
    private static final int POINT_SIZE = 3;
    /**
     * Порядок разделителя сетки, т.е. раз в сколько отсечек
     * будет нарисована увеличенная
     */
    private static final int DELIMITER_ORDER = 10;

    /**
     * Задача
     *
     * @param ownCS  СК задачи
     * @param points массив точек
     */
    @JsonCreator
    public Task(
            @JsonProperty("ownCS") CoordinateSystem2d ownCS,
            @JsonProperty("points") ArrayList<Point> points
    ) {
        this.ownCS = ownCS;
        this.points = points;
        this.triangles = new ArrayList<>();
    }

    /**
     * Рисование
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void paint(Canvas canvas, CoordinateSystem2i windowCS) {
        // Сохраняем последнюю СК
        lastWindowCS = windowCS;
        // рисуем координатную сетку
        renderGrid(canvas, lastWindowCS);
        // рисуем задачу
        renderTask(canvas, windowCS);
    }
    /**
     * Рисование задачи
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    private void renderTask(Canvas canvas, CoordinateSystem2i windowCS) {
        canvas.save();
        // создаём перо
        try (var paint = new Paint()) {
            for (Point p : points) {
                paint.setColor(CROSSED_COLOR);
                // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                // а в классическом представлении - вверх
                Vector2i windowPos = windowCS.getCoords(p.pos.x, p.pos.y, ownCS);
                // рисуем точку
                canvas.drawRect(Rect.makeXYWH(windowPos.x - POINT_SIZE, windowPos.y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2), paint);
                if (solved) {
                    paint.setColor(TRIANGLE_COLOR);
                    for (Triangle t : triangles){
                        //пересчитываем вершины треугольника и по ним рисуем его стороны
                        Vector2i windowPosPointA = windowCS.getCoords(t.pointA.pos.x, t.pointA.pos.y, ownCS);
                        Vector2i windowPosPointB = windowCS.getCoords(t.pointB.pos.x, t.pointB.pos.y, ownCS);
                        Vector2i windowPosPointC = windowCS.getCoords(t.pointC.pos.x, t.pointC.pos.y, ownCS);
                        canvas.drawLine(windowPosPointA.x, windowPosPointA.y, windowPosPointB.x, windowPosPointB.y, paint);
                        canvas.drawLine(windowPosPointA.x, windowPosPointA.y, windowPosPointC.x, windowPosPointC.y, paint);
                        canvas.drawLine(windowPosPointC.x, windowPosPointC.y, windowPosPointB.x, windowPosPointB.y, paint);
                    }
                    paint.setColor(SUBTRACTED_COLOR);
                    for (Point point : maxset){
                        //точки из искомого множества
                        Vector2i windowPos1 = windowCS.getCoords(point.pos.x, point.pos.y, ownCS);
                        canvas.drawRect(Rect.makeXYWH(windowPos1.x - POINT_SIZE, windowPos1.y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2), paint);
                    }
                    paint.setColor(ANSWER_TRIANGLE_COLOR);
                    for (Triangle t : triangles){
                        if (maxset.contains(t.pointA) && maxset.contains(t.pointB) && maxset.contains(t.pointC)) {
                            //если все три вершины содержатся в искомом множестве, то рисуем по ним треугольник
                            Vector2i windowPosPointA = windowCS.getCoords(t.pointA.pos.x, t.pointA.pos.y, ownCS);
                            Vector2i windowPosPointB = windowCS.getCoords(t.pointB.pos.x, t.pointB.pos.y, ownCS);
                            Vector2i windowPosPointC = windowCS.getCoords(t.pointC.pos.x, t.pointC.pos.y, ownCS);
                            canvas.drawLine(windowPosPointA.x, windowPosPointA.y, windowPosPointB.x, windowPosPointB.y, paint);
                            canvas.drawLine(windowPosPointA.x, windowPosPointA.y, windowPosPointC.x, windowPosPointC.y, paint);
                            canvas.drawLine(windowPosPointC.x, windowPosPointC.y, windowPosPointB.x, windowPosPointB.y, paint);
                        }
                    }
                }
            }
        }
        canvas.restore();
    }

    /**
     * Добавить точку
     *
     * @param pos      положение
     */
    public void addPoint(Vector2d pos) {
        solved = false;
        Point newPoint = new Point(pos);
        points.add(newPoint);
        PanelLog.info("точка " + newPoint + " добавлена");
    }
    /**
     * Клик мыши по пространству задачи
     *
     * @param pos         положение мыши
     * @param mouseButton кнопка мыши
     */
    public void click(Vector2i pos, MouseButton mouseButton) {
        if (lastWindowCS == null) return;
        // получаем положение на экране
        Vector2d taskPos = ownCS.getCoords(pos, lastWindowCS);
        // если левая кнопка мыши, добавляем в первое множество
        if (mouseButton.equals(MouseButton.PRIMARY)) {
            addPoint(taskPos);
            // если правая, то во второе
        }
    }

    /**
     * Добавить случайные точки
     *
     * @param cnt кол-во случайных точек
     */
    public void addRandomPoints(int cnt) {
        // если создавать точки с полностью случайными координатами,
        // то вероятность того, что они совпадут крайне мала
        // поэтому нужно создать вспомогательную малую целочисленную ОСК
        // для получения случайной точки мы будем запрашивать случайную
        // координату этой решётки (их всего 30х30=900).
        // после нам останется только перевести координаты на решётке
        // в координаты СК задачи
        CoordinateSystem2i addGrid = new CoordinateSystem2i(30, 30);

        // повторяем заданное количество раз
        for (int i = 0; i < cnt; i++) {
            // получаем случайные координаты на решётке
            Vector2i gridPos = addGrid.getRandomCoords();
            // получаем координаты в СК задачи
            Vector2d pos = ownCS.getCoords(gridPos, addGrid);
            addPoint(pos);
        }
    }
    /**
     * Рисование сетки
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void renderGrid(Canvas canvas, CoordinateSystem2i windowCS) {
        // сохраняем область рисования
        canvas.save();
        // получаем ширину штриха(т.е. по факту толщину линии)
        float strokeWidth = 0.03f / (float) ownCS.getSimilarity(windowCS).y + 0.5f;
        // создаём перо соответствующей толщины
        try (var paint = new Paint().setMode(PaintMode.STROKE).setStrokeWidth(strokeWidth).setColor(TASK_GRID_COLOR)) {
            // перебираем все целочисленные отсчёты нашей СК по оси X
            for (int i = (int) (ownCS.getMin().x); i <= (int) (ownCS.getMax().x); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(i, 0, ownCS);
                // каждый 10 штрих увеличенного размера
                float strokeHeight = i % DELIMITER_ORDER == 0 ? 5 : 2;
                // рисуем вертикальный штрих
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x, windowPos.y + strokeHeight, paint);
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x, windowPos.y - strokeHeight, paint);
            }
            // перебираем все целочисленные отсчёты нашей СК по оси Y
            for (int i = (int) (ownCS.getMin().y); i <= (int) (ownCS.getMax().y); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(0, i, ownCS);
                // каждый 10 штрих увеличенного размера
                float strokeHeight = i % 10 == 0 ? 5 : 2;
                // рисуем горизонтальный штрих
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x + strokeHeight, windowPos.y, paint);
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x - strokeHeight, windowPos.y, paint);
            }
        }
        // восстанавливаем область рисования
        canvas.restore();
    }

    /**
     * Очистить задачу
     */
    public void clear() {
        points.clear();
        solved = false;
    }
    /**
     * Наибольшее множество
     */
    @Getter
    @JsonIgnore
    private ArrayList<Point> maxset = new ArrayList<>();
    /**
     * Решить задачу
     */
    public void solve() {
        //очищаем списки
        triangles.clear();
        maxset.clear();
        //избавляемся от совпадающих точек
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                if (points.get(i).equals(points.get(j))) {
                    points.remove(j);
                }
            }
        }
        //обнуляем счетчик треугольников у каждой точки
        for (Point point : points) {
            point.count = 0;
        }
        //матрица смежности (соединены ли точки стороной треугольника)
        int[][] matrix = new int[points.size()][points.size()];
        //перебираем все точки
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                for (int k = j + 1; k < points.size(); k++) {
                    //рисуем треугольник по трем точкам
                    Triangle t = new Triangle(points.get(i), points.get(j), points.get(k));
                    //если правильный - увеличиваем счетчик и значение в матрице смежности
                    if (t.isRight()) {
                        triangles.add(t);
                        matrix[i][j]++;
                        matrix[j][i]++;
                        matrix[i][k]++;
                        matrix[k][i]++;
                        matrix[j][k]++;
                        matrix[k][j]++;
                        points.get(i).count++;
                        points.get(j).count++;
                        points.get(k).count++;
                    }
                }
            }
        }
        //отбрасываем точки, являющиеся вершиной только одного треугольника
        //эти точки не войдут в финальное множество, а треугольник, построенный
        //на этой точке не удовлетворяет условию, значит понижаем счетчик двух оставшихся вершин
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < points.size(); i++) {
                if (points.get(i).count == 1) {
                    points.get(i).count--;
                    for (int j = 0; j < points.size(); j++) {
                        if (matrix[i][j] > 0) {
                            points.get(j).count--;
                        }
                    }
                }
            }
            //пока не останутся точки либо из множеств (count>=2), либо остальные
            for (Point point : points) {
                if (point.count == 1) {
                    flag = true;
                    break;
                }
            }
        }
        //заново перебираем все точки
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).count >= 2) {
                //если есть два треугольника из условия, создаем подмножество точек
                ArrayList<Point> set = new ArrayList<>();
                set.add(points.get(i));
                //перебираем все связанные с ней точки по матрице и добавляем их в подмножество
                for (int j = 0; j < points.size(); j++) {
                    if ((matrix[i][j] > 0)  && (points.get(j).count >= 2)){
                        set.add(points.get(j));
                    }
                }
                //перебираем все точки подмножества, новые добавляем в конец списка
                //перебираем, пока не дойдем до конца подмножества
                for (int j = 0; j < set.size(); j++) {
                    for (int k = 0; k < points.size(); k++) {
                        if ((matrix[points.indexOf(set.get(j))][k] > 0) && (points.get(k).count >= 2))
                            if (!set.contains(points.get(k)))
                                set.add(points.get(k));
                    }
                }
                //сравниваем с максимумом
                if (set.size() > maxset.size())
                    maxset = set;
            }
        }
        solved = true;
    }

    /**
     * Отмена решения задачи
     */
    public void cancel() {
        solved = false;
    }

    /**
     * проверка, решена ли задача
     *
     * @return флаг
     */
    public boolean isSolved() {
        return solved;
    }
    /**
     * Масштабирование области просмотра задачи
     *
     * @param delta  прокрутка колеса
     * @param center центр масштабирования
     */
    public void scale(float delta, Vector2i center) {
        if (lastWindowCS == null) return;
        // получаем координаты центра масштабирования в СК задачи
        Vector2d realCenter = ownCS.getCoords(center, lastWindowCS);
        // выполняем масштабирование
        ownCS.scale(1 + delta * WHEEL_SENSITIVE, realCenter);
    }
    /**
     * Получить положение курсора мыши в СК задачи
     *
     * @param x        координата X курсора
     * @param y        координата Y курсора
     * @param windowCS СК окна
     * @return вещественный вектор положения в СК задачи
     */
    @JsonIgnore
    public Vector2d getRealPos(int x, int y, CoordinateSystem2i windowCS) {
        return ownCS.getCoords(x, y, windowCS);
    }
    /**
     * Рисование курсора мыши
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     * @param font     шрифт
     * @param pos      положение курсора мыши
     */
    public void paintMouse(Canvas canvas, CoordinateSystem2i windowCS, Font font, Vector2i pos) {
        // создаём перо
        try (var paint = new Paint().setColor(TASK_GRID_COLOR)) {
            // сохраняем область рисования
            canvas.save();
            // рисуем перекрестие
            canvas.drawRect(Rect.makeXYWH(0, pos.y - 1, windowCS.getSize().x, 2), paint);
            canvas.drawRect(Rect.makeXYWH(pos.x - 1, 0, 2, windowCS.getSize().y), paint);
            // смещаемся немного для красивого вывода текста
            canvas.translate(pos.x + 3, pos.y - 5);
            // положение курсора в пространстве задачи
            Vector2d realPos = getRealPos(pos.x, pos.y, lastWindowCS);
            // выводим координаты
            canvas.drawString(realPos.toString(), 0, 0, font, paint);
            // восстанавливаем область рисования
            canvas.restore();
        }
    }
}