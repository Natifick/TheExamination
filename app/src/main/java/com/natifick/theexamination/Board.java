package com.natifick.theexamination;


import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.ListIterator;

/** перечисление для направления движения*/
enum dir{
    Left, No, Right;
}
public class Board {
    /**  информация об игроке и его доске  */
    public int LeftX, LeftY, RightX, RightY,
            Health,
            DestinationX, DestinationY,
            BoardWidth, BoardHeight;

    /** не перечисление, чтобы можно было вычислять */
    dir Moving;
    public final int speed = 20;
    float angle, AttackAngle;
    /**  информация об основном поле  */
    public int FieldWidth, FieldHeight;

    public Board(int w, int h){
        this.FieldWidth = w-GameView.PADDING_X*2;
        this.FieldHeight = h-GameView.PADDING_Y*2;
        BoardWidth = (int) (FieldWidth/5.0);
        BoardHeight = (int) (BoardWidth/4.0);

        AttackAngle = (float) Math.toDegrees(Math.acos(1.0/Math.sqrt(1+Math.pow(BoardWidth/(float)FieldWidth, 2))));

        RightX = (int) ((FieldWidth-BoardWidth)/2.0);
        LeftX = (int) ((FieldWidth+BoardWidth)/2.0);
        RightY = 0;
        LeftY = 0;

        Health = 100;
        angle = 0;
        Moving = dir.No;
        DestinationX = (int)(FieldWidth/2.0);
        DestinationY = 0;
    }

    /**  сталкиваемся со "снарядом"  */
    public void collide(ArrayList<Cell> cells){
        if (cells.size()>0) {
            double X2, Y2, X1 = (LeftX-RightX)/(double)BoardWidth, Y1 = (LeftY-RightY)/(double)BoardWidth;
            if (Y1!=0){
                X2 = (LeftX <= FieldWidth/2.0?1:-1)*Math.abs(Y1);
                Y2 = -X2*X1/Y1;
            }
            else{
                Y2 = (LeftY <= FieldHeight/2.0?1:-1)*Math.abs(X1);
                X2 = -Y2*Y1/X1;
            }
            //       ________________________
            //       |         |Top          |           /|\
            //Right  |                       | - Left     |    ____\
            //       |         Bottom        |           X2    X1  /
            //       |_________|_____________|
            // Формула расстояния до прямой: Ax+By+C или x*Y1 - y*X1 + (Y0*X1 - X0*Y1)

            double bottom, top, left, right;

            double distL = LeftY*X2-LeftX*Y2;
            double distM = (LeftY+Y2*BoardHeight)*X1-(LeftX+X2*BoardHeight)*Y1;
            double distR = RightY*X2-RightX*Y2;

            double PrLen; // Длина проекции
            // Для определения расстояния между точками используем функцию
            for (Cell c: cells){
                // для ускорения вычислений с большим количеством снарядов проверяем с какого расстояния вообще нужно вычислять
                if (!(Math.pow(LeftX-c.CellX, 2) + Math.pow(LeftY-c.CellY, 2)<=
                        Math.pow(X1*(BoardWidth+c.CellR)+X2*(BoardHeight+c.CellR), 2) + Math.pow(Y1*(BoardWidth+c.CellR)+Y2*(BoardHeight+c.CellR), 2))){
                    continue;
                }

                bottom = c.CellX*Y1-c.CellY*X1 + (LeftY-Y2*c.CellR)*X1 - (LeftX-X2*c.CellR)*Y1;
                top = c.CellX*Y1-c.CellY*X1 + (LeftY+Y2*(BoardHeight+c.CellR))*X1 - (LeftX+X2*(BoardHeight+c.CellR))*Y1;
                // На самом деле bottom мы использовать не будем, лишь для проверки попадения внутрь прямоугольника
                left = c.CellX*Y2-c.CellY*X2 + (LeftY+Y1*c.CellR)*X2 - (LeftX-X1*c.CellR)*Y2;
                right = c.CellX*Y2-c.CellY*X2 + (RightY-Y1*c.CellR)*X2 - (RightX-X1*c.CellR)*Y2;

                if(Math.signum(bottom) != Math.signum(top) && Math.signum(left) != Math.signum(right)){
                    switch(c.CellType){
                        case Cell2:
                            Health -= 5;
                            c.dead =true;
                            break;
                        case Failed:
                        case Resit:
                            Health -= 6;
                            c.dead = true;
                            break;
                        case Mew:
                            Health -= 3;
                            c.dead = true;
                            break;
                        default:
                            if (c.invinc==0){
                                if (Math.abs(top)<=Math.abs(left) && Math.abs(top)<=Math.abs(right)){
                                    PrLen = c.speedX*X2 + c.speedY*Y2;
                                    c.speedX -= 2*PrLen*X2;
                                    c.speedY -= 2*PrLen*Y2;
                                }
                                else if (Math.abs(right)<=Math.abs(left) && Math.abs(right) <=Math.abs(top)){
                                    PrLen = c.speedX*X1 + c.speedY*Y1;
                                    c.speedX += 2*PrLen*X1;
                                    c.speedY += 2*PrLen*Y1;
                                }
                                else if (Math.abs(left)<=Math.abs(right) && Math.abs(left) <= Math.abs(top)){
                                    PrLen = c.speedX*X1 + c.speedY*Y1;
                                    c.speedX -= 2*PrLen*X1;
                                    c.speedY -= 2*PrLen*Y1;
                                }
                                else{ // Такая ситуация, конечно, не возникнет
                                    PrLen = c.speedX*X2 + c.speedY*Y2;
                                    c.speedX += 2*PrLen*X2;
                                    c.speedY += 2*PrLen*Y2;
                                }
                                c.invinc = 4; // примерное число тактов, которые можем пропустить
                            }
                            else{
                                c.invinc--;
                            }
                    }
                }
            }
        }
    }


