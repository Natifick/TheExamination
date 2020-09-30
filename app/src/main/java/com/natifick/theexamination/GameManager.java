package com.natifick.theexamination;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import java.util.LinkedList;


public class GameManager extends Thread{
    /**  Объект класса  */
    private GameView view;

    /**  частота смены кадров, меньше - лучше  */
    int FPS = 40;

    /** Список всех подклассов */
    Board board;
    LinkedList<Cell> cells;
    Boss boss;

    /** Картинка нашего героя будет всегда разной */
    Bitmap ImgPerson;

    /**  Переменная для задания состояния потока отрисовки  */
    private boolean running = false;

    /**  Конструктор класса  */
    public GameManager(GameView view)
    {
        this.view = view;
    }

    void consider(){
        switch((int)(Math.random()*4.0)){
            case 0:
                ImgPerson = BitmapFactory.decodeResource(GameView.res, R.drawable.ilya);
                break;
            case 1:
                ImgPerson = BitmapFactory.decodeResource(GameView.res, R.drawable.max);
                break;
            case 3:
                ImgPerson = BitmapFactory.decodeResource(GameView.res, R.drawable.olya);
                break;
        }
        ImgPerson = Bitmap.createScaledBitmap(ImgPerson, (int)(GameView.PADDING_Y /2.0), (int)(GameView.PADDING_Y /2.0), true);
    }

    /**  Задание состояния потока  */
    public void setRunning(boolean run)
    {
        running = run;
    }

    /**  Действия, выполняемые в потоке  */
    public void run() {
        long ticksPS = 500 / FPS;
        Canvas c = null;

        while (running) {
            c = null;
            try {
                c = view.getHolder().lockCanvas();
                synchronized (view.getHolder()) {
                    view.draw(c);
                }
            } finally {
                if (c != null) {
                    view.getHolder().unlockCanvasAndPost(c);
                }
            }
            try {
                sleep(ticksPS);
            } catch (Exception e) {}

            if (boss.Health == 0 || board.Health == 0){
                break;
            }

            // Перемещаем элементы на поле
            board.Move();
            boss.Move();

            if (cells != null){
                board.collide(cells);
                boss.collide(cells);
                for (int i=0;i<cells.size();i++){
                    cells.get(i).Move(); // Двигаем каждую клетку и проверяем, за пределами ли она поля
                    if (cells.get(i).dead){
                        cells.remove(i);
                        i--;
                    }
                }
            }
            Cell newc = boss.Attack();
            if (newc != null){
                cells.add(newc);
            }

            // Чтобы здоровье не продолжало отрисовываться в минус делаем ReLU
            if (board.Health < 0) board.Health = 0;
            if (boss.Health < 0) boss.Health = 0;
        }
        c = null;
        try {
            c = view.getHolder().lockCanvas();
            synchronized (view.getHolder()) {
                view.findraw(c);
            }
        } finally {
            if (c != null) {
                view.getHolder().unlockCanvasAndPost(c);
            }
        }
        try {
            sleep(ticksPS);
        } catch (Exception e) {}
    }
}
