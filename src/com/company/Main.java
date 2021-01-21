package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        /* System.out.println("got event: " + ev);
        VBoxEventType type = ev.getType();
        System.out.println("type = " + type);  */

        VBoxManager vBoxMgr = new VBoxManager();
        String result = vBoxMgr.getVBoxVersion();
        try {

        //vBoxMgr.createVM();
        vBoxMgr.openLastVM();

        System.out.println(result);

        System.out.println("done, press Enter...");
            int ch = System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        vBoxMgr.cleanup();
    }
}