    /** По координате касания вычисляем, куда должна двигаться доска */
    public void CountTarget(int X, int Y){

        // Если мы ниже или выше границ, то смещаем в пределы границ
        int minX = X, minY = Y;

        if (X>FieldWidth) X = FieldWidth;
        else if (X <0) X = 0;
        if (Y>FieldHeight) Y = FieldHeight;
        else if (Y<0) Y=0;


        // Находим к какой из точек на рельсах мы ближе всего
        if (X > FieldWidth-X){
            minX = FieldWidth-X;
        }
        if (Y > FieldHeight-Y){
            minY =  FieldHeight-Y;
        }

        if (minX > minY){ // Если мы ближе к Y, то мы должны сохранить X, округлив Y
            Y = FieldHeight*(int)(2*Y/((float)FieldHeight));
        }
        else{
            X = FieldWidth*(int)(2*X/((float)FieldWidth));
        }

        if (X == DestinationX && Y == DestinationY){ // зачем вновь вычислять одно и то же?
            return;
        }

        DestinationY = Y;
        DestinationX = X;

        // Если мы уже на одной стороне с нужной точкой (это для X)
        if (LeftX==X || RightX == X){
            if (LeftX!=LeftY){ // Если при этом разные края доски на разных частях
                if (Math.abs(LeftY-Y)>Math.abs(RightY-Y)){ // Смотрим какой из них ближе
                    Moving = dir.values()[1+(int)Math.signum((1-X)*(Y-RightY))];
                }
                else{
                    Moving = dir.values()[1+(int)Math.signum((1-X)*(Y-LeftY))];
                }
            }
            else{
                int middle = (int)((RightY+LeftY)/2.0);
                Moving = dir.values()[1+(int)Math.signum((1-Y)*(middle-Y))];
            }
            return;
        }
        else if (LeftY==Y || RightY == Y){ // Если мы на одной стороне с нужной точкой (для Y)
            if (LeftY != RightY){ // Если при этом края на разных сторонах
                if (Math.abs(LeftX-X)>Math.abs(RightX-X)){ // Смотрим какой ближе
                    Moving = dir.values()[1+(int)Math.signum((1-Y)*(RightX-X))];
                }
                else{
                    Moving = dir.values()[1+(int)Math.signum((1-Y)*(LeftX-X))];
                }
            }
            else{
                int middle = (int)((RightX + LeftX)/2.0);
                Moving = dir.values()[1+(int)Math.signum((1-Y)*(middle-X))];
            }
            return;
        }

        // Пытаемся пройти всегда двигаясь направо
        // Когда находимся на одной стороне,
        // отсчитываем расстояние до точки
        int pathR = 0;
        Y = RightY;
        X = RightX;
        while (Y != DestinationY && X != DestinationX){
            if (Y==0){
                pathR+=X;
                X = 0;
                Y = 1;
            }
            else if (X==0){
                pathR+= FieldHeight-Y;
                Y = FieldHeight;
                X = 1;
            }
            else if (Y==FieldHeight){
                pathR += FieldWidth-X;
                X = FieldWidth;
                Y = FieldHeight-1;
            }
            else{
                pathR += Y;
                X = FieldWidth-1;
                Y = 0;
            }
            if (pathR>FieldHeight+FieldWidth){ // зачем считать дальше, если итак очевидно?
                Moving=dir.Left;
                return;
            }
        }
        if (X==DestinationX){
            if (X==0){
                pathR+= DestinationY-Y;
            }
            else{
                pathR+= Y-DestinationY;
            }
        }
        else {
            if (Y==0){
                pathR += X-DestinationX;
            }
            else{
                pathR += DestinationX-X;
            }
        }
        if (pathR>FieldHeight+FieldWidth){ // зачем считать дальше, если итак очевидно?
            Moving=dir.Left;
        }
        else{
            Moving=dir.Right;
        }
    }


