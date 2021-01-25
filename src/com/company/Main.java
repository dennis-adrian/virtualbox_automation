package com.company;

import org.virtualbox_6_1.IMachine;
import org.virtualbox_6_1.IMedium;
import org.virtualbox_6_1.IMediumFormat;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        /* System.out.println("got event: " + ev);
        VBoxEventType type = ev.getType();
        System.out.println("type = " + type);  */

        VBoxManager vBoxMgr = new VBoxManager();
        String result = vBoxMgr.getVBoxVersion();
        try {

            //vBoxMgr.createVM("ubuntu linux");
           //vBoxMgr.openLastVM();
            //vBoxMgr.listAllVMs();
            //vBoxMgr.getVM("Fedora 3");
            //vBoxMgr.installationProcess();
            //vBoxMgr.powerOffMachine("My Ubuntu Machine");
            //vBoxMgr.removeVM("my new machine");
            //vBoxMgr.launchVM("Fedora 33");
            vBoxMgr.createVM("Ubuntu 20.04.1 Server");

            System.out.println("done, press Enter...");
            int ch = System.in.read();

        } catch (IOException e) {
            e.printStackTrace();
        }


        vBoxMgr.cleanup();
    }
}
