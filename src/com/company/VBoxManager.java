package com.company;

import org.virtualbox_6_1.*;

import java.util.List;

public class VBoxManager {

    private final VirtualBoxManager boxManager;
    private final IVirtualBox vbox;

    public VBoxManager() {
        boxManager = VirtualBoxManager.createInstance(null);
        vbox = boxManager.getVBox();
    }

    public String getVBoxVersion() {
        return vbox.getVersion();
    }

    public void createVM() {
        //Creating the machine
        var machine = vbox.createMachine(null, "MyMachine", null, "ubuntu", "forceOverwrite=1");
        //Configuring the machine
        machine.setDescription("This is some description");
        //Saving the settings
        machine.saveSettings();

        vbox.registerMachine(machine);
    }

}
