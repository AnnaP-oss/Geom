package app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import misc.Vector2d;
/**
 * Класс треугольника
 */
public class Triangle {
    /**
     * Вершины треугольника
     */
    public final Point pointA;
    public final Point pointB;
    public final Point pointC;
    /**
     * Конструктор точки
     * @param pointA   вершина треугольника
     * @param pointB   вершина треугольника
     * @param pointC   вершина треугольника
     */
    @JsonCreator
    public Triangle(@JsonProperty("pointA") Point pointA, @JsonProperty("pointB") Point pointB, @JsonProperty("pointC") Point pointC) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;
    }
    /**
     * Проверка на правильность треугольника
     *
     * @return правильный ли треугольник
     */
    public boolean isRight () {
        double ab = Math.sqrt((this.pointA.pos.x - this.pointB.pos.x)*(this.pointA.pos.x - this.pointB.pos.x) + (this.pointA.pos.y - this.pointB.pos.y)*(this.pointA.pos.y - this.pointB.pos.y));
        double ac = Math.sqrt((this.pointA.pos.x - this.pointC.pos.x)*(this.pointA.pos.x - this.pointC.pos.x) + (this.pointA.pos.y - this.pointC.pos.y)*(this.pointA.pos.y - this.pointC.pos.y));
        double bc = Math.sqrt((this.pointC.pos.x - this.pointB.pos.x)*(this.pointC.pos.x - this.pointB.pos.x) + (this.pointC.pos.y - this.pointB.pos.y)*(this.pointC.pos.y - this.pointB.pos.y));
        return Math.abs(ab - ac) <= 0.1 && Math.abs(ab - bc) <= 0.1 && Math.abs(bc - ac) <= 0.1;
        //очень трудно поставить точки так, чтобы длины сторон в точности совпадали
        //поэтому считаем треугольник правильным, если его стороны отличаются не более, чем на 0.1
    }
}
