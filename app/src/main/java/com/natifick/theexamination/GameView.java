package com.natifick.theexamination;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;

public class GameView extends SurfaceView {

    /**  отступ от краёв экрана  */
    public static int PADDING_X = 30, PADDING_Y = 200+30;

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
    ArrayList<Cell> cells;
    Boss boss;

    /** Доска, которую двигает игрок */
    RectF boardRect;

    /** иногда используется в других классах */
    public static Resources res;

    /**  используется для смещения босса в нужную координату */
    Matrix matrix = new Matrix();

    /** конструктор класса */
    public GameView(Context context){
        super(context);
        // Настраиваем цвета
        BackgroudColor = getResources().getColor(R.color.Light, null);
        BoardColor = getResources().getColor(R.color.colorAccent, null);
        cells = new ArrayList<>();

        res = context.getResources();

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

        setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_DOWN:
                        board.CountTarget((int)event.getX(event.getPointerCount()-1)-PADDING_X,
                                (int)event.getY(event.getPointerCount()-1)-PADDING_Y);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        board.Moving=dir.No;
                        break;
                }
                return true;
            }
        });
    }

    /** Узнаём размерность окошка для рисования */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;
        PADDING_Y = (int)((h-w)/2.0)+PADDING_X;

        board = new Board(width, height);
        boss = new Boss(width, height, board);
        gameThread.board = board;
        gameThread.boss = boss;
        gameThread.cells = cells;
        boardRect = new RectF(0, 0, board.BoardWidth, board.BoardHeight);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void intialDraw(Canvas canvas) {
        paint.setColor(BackgroudColor);
        canvas.drawPaint(paint);
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
        canvas.drawRect(PADDING_X+(int)(board.BoardHeight/2.0), PADDING_Y+(int)(board.BoardHeight/2.0),
                width-PADDING_X-(int)(board.BoardHeight/2.0), height-PADDING_Y-(int)(board.BoardHeight/2.0), paint);

        // сохраняем текущее состояние поворота и положения канваса
        canvas.save();

        // Перемещаем краешек доски туда, куда нужно
        // и рисуем сначла сам прямоугольник, а затем его обрамление
        canvas.translate(PADDING_X + board.RightX, PADDING_Y + board.RightY);
        canvas.rotate(board.angle);
        paint.setColor(BoardColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(boardRect, paint);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawRect(boardRect, paint);
        paint.setStrokeWidth(1);
        // Возвращаемся к сохранённому состоянию
        canvas.restore();

        // рисуем босса
        matrix.setRotate(boss.angle, boss.radius, boss.radius);
        matrix.postTranslate((int)(width/2.0)-boss.radius, (int)(height/2.0)-boss.radius);
        switch (boss.CurPic){
            case -1: canvas.drawBitmap(boss.ImgLeft, matrix, paint);
                break;
            case 0: canvas.drawBitmap(boss.ImgMiddle, matrix, paint);
                break;
            case 1: canvas.drawBitmap(boss.ImgRight, matrix, paint);
                break;
        }

        // Выводим все "снаряды"
        for (Cell cell: cells){
            paint.setColor(Type.getColor(cell.CellType));
            switch(cell.CellType){
                case Cell2:
                case Cell3:
                case Cell4:
                case Cell5:
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(cell.CellX+PADDING_X, cell.CellY+PADDING_Y, cell.CellR, paint);
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(cell.CellR*2);
                    canvas.drawText(Type.getName(cell.CellType), cell.CellX+PADDING_X-(int)(cell.CellR/2.0), cell.CellY+PADDING_Y+(int)(cell.CellR/2.0), paint);
                    break;
                case Failed:
                case Mew:
                case Resit:
                    break;
            }

        }


    }
}
