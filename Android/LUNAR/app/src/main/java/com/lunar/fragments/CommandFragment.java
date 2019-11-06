package com.lunar.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.lunar.interfaces.CommandHandler;
import com.lunar.R;

public class CommandFragment extends Fragment {

    //Sets command order
    private static int[] commands = {R.string.command_drogue, R.string.command_body, R.string.command_main, R.string.command_payload};

    private Switch arm;
    private TextView selected;
    private Button send;
    private RadioGroup commandGroup;

    private CommandHandler commandHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.command_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //Find views
        arm = view.findViewById(R.id.command_arm);
        selected = view.findViewById(R.id.command_selected);
        send = view.findViewById(R.id.command_send);
        commandGroup = view.findViewById(R.id.command_radio_group);

        //Set listeners
        arm.setOnCheckedChangeListener(armListener);
        commandGroup.setOnCheckedChangeListener(radioListener);
        send.setOnClickListener(sendListener);

        //Populate command group
        for(int i=0; i<commands.length; i++){
            RadioButton button = new RadioButton(getContext());
            button.setText(getResources().getString(commands[i]));
            button.setEnabled(false);
            button.setChecked(false);
            button.setPadding(50, 50, 50, 50);
            button.setId(commands[i]);

            commandGroup.addView(button, i);
        }

        //Set initial states
        arm.setChecked(false);
        commandGroup.clearCheck();
        send.setEnabled(false);
        send.getBackground().clearColorFilter();
    }

    Switch.OnCheckedChangeListener armListener = new Switch.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean armed) {

            commandGroup.clearCheck();

            for(int i=0; i<commandGroup.getChildCount(); i++) {
                commandGroup.getChildAt(i).setEnabled(armed);
            }

            selected.setText("");
            send.setEnabled(false);
        }
    };

    RadioGroup.OnCheckedChangeListener radioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int id) {

            //Set selected title
            RadioButton button = getView().findViewById(id);
            if(button != null){
                String title = button.getText().toString();
                selected.setText(title);
            }

            //Enable send button
            send.setEnabled(true);
            send.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    };

    Button.OnClickListener sendListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {

            int id = commandGroup.getCheckedRadioButtonId();
            commandHandler.onCommandSent(id);

            //Reset view states
            send.getBackground().clearColorFilter();
            send.setEnabled(false);
            commandGroup.clearCheck();
            selected.setText("");
            arm.setChecked(false);
        }
    };

    public void setCommandHandler(CommandHandler commandHandler){
        this.commandHandler = commandHandler;
    }
}
