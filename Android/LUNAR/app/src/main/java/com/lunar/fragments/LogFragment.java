package com.lunar.fragments;

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
import com.lunar.adapters.LogAdapter;
import com.lunar.models.LogItem;

public class LogFragment extends Fragment {

    private static LogFragment instance;

    private ListView logList;
    private LogAdapter adapter;
    private ArrayList<LogItem> items;

    public static LogFragment newInstance(){

        instance = new LogFragment();
        instance.items = new ArrayList();

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        adapter = instance.adapter;
//        items = instance.items;
        return inflater.inflate(R.layout.log_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        logList = view.findViewById(R.id.log_list);
        adapter = new LogAdapter(getContext(), items);
        logList.setAdapter(adapter);
    }

    public void receive(){
        items.add(new LogItem(LogItem.EventType.RECEIVED));
        update();
    }

    public void send(){
        items.add(new LogItem(LogItem.EventType.SENT));
        update();
    }

    public void connect(){
        items.add(new LogItem(LogItem.EventType.CONNECTED));
        update();
    }

    public void disconnect(){
        items.add(new LogItem(LogItem.EventType.DISCONNECTED));
        update();
    }

    private void update(){
        if(adapter != null)
            adapter.notifyDataSetChanged();
    }
}
