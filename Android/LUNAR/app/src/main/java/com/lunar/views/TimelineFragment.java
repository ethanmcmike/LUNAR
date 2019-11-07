package com.lunar.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import com.lunar.R;
import com.lunar.adapters.TimelineAdapter;
import com.lunar.models.Event;

public class TimelineFragment extends Fragment {

    private static TimelineFragment instance;

    private ListView listView;
    private ArrayList<Event> timeline;
    private TimelineAdapter adapter;

    public static TimelineFragment getInstance(){
        if(instance == null){
            instance = new TimelineFragment();
        }

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        timeline = instance.timeline;
        adapter = instance.adapter;
        return inflater.inflate(R.layout.timeline_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Populate timeline
        String[] events = getResources().getStringArray(R.array.timeline_events);

        timeline = new ArrayList();

        for(int i=0; i<events.length; i++){
            timeline.add(new Event(events[i], i));
        }

        //Init list
        listView = view.findViewById(R.id.timeline_list);
        adapter = new TimelineAdapter(getContext(), timeline);
        listView.setAdapter(adapter);
    }

    public void update(int eventIndex){

        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }
}
