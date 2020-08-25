package com.natifick.theexamination;


public class Board {
    /**  информация об игроке и его доске  */
    public int LeftX, LeftY, RightX, RightY,
            Health,
            DestinationX, DestinationY,
            BoardWidth, BoardHeight,
            speed,
            IsMoving; // -1 - налево, 0 - не двигается, 1 - направо
    float angle;
    /**  информация об основном поле  */
    private int FieldWidth, FieldHeight;
    public Board(int w, int h){
        this.FieldWidth = w-GameView.PADDING*2;
        this.FieldHeight = h-GameView.PADDING*2;
        BoardWidth = (int) (FieldWidth/5.0);
        BoardHeight = (int) (BoardWidth/4.0);

        RightX = (int) ((FieldWidth-BoardWidth)/2.0);
        LeftX = (int) ((FieldWidth+BoardWidth)/2.0);
        RightY = 0;
        LeftY = 0;

        Health = 100;
        speed = 10;
        angle = 0;

        // ТОЛЬКО ВРЕМЕННО!
        IsMoving = 1;
        DestinationX = 0;
        DestinationY = 0;
    }

    public void Move(){ // При движении точка не знает об отступах от границ
        if (IsMoving==0){ // если не двигаемся, то выходим из функции
            return;
        }
        int TempRightX = RightX, TempRightY = RightY, TempLeftX = LeftX, TempLeftY = LeftY;
        float TempAngle = angle;
        if(RightY==0){ // мы сверху
            TempRightX = RightX - speed*IsMoving;
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
                TempLeftY = (int)Math.sqrt(Math.pow(BoardWidth, 2)-Math.pow(FieldWidth-TempRightX, 2));
                TempAngle = (float)Math.toDegrees(Math.asin(TempLeftY/((float)BoardWidth)));
            }
        }
        else if (RightX == FieldWidth){ // мы на правой стороне
            TempRightY = RightY - speed*IsMoving;

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
                TempLeftX = FieldWidth-(int)Math.sqrt(Math.pow(BoardWidth, 2)-Math.pow(FieldHeight-TempRightY, 2));
                TempAngle = 90+(float)Math.toDegrees(Math.acos((FieldHeight-TempRightY)/((float)BoardWidth)));
            }
        }
        else if  (RightY == FieldHeight){ // мы на нижней стороне
            TempRightX = RightX + speed*IsMoving;

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
                TempLeftY = FieldHeight-(int)Math.sqrt(Math.pow(BoardWidth, 2)-Math.pow(TempRightX, 2));
                TempAngle = 180+(float)Math.toDegrees(Math.acos(TempRightX/((float)BoardWidth)));
            }
        }
        else if (RightX==0){ // мы на левой стороне
            TempRightY = RightY + speed*IsMoving;

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
                TempLeftX = (int)Math.sqrt(Math.pow(BoardWidth, 2)-Math.pow(TempRightY, 2));
                TempAngle = 360-(float)Math.toDegrees(Math.acos(TempLeftX/((float)BoardWidth)));
            }

        }


        double dist = Math.sqrt(Math.pow(DestinationX-(TempLeftX+TempRightX)/2.0, 2)+Math.pow(DestinationY-(TempLeftY+TempRightY)/2.0, 2));
        double distR = Math.sqrt(Math.pow(DestinationX-TempRightX, 2)+Math.pow(DestinationY-TempRightY, 2));
        double distL = Math.sqrt(Math.pow(DestinationX-TempLeftX, 2)+Math.pow(DestinationY-TempLeftY, 2));
        if (dist < Math.sqrt(Math.pow(DestinationX-(LeftX+RightX)/2.0, 2)+Math.pow(DestinationY-(LeftY+RightY)/2.0, 2)) ||
        dist > BoardWidth || Math.abs(distL-distR)>=speed*2){
            LeftX = TempLeftX;
            LeftY = TempLeftY;
            RightX = TempRightX;
            RightY = TempRightY;
            angle = TempAngle;
        }
        else{
            IsMoving = 0;
        }
    }

}
