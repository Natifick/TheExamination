package com.natifick.theexamination;

import android.graphics.Canvas;

import java.util.ArrayList;

public class GameManager extends Thread{
    /**  Объект класса  */
    private GameView view;

    /**  частота смены кадров  */
    int FPS = 60;

    /** Список всех подклассов */
    Board board;
    ArrayList<Cell> cells;
    Boss boss;

    /**  Переменная для задания состояния потока отрисовки  */
    private boolean running = false;

    /**  Конструктор класса  */
    public GameManager(GameView view)
    {
        this.view = view;
    }

    /**  Задание состояния потока  */
    public void setRunning(boolean run)
    {
        running = run;
    }

    /**  Действия, выполняемые в потоке  */
    public void run() {
        long ticksPS = 1000 / FPS;
        while (running) {
            Canvas c = null;
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

            // Перемещаем элементы на поле
            board.Move();
            boss.Move();
        }
    }
}
