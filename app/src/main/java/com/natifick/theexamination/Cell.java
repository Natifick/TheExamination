package com.natifick.theexamination;

import android.graphics.Color;

enum Type{
    Cell2, Cell3, Cell4, Cell5, Failed, Resit, Mew;
    public static int length(){
        return 4;
    }
    public static int getColor(Type type){
        switch(type){
            case Resit:
            case Failed:
            case Cell2: return Color.RED;
            case Cell4:
            case Cell5: return Color.GREEN;
            default:
                return Color.YELLOW; // и для 4 тоже
        }
    }
    public static String getName(Type type){
        switch (type){
            case Cell2:
                return "2";
            case Cell3:
                return "3";
            case Cell4:
                return "4";
            case Cell5:
                return "5";
            case Failed:
                return "несдал";
            case Mew:
                return "мяу";
            case Resit:
                return "пересдача";
            default:
                return "";
        }
    }
}

public class Cell {

    public int CellX, CellY, CellR;
    float speedX, speedY;
    float angle;
    /** Чтобы не просчитывать выход из поля головы босса  */
    public short invinc;
    /**  информация об основном поле  */
    private int FieldWidth, FieldHeight;

    /**  если точку нужно удалить */
    public boolean dead = false;

    Type CellType;

    public Cell(Type type, float speedX, float speedY, int w, int h, int bossRadius){
        CellType = type;
        this.speedX = speedX;
        this.speedY = speedY;
        this.FieldWidth = w;
        this.FieldHeight = h;
        CellR = (int)(FieldWidth/50.0);
        CellX = (int)(FieldWidth/2.0);
        CellY = (int)(FieldHeight/2.0);
        invinc = (short)((bossRadius+CellR*2)/Math.sqrt(Math.pow(speedX, 2)+Math.pow(speedY, 2)));
    }

    public void Move(){
        CellX+= speedX;
        CellY+= speedY;
        if (CellX-CellR <= 0 || CellY-CellR <= 0 || CellX+CellR >= FieldWidth || CellY+CellR >= FieldHeight){
            dead = true;
        }
    }
}
