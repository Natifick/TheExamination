package com.natifick.theexamination;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView {

    /**  отступ от краёв экрана  */
    public static final int PADDING = 30;

    /** ширина поля */
    public int width;
    public int height;

    /** Цвета, которые будем часто использовать */
    int BackgroudColor;
    int BoardColor;

    /**  Рисователь  */
    Paint paint;

    /**  Поток игры  */
    GameManager gameThread;

    /** Наше поле рисования  */
    private SurfaceHolder holder;

    /** Список всех подклассов */
    Board board;

    /** Доска, которую двигает игрок */
    RectF boardRect;

    int cnt = 0;

    /** конструктор класса */
    public GameView(Context context){
        super(context);
        // Настраиваем цвета
        BackgroudColor = getResources().getColor(R.color.Light, null);
        BoardColor = getResources().getColor(R.color.BadassDark, null);

        // Берем "кисточку" и сам "холст"
        paint = new Paint();
        paint.setAntiAlias(true); // сглаживание линий
        holder = getHolder();

        // передаём поток и все подклассы
        gameThread = new GameManager(this);

        holder.addCallback(new SurfaceHolder.Callback()
        {
            /** Уничтожение области рисования */
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                boolean retry = true;
                gameThread.setRunning(false);
                while (retry) {
                    try {
                        gameThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }

            /** Создание области рисования */
            public void surfaceCreated(SurfaceHolder holder)
            {
                gameThread.setRunning(true);
                gameThread.start();
            }

            /** Изменение области рисования */
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
            }
        });
        //bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    }

    /** Узнаём размерность окошка для рисования */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;
        board = new Board(width, height);
        gameThread.board = board;
        boardRect = new RectF(0, 0, board.BoardWidth, board.BoardHeight);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /** Рисуем кружок на голубом фоне */
    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        // фон
        paint.setColor(BackgroudColor);
        canvas.drawPaint(paint);

        // рамка или же "рельсы"
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(PADDING, PADDING, width-PADDING, height-PADDING, paint);

        // сохраняем текущее состояние поворота и положения канваса
        canvas.save();

        // Перемещаем краешек доски туда, куда нужно
        paint.setColor(BoardColor);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.translate(PADDING + board.RightX, PADDING + board.RightY);
        canvas.rotate(board.angle);
        canvas.drawRect(boardRect, paint);
        paint.setStrokeWidth(1);
        // Возвращаемся к сохранённому состоянию
        canvas.restore();

    }
}
