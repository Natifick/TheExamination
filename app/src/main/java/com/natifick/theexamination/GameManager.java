package com.natifick.theexamination;

import android.graphics.Canvas;

import java.util.LinkedList;

public class GameManager extends Thread{
    /**  Объект класса  */
    private GameView view;

    /**  частота смены кадров  */
    int FPS = 60;

    /** Список всех подклассов */
    Board board;
    LinkedList<Cell> cells;
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
        Canvas c = null;
        // Блокируем
        try{
            c = view.getHolder().lockCanvas();
            synchronized (view.getHolder()){
                view.intialDraw(c);
            }
        } finally{
            if (c != null){
                view.getHolder().unlockCanvasAndPost(c);
            }
        }
        view.intialDraw(c);
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
        }
    }
}
