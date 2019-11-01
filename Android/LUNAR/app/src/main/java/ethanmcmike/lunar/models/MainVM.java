package ethanmcmike.ndb.models;

import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;

import ethanmcmike.ndb.utils.BTController;

public class MainVM extends ViewModel {

    public Aircraft rocket, payload;
    public BTController bt;

    public MainVM(){

    }
}
