package com.natifick.theexamination;

import android.graphics.Color;

enum Type{
    Cell2, Cell3, Cell4, Cell5, Failed, Resit, Mew;
    public static int length(){
        return 4;
    } // пока что мы не будем использовать все
    public static int getColor(Type type){
        switch(type){
            case Resit:
            case Failed:
            case Cell2: return Color.RED;
            case Cell3:
            case Cell5: return Color.GREEN;
            default:
                return Color.YELLOW; // и для 4 тоже
        }
    }
}

public class Cell {

    public int CellX, CellY, CellR,
    speedX, speedY;
    /** Чтобы не просчитывать выход из поля головы босса  */
    public short invinc;
    /**  информация об основном поле  */
    private int FieldWidth, FieldHeight;

    /**  если точку нужно удалить */
    public boolean dead = false;

    Type CellType;

    public Cell(Type type, int speedX, int speedY, int w, int h){
        CellType = type;
        this.speedX = speedX;
        this.speedY = speedY;
        this.FieldWidth = w-GameView.PADDING_X*2;
        this.FieldHeight = h-GameView.PADDING_Y*2;
        CellX = (int)(FieldWidth/2.0);
        CellY = (int)(FieldHeight/2.0);
    }

    public void Move(){
        CellX+= speedX;
        CellY+= speedY;
        if (CellX+CellR <= 0 || CellY+CellR <= 0 || CellX-CellR >= FieldWidth || CellY-CellR >= FieldHeight){
            dead = true;
        }
    }
}
