package com.lunar.models;

import android.arch.lifecycle.ViewModel;

import com.lunar.utils.BTController;

public class MainVM extends ViewModel {

    public Aircraft rocket, payload;
    public BTController bt;

    public MainVM(){

    }
}
