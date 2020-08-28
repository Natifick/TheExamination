package com.natifick.theexamination;


import android.view.MotionEvent;

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
    public final int speed = 18;
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
    /** По координате касания вычисляем, куда должна двигаться доска */
    public void CountTarget(int X, int Y){

        // Находим к какой из точек на рельсах мы ближе всего
        int minX = X, minY = Y;
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

    public void collide(Cell cell){

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
