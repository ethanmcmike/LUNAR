package com.lunar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.lunar.R;
import com.lunar.models.LogItem;

public class LogAdapter extends ArrayAdapter<LogItem> {

    private ArrayList<LogItem> items;

    public LogAdapter(Context context, ArrayList<LogItem> items) {
        super(context, 0);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public LogItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LogItem item = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.log_item, parent, false);
        }

        TextView time = convertView.findViewById(R.id.log_item_time);
        TextView text = convertView.findViewById(R.id.log_item_text);

        time.setText(item.time);

        String eventText = "";

        switch(item.event){

            case CONNECTED:
                eventText = getContext().getResources().getString(R.string.log_connect);
                break;

            case DISCONNECTED:
                eventText = getContext().getResources().getString(R.string.log_disconnect);
                break;

            case RECEIVED:
                eventText = getContext().getResources().getString(R.string.log_receive);
                break;

            case SENT:
                eventText = getContext().getResources().getString(R.string.log_send);
                break;
        }

        text.setText(eventText);

        return convertView;
    }
}
