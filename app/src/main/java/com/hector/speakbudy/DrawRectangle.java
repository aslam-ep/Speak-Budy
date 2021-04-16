package com.hector.speakbudy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.objects.DetectedObject;

import java.util.List;

public class DrawRectangle extends View {

    Paint boundaryPaint, textPaint;
    String text;
    Rect rect;

    public DrawRectangle(Context context, Rect rect, String text) {
        super(context);

        boundaryPaint = new Paint();
        boundaryPaint.setColor(getResources().getColor(R.color.textColor));
        boundaryPaint.setStrokeWidth(10f);
        boundaryPaint.setStyle(Paint.Style.STROKE);


        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.amaranth));
        textPaint.setStrokeWidth(10f);
        textPaint.setStyle(Paint.Style.FILL);

        this.text = text;
        this.rect = rect;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(text, rect.centerX(), rect.centerY(), textPaint);
//        canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, boundaryPaint);
        canvas.drawRect(rect, boundaryPaint);
    }
}
