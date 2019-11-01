package ethanmcmike.ndb.views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class RXIndicatorView extends AppCompatImageView implements Runnable{

    private Thread thread;
    private long lastUpdate;
    private int delay;
    private boolean running;

    public RXIndicatorView(Context context) {
        super(context);
        init();
    }

    public RXIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RXIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        delay = 1000;
        thread = new Thread(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(System.currentTimeMillis() <= lastUpdate + delay)
            super.onDraw(canvas);
    }

    public void update(){

        lastUpdate = System.currentTimeMillis();

        invalidate();

        if(!running) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public void run() {

        running = true;

        while(System.currentTimeMillis() <= lastUpdate + delay){

        }

        postInvalidate();

        running = false;
    }
}
