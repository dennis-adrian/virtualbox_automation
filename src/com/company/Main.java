package com.company;

import org.virtualbox_6_1.VBoxEventType;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        /* System.out.println("got event: " + ev);
        VBoxEventType type = ev.getType();
        System.out.println("type = " + type);  */

        VBoxManager vBoxMgr = new VBoxManager();
        String result = vBoxMgr.getVBoxVersion();

        vBoxMgr.createVM();

        System.out.println(result);
    }
}
