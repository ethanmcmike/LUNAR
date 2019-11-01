package ethanmcmike.ndb.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LevelView extends View {

    public static final int BUBBLE_SIZE = 40;

    private float x, y;

    public LevelView(Context context) {
        super(context);
        init();
    }

    public LevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        x = 0;
        y = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint paint = new Paint();


        int w = canvas.getWidth();
        int h = canvas.getHeight();

        canvas.translate(w/2f, h/2f);
        canvas.scale(0.8f, 0.8f);

        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(10);
        canvas.drawLine(-w/2f, 0, w/2f, 0, paint);
        canvas.drawLine(0, -h/2f, 0, h/2f, paint);

        paint.setAlpha(100);
        canvas.drawCircle(x*w/2f, -y*h/2f, BUBBLE_SIZE, paint);
    }

    public void set(float x, float y){

        if(x > 1)
            this.x = 1;
        else if(x < -1)
            this.x = -1;
        else
            this.x = x;

        if(y > 1)
            this.y = 1;
        else if(y < -1)
            this.y = -1;
        else
            this.y = y;

        invalidate();
    }
}
