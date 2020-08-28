package com.natifick.theexamination;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Boss {
    int speed, radius;
    float angle;
    Bitmap ImgLeft, ImgMiddle, ImgRight; // изображения для босса
    dir Moving, Attack;
    short CurPic;
    Board board;
    double VecX, VecY;

    public Boss(int w, int h, Board board) {
        this.board = board;
        int FieldWidth = w - GameView.PADDING_X * 2;
        int FieldHeight = h - GameView.PADDING_Y * 2;
        Attack = dir.No;
        angle = 0;
        Moving = dir.No;
        speed=1;
        this.ImgLeft = BitmapFactory.decodeResource(GameView.res, R.drawable.karpov_left);
        this.ImgMiddle = BitmapFactory.decodeResource(GameView.res, R.drawable.karpov_middle);
        this.ImgRight = BitmapFactory.decodeResource(GameView.res, R.drawable.karpov_right);
        CurPic = 0;
        radius = (int)(ImgLeft.getHeight()/2.0);

    }

    public void Move(){
        VecX = -Math.cos(Math.toRadians(angle)); // Вектор, параллельный рту босса
        VecY = -Math.sin(Math.toRadians(angle));

        double AngleLeft, AngleMid1, AngleMid2, AngleRight;
        // Считаем углы между ртом и левым и правым бортом доски
        AngleLeft = Math.toDegrees(Math.acos((VecX*(board.LeftX-board.FieldWidth/2.0)+VecY*(board.LeftY-board.FieldHeight/2.0))/
                Math.sqrt(Math.pow(board.LeftX-board.FieldWidth/2.0, 2)+Math.pow(board.LeftY-board.FieldHeight/2.0, 2))));
        AngleRight = Math.toDegrees(Math.acos((VecX*(board.RightX-board.FieldWidth/2.0)+VecY*(board.RightY-board.FieldHeight/2.0))/
                Math.sqrt(Math.pow(board.RightX-board.FieldWidth/2.0, 2)+Math.pow(board.RightY-board.FieldHeight/2.0, 2))));

        // Находим угол между ртом и центром
        AngleMid1 = Math.toDegrees(Math.acos((VecX*(board.RightX+board.LeftX-board.FieldWidth)+VecY*(board.RightY+board.LeftY-board.FieldHeight))/
                Math.sqrt(Math.pow(board.RightX+board.LeftX-board.FieldWidth, 2)+Math.pow(board.RightY+board.LeftY-board.FieldHeight, 2))));

        // А второй между центром доски и перпендикуляром ко рту, чтобы было легче считать, куда поворачиваться
        // Ведь мы вычисляем из косинуса угла, а значит обрезаем нижнюю часть поля
        AngleMid2 = Math.toDegrees(Math.acos((-VecY*(board.RightX+board.LeftX-board.FieldWidth)+VecX*(board.RightY+board.LeftY-board.FieldHeight))/
                Math.sqrt(Math.pow(board.RightX+board.LeftX-board.FieldWidth, 2)+Math.pow(board.RightY+board.LeftY-board.FieldHeight, 2))));

        // Если рот под нужным углом для атаки (
        if (AngleLeft+AngleRight <= board.AttackAngle*2){
            CurPic = -1;
            if (AngleMid1 <= speed && AngleMid1 >= -speed || AngleMid1 <= 180+speed && AngleMid1 >= 180-speed){
                Moving = dir.No;
            }
        }
        else if (AngleLeft+AngleRight >= (180-board.AttackAngle)*2){
            CurPic = 1;
            if (AngleMid1 <= speed && AngleMid1 >= -speed || AngleMid1 <= 180+speed && AngleMid1 >= 180-speed){
                Moving = dir.No;
            }
        }
        else{
            if(AngleMid2 >= 90){
                Moving = dir.Left;
            }
            else{
                Moving = dir.Right;
            }
            CurPic = 0;
        }




        if (Moving==dir.No){
            return;
        }

        angle = (angle+speed*(Moving.ordinal()-1)+360)%360;
    }

}
