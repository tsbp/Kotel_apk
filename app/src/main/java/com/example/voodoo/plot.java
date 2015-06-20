package com.example.voodoo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by voodoo on 18.06.2015.
 */
public class plot extends View {

    Paint p;
    public static int[] aBuf;

    public plot(Context context, AttributeSet attrs) {
        super(context, attrs);

        p = new Paint();
        aBuf = new int[]{102,334,223,123,256,278,267,345,456,234,345,234,
                222,222,222,222,222,222,222,222,222,222,222,222};

    }
    //==============================================================================================


    //#define Y_COORD          (100)
    int LEFT_OFFSET = 30;
    int RIGHT_OFFSET  =  (3);
    int X_OFFSET     =    (LEFT_OFFSET + RIGHT_OFFSET);
    int TOP_OFFSET   =    (10);
    int BOTTOM_OFFSET  =  (5);
    int Y_OFFSET       =  (TOP_OFFSET  + BOTTOM_OFFSET);

    int AREA_WIDTH;
    int AREA_HEIGH;

    int aY = 0;

    static float cena = 0;
    static int tmax, tmin;
    //==============================================================================================
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawARGB(150, 102, 204, 255); //set canvas background


        AREA_WIDTH = canvas.getWidth();
        AREA_HEIGH = canvas.getHeight();

        int PLOT_WIDTH    =   (AREA_WIDTH - X_OFFSET);
        int PLOT_HEIGH    =   (AREA_HEIGH - Y_OFFSET);
        int POINTS_CNT    =   (24);
        int HGRID_SPACING =   (PLOT_WIDTH / (POINTS_CNT-1));   // dots between dividings
        int VGRID_CNT      =  (10);
        int VGRID_SPACING  =  (PLOT_HEIGH / VGRID_CNT);

        p.setStrokeWidth(1);
        p.setColor(Color.GRAY);
        canvas.drawLine(LEFT_OFFSET,  aY + TOP_OFFSET, LEFT_OFFSET +PLOT_WIDTH, aY + TOP_OFFSET, p);
        for(int i = 0; i < VGRID_CNT+1;  i++)
            canvas.drawLine(LEFT_OFFSET,  aY + PLOT_HEIGH - i*VGRID_SPACING + TOP_OFFSET, LEFT_OFFSET + PLOT_WIDTH, aY + PLOT_HEIGH - i*VGRID_SPACING + TOP_OFFSET,  p);
        for(int i = 0; i < POINTS_CNT; i++)
            canvas.drawLine(i*HGRID_SPACING + LEFT_OFFSET, aY + TOP_OFFSET, i*HGRID_SPACING + LEFT_OFFSET, aY + TOP_OFFSET + PLOT_HEIGH,  p);

        tmax = aBuf[0]; tmin = aBuf[0];
        for(int i = 1; i < POINTS_CNT; i++) if (tmax < aBuf[i]) tmax = aBuf[i]; // tmax
        for(int i = 1; i < POINTS_CNT; i++) if (tmin > aBuf[i]) tmin = aBuf[i]; // tmin

        tmax /= 10; tmax *= 10; tmax += 10;
        tmin /= 10; tmin *= 10; tmin -= 10;

        float delta = tmax - tmin;

        cena = PLOT_HEIGH / delta;
        int tmp  = tmax;

        p.setTextSize(10);
        p.setColor(Color.BLUE);

        canvas.drawText(String.valueOf(tmax), 10, aY + TOP_OFFSET, p);
        canvas.drawText(String.valueOf(tmin), 10, aY + AREA_HEIGH-9, p);

        p.setStrokeWidth(2);
        p.setColor(Color.RED);
        for(int i = 0; i < POINTS_CNT-1; i++)
            canvas.drawLine (i*HGRID_SPACING + LEFT_OFFSET,       aY + PLOT_HEIGH + TOP_OFFSET - (int)((aBuf[i]   - tmin)*cena),
                (i+1) *HGRID_SPACING + LEFT_OFFSET, aY + PLOT_HEIGH + TOP_OFFSET - (int)((aBuf[i+1] - tmin)*cena), p);
    }
}