    /** Считаем куда передвинется доска за 1 такт и если мы достигли "цели", то останавливаемся  */
    public void Move(){ // При движении точка не знает об отступах от границ
        if (Moving==dir.No){ // если не двигаемся, то выходим из функции
            return;
        }
        int TempRightX = RightX, TempRightY = RightY, TempLeftX = LeftX, TempLeftY = LeftY;
        float TempAngle = angle;
        if(RightY==0){ // мы сверху
            TempRightX = RightX - speed*(Moving.ordinal()-1);
            TempRightY = RightY;

            // если проехали за пределы стороны, то перемещаемся на другую
            // проехали сильно влево
            if (TempRightX <= 0){
                TempRightX = 0;
                TempRightY = 1;
            }
            // проехали сильно вправо
            if (TempRightX >= FieldWidth){
                TempRightX = FieldWidth;
                TempRightY = 1;
            }
            // углы на разных сторонах
            if (TempRightX <= FieldWidth-BoardWidth) {
                TempLeftX = TempRightX + BoardWidth;
                TempLeftY = 0;
                TempAngle = 0;
            }
            else {
                TempLeftX = FieldWidth;
                TempLeftY = (int)Math.round(Math.sqrt(Math.pow(BoardWidth, 2)-Math.pow(FieldWidth-TempRightX, 2)));
                TempAngle = (float)Math.toDegrees(Math.asin(TempLeftY/((float)BoardWidth)));
            }
        }
        else if (RightX == FieldWidth){ // мы на правой стороне
            TempRightY = RightY - speed*(Moving.ordinal()-1);

            // если проехали за пределы стороны, то перемещаемся на другую
            // проехали сильно вверх
            if (TempRightY <= 0){
                TempRightY = 0;
                TempRightX = FieldWidth-1;
            }
            // проехали сильно вниз
            if (TempRightY >= FieldHeight){
                TempRightY = FieldHeight;
                TempRightX = FieldWidth-1;
            }
            // углы на разных сторонах
            if (TempRightY <= FieldHeight-BoardWidth) {
                TempLeftY = TempRightY + BoardWidth;
                TempLeftX = FieldWidth;
                TempAngle = 90;
            }
            else {
                TempLeftY = FieldHeight;
                TempLeftX = FieldWidth-(int)Math.round(Math.sqrt(Math.pow(BoardWidth, 2)-Math.pow(FieldHeight-TempRightY, 2)));
                TempAngle = 90+(float)Math.toDegrees(Math.acos((FieldHeight-TempRightY)/((float)BoardWidth)));
            }
        }
        else if  (RightY == FieldHeight){ // мы на нижней стороне
            TempRightX = RightX + speed*(Moving.ordinal()-1);

            // если проехали за пределы стороны, то перемещаемся на другую
            // проехали сильно влево
            if (TempRightX <= 0){
                TempRightX = 0;
                TempRightY = FieldHeight-1;
            }
            // проехали сильно вправо
            if (TempRightX >= FieldWidth){
                TempRightX = FieldWidth;
                TempRightY = FieldHeight-1;
            }
            // углы на разных сторонах
            if (TempRightX >= BoardWidth) {
                TempLeftX = TempRightX - BoardWidth;
                TempLeftY = FieldHeight;
                TempAngle = 180;
            }
            else {
                TempLeftX = 0;
                TempLeftY = FieldHeight-(int)Math.round(Math.sqrt(Math.pow(BoardWidth, 2)-Math.pow(TempRightX, 2)));
                TempAngle = 180+(float)Math.toDegrees(Math.acos(TempRightX/((float)BoardWidth)));
            }
        }
        else if (RightX==0){ // мы на левой стороне
            TempRightY = RightY + speed*(Moving.ordinal()-1);

            // если проехали за пределы стороны, то перемещаемся на другую
            // проехали сильно вниз
            if (TempRightY >= FieldHeight){
                TempRightY = FieldHeight;
                TempRightX = 1;
            }
            // проехали сильно вверх
            if (TempRightY <= 0){
                TempRightY = 0;
                TempRightX = 1;
            }
            // углы на разных сторонах
            if (TempRightY >= BoardWidth) {
                TempLeftY = TempRightY - BoardWidth;
                TempLeftX = 0;
                TempAngle = 270;
            }
            else {
                TempLeftY = 0;
                TempLeftX = (int)Math.round(Math.sqrt(Math.pow(BoardWidth, 2)-Math.pow(TempRightY, 2)));
                TempAngle = 360-(float)Math.toDegrees(Math.acos(TempLeftX/((float)BoardWidth)));
            }
        }
        // Если мы только что приблизились к точке назначения на нужное расстояние
        if (Math.sqrt(Math.pow(DestinationX-TempRightX, 2)+Math.pow(DestinationY-TempRightY, 2)) >= BoardWidth/Math.sqrt(2) ||
                Math.sqrt(Math.pow(DestinationX-TempLeftX, 2)+Math.pow(DestinationY-TempLeftY, 2)) >= BoardWidth/Math.sqrt(2)){
            LeftX = TempLeftX;
            LeftY = TempLeftY;
            RightX = TempRightX;
            RightY = TempRightY;
            angle = TempAngle;
        }
        else{
            Moving = dir.No;
        }
    }

}
