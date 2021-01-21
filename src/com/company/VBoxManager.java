package com.company;

import org.virtualbox_6_1.*;

import java.util.ArrayList;
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

    public void listAllVMs() {
        var machinesList = vbox.getMachines();
        String name;
        for (var m: machinesList) {
            name = m.getName();
            System.out.println(name);
        }
    }

    //not sure what this does but it seems to be important
    static boolean progressBar(VirtualBoxManager mgr, IProgress p, long waitMillis)
    {
        long end = System.currentTimeMillis() + waitMillis;
        while (!p.getCompleted())
        {
            // process system event queue
            mgr.waitForEvents(0);
            // wait for completion of the task, but at most 200 msecs
            p.waitForCompletion(200);
            if (System.currentTimeMillis() >= end)
                return false;
        }
        return true;
    }

    public void openLastVM() {
        //getting the VM
        var machinesList = vbox.getMachines();
        int lastMachineIndex = machinesList.size() - 1;
        var machine = machinesList.get(lastMachineIndex);
        String machineName = machine.getName();
        System.out.println("\nAttempting to start VM '" + machineName + "'");

        //opening the VM
        ISession session = boxManager.getSessionObject();
        ArrayList<String> env = new ArrayList<String>();
        IProgress p = machine.launchVMProcess(session, "gui", env);
        progressBar(boxManager, p, 10000);
        session.unlockMachine();
        // process system event queue
        boxManager.waitForEvents(0);
    }
    public void createVM() {
        //Creating the machine
        var machine = vbox.createMachine(null, "New Machine", null, "", "forceOverwrite=1");
        //=========Configuring the machine
        //general
        machine.setName("My Ubuntu Machine");
        machine.setDescription("This is some description");
        machine.setOSTypeId("ubuntu");
        machine.setMemorySize(2048l);
        //graphics
        var machineGraphics = machine.getGraphicsAdapter();
        machineGraphics.setVRAMSize(128l);
        machineGraphics.setGraphicsControllerType(GraphicsControllerType.VMSVGA);
        //bios
        var machineBios = machine.getBIOSSettings();
        machineBios.setACPIEnabled(true);
        //Saving the settings
        machine.saveSettings();

        vbox.registerMachine(machine);
    }

    public void removeVM() {
    }

    public void cleanup(){
        boxManager.cleanup();
    }
}
