package com.natifick.theexamination;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.ArrayList;

public class Boss {
    int speed, radius;
    float angle;
    Bitmap ImgLeft, ImgMiddle, ImgRight; // изображения для босса
    dir Moving;
    short CurPic;
    Board board;
    private double VecX, VecY;
    int FieldWidth, FieldHeight;
    short Health;

    short AttackRate = 14; // Насколько часто вылетают снаряды? (больше - реже)
    short Relief = 0;
    boolean Attack; // Атакуем ли мы в данный момент

    public Boss(int w, int h, Board board) {
        this.board = board;
        FieldWidth = w - GameView.PADDING_X * 2;
        FieldHeight = h - GameView.PADDING_Y * 2;
        Attack = false;
        angle = 0;
        Moving = dir.No;
        Health = 100;
        speed=1;
        this.ImgLeft = BitmapFactory.decodeResource(GameView.res, R.drawable.karpov_left);
        this.ImgMiddle = BitmapFactory.decodeResource(GameView.res, R.drawable.karpov_middle);
        this.ImgRight = BitmapFactory.decodeResource(GameView.res, R.drawable.karpov_right);
        CurPic = 0;
        radius = (int)(ImgLeft.getHeight()/2.0);
    }

    public Cell Attack(){
        if (Attack){
            Relief = (short)((Relief+1)%AttackRate);
            if (Relief==0){ // уменьшаем вероятность атаки
                return new Cell(Type.values()[(int)(Math.random()*(Type.length()+1))], (float)VecX*13, (float)VecY*13, FieldWidth, FieldHeight, radius);
            }
        }
        else{
            Relief = (short)(AttackRate-1);
        }

        return null;
    }

    public void collide(ArrayList<Cell> cells){
        if (cells.size()>0){
            for (Cell c: cells){
                if (Math.pow(c.CellX-FieldWidth/2.0, 2) + Math.pow(c.CellY-FieldHeight/2.0, 2) <= (radius+c.CellR)*(radius+c.CellR)) {
                    if (c.invinc>0){
                        c.invinc--;
                    }
                    else{
                        switch (c.CellType) {
                            case Cell3:
                                Health -= 3;
                                break;
                            case Cell4:
                                Health -= 4;
                                break;
                            case Cell5:
                                Health -= 5;
                                break;
                            default:
                                break;
                        }
                        c.dead = true;
                    }
                }
            }
        }
    }


    public void Move(){
        VecX = -Math.cos(Math.toRadians(angle)); // Вектор, параллельный рту босса
        VecY = -Math.sin(Math.toRadians(angle));

        double AngleLeft, AngleMid1, AngleMid2, AngleRight;
        // Считаем углы между ртом и левым и правым бортом доски
        AngleLeft = Math.toDegrees(Math.acos((VecX*(board.LeftX-board.FieldWidth/2.0)+VecY*(board.LeftY-board.FieldHeight/2.0))/
                Math.sqrt(Math.pow(board.LeftX-board.FieldWidth/2.0, 2)+Math.pow(board.LeftY-board.FieldHeight/2.0, 2))));
        AngleRight = Math.toDegrees(Math.acos((VecX*(board.RightX-board.FieldWidth/2.0)+VecY*(board.RightY-board.FieldHeight/2.0))/
                Math.sqrt(Math.pow(board.RightX-FieldWidth/2.0, 2)+Math.pow(board.RightY-FieldHeight/2.0, 2))));

        // Находим угол между ртом и центром
        AngleMid1 = Math.toDegrees(Math.acos((VecX*(board.RightX+board.LeftX-FieldWidth)+VecY*(board.RightY+board.LeftY-FieldHeight))/
                Math.sqrt(Math.pow(board.RightX+board.LeftX-FieldWidth, 2)+Math.pow(board.RightY+board.LeftY-FieldHeight, 2))));

        // А второй между центром доски и перпендикуляром ко рту, чтобы было легче считать, куда поворачиваться
        // Ведь мы вычисляем из косинуса угла, а значит обрезаем нижнюю часть поля
        AngleMid2 = Math.toDegrees(Math.acos((-VecY*(board.RightX+board.LeftX-FieldWidth)+VecX*(board.RightY+board.LeftY-FieldHeight))/
                Math.sqrt(Math.pow(board.RightX+board.LeftX-FieldWidth, 2)+Math.pow(board.RightY+board.LeftY-FieldHeight, 2))));

        // Если рот под нужным углом для атаки (
        if (AngleLeft+AngleRight <= board.AttackAngle*2){
            CurPic = -1;
            Attack = true;
            if (AngleMid2 <= 90+speed && AngleMid2 >= 90-speed){
                Moving = dir.No;
            }
        }
        else if (AngleLeft+AngleRight >= (180-board.AttackAngle)*2){
            CurPic = 1;
            VecX = -VecX;
            VecY = -VecY;
            Attack = true;
            if (AngleMid2 <= 90+speed && AngleMid2 >= 90-speed){
                Moving = dir.No;
            }
        }
        else{
            if(AngleMid2 > 90 && AngleMid1 < 90 || AngleMid2 < 90 && AngleMid1 > 90){
                Moving = dir.Left;
            }
            else{
                Moving = dir.Right;
            }
            CurPic = 0;
            Attack = false;
        }

        if (Moving==dir.No){
            return;
        }

        angle = (angle+speed*(Moving.ordinal()-1)+360)%360;
    }

}
