package ethanmcmike.ndb.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import ethanmcmike.ndb.R;

public class ArrowView extends View {

    private float angle;

    public ArrowView(Context context) {
        super(context);
        init();
    }

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(){
        angle = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint paint = new Paint();

        canvas.translate(canvas.getWidth()/2f, canvas.getHeight()/2f);
        canvas.rotate(angle);

        Bitmap arrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
        Rect src = new Rect(0, 0, arrow.getWidth(), arrow.getHeight());
        Rect dst = new Rect(-canvas.getWidth()/3, -canvas.getHeight()/3, canvas.getWidth()/3, canvas.getHeight()/3);

        canvas.drawBitmap(arrow, src, dst, paint);
    }

    public void setAngle(float angle){
        this.angle = angle;
        invalidate();
    }

    public float getAngle(){
        return angle;
    }
}
