package ethanmcmike.ndb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ethanmcmike.ndb.R;
import ethanmcmike.ndb.models.Event;

public class TimelineAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> timeline;

    public TimelineAdapter(Context context, ArrayList<Event> timeline) {
        super(context, 0);
        this.timeline = timeline;
    }

    @Override
    public int getCount() {
        return timeline.size();
    }

    @Override
    public Event getItem(int position) {
        return timeline.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Event event = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_event, parent, false);
        }

        CheckBox circle = convertView.findViewById(R.id.circle);
        TextView name = convertView.findViewById(R.id.name);

        circle.setChecked(event.complete);
        name.setText(event.name);

        return convertView;
    }
}
