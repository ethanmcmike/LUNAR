package ethanmcmike.ndb.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ListView;

import java.util.ArrayList;

import ethanmcmike.ndb.R;
import ethanmcmike.ndb.models.Event;

public class TimelineView extends ListView {

    private Context context;

    private ArrayList<Event> timeline;

    public TimelineView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TimelineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimelineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        this.context = context;

        //Populate timeline
        String[] events = getResources().getStringArray(R.array.timeline_events);

        timeline = new ArrayList();

        for(int i=0; i<events.length; i++){
            timeline.add(new Event(events[i], i));
        }
    }

    public ArrayList<Event> getTimeline(){
        return timeline;
    }
}
