package com.company;

import com.sun.security.jgss.GSSUtil;
import org.virtualbox_6_1.*;

import java.util.ArrayList;
import java.util.List;

public class VBoxManager {

    private final VirtualBoxManager boxManager;
    private final IVirtualBox vbox;
    private IProgress progress;

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
        for (var m : machinesList) {
            name = m.getName();
            System.out.println(name);
        }
    }
    public IMachine getVM(String machineName){
        IMachine machine = null;
        try {
            var machinesList = vbox.getMachines();
            for (var m : machinesList) {
                if (m.getName().equals(machineName)) machine = m;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return machine;
    }

    //not sure what this does but it seems to be important
    static boolean progressBar(VirtualBoxManager mgr, IProgress p, long waitMillis) {
        long end = System.currentTimeMillis() + waitMillis;
        while (!p.getCompleted()) {
            // process system event queue
            mgr.waitForEvents(0);
            // wait for completion of the task, but at most 200 msecs
            p.waitForCompletion(200);
            if (System.currentTimeMillis() >= end)
                return false;
        }
        return true;
    }

    private boolean machineExists(String machineName) {
        ///VBOX_E_OBJECT_NOT_FOUND
        //kind of "exists"
        if (machineName == null) {
            return false;
        }
        //since the method findMachine returns org.virtualbox_5_2.VBoxException
        //if the machine doesn't exists we will need to find it by
        //ourselves iterating over all the machines
        List<IMachine> machines = vbox.getMachines();
        for (IMachine machine : machines) {
            if (machine.getName().equals(machineName)) {
                return true;
            }
        }
        return false;
    }

    public void launchVM(String machineName){
        var machine = getVM(machineName);
        if (machine != null) {
            ISession session = boxManager.getSessionObject();
            List list = new ArrayList();
            IProgress prog = machine.launchVMProcess(session, "gui", list);
            prog.waitForCompletion(10000);
            if (prog.getResultCode() != 0)
                System.out.println("Cannot launch VM " + machineName);
        }
        else System.out.println("The machine " + machineName + " wasn't found");


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

    public void configureNewMachine() {

    }
    public void createVM(String machineName) {
        //variables
        //this is the machine that will be created
        IMachine machine;
        IGraphicsAdapter machineGraphics;
        IBIOSSettings machineBios;
        //this session and mutable are created so it is possible to make changes to the just registered machine
        ISession session;
        IMachine mutable;
        //the medium that will be added to the machine
        IMedium hardDisk;
        IMedium dvdDisk;
        //this represents the characteristics of the hard disk (if it's fixed/dynamic for example)
        List hardDiskVariants;
        //the progress on the process of creating the hard disk
        IProgress hardDiskProgress;

        //Creating the machine
        machine = vbox.createMachine(null, machineName, null, "", "forceOverwrite=1");
        try {
            //=========Configuring the machine
            //general
            machine.setName(machineName);
            machine.setDescription("This is some description");
            machine.setOSTypeId("ubuntu");
            machine.setMemorySize(2048l);
            //graphics
            machineGraphics = machine.getGraphicsAdapter();
            machineGraphics.setVRAMSize(128l);
            machineGraphics.setGraphicsControllerType(GraphicsControllerType.VMSVGA);
            //bios
            machineBios = machine.getBIOSSettings();
            machineBios.setACPIEnabled(true);
            //USB controller
            machine.addUSBController("USB 1", USBControllerType.OHCI);
            //network

            //register the machine
            vbox.registerMachine(machine);

            //this session and mutable are created so it is possible to make changes to the just registered machine
            session = boxManager.getSessionObject();
            machine.lockMachine(session, LockType.Write);
            mutable = session.getMachine();

            //creating the hard disk and other medium devices
            hardDisk = vbox.createMedium("vdi", "/Users/dennisguzman/VirtualBox VMs/" + machineName + "/" + machineName + ".vdi", AccessMode.ReadWrite, DeviceType.HardDisk);
            //dvdDisk = vbox.createMedium("ISO", "/Users/dennisguzman/Downloads/ubuntu-20.04.1-live-server-amd64.iso", AccessMode.ReadOnly, DeviceType.DVD);
            //hardisk
            hardDiskVariants = new ArrayList();
            hardDiskVariants.add(MediumVariant.Standard);
            hardDiskProgress = hardDisk.createBaseStorage(15l * 1024 * 1024 * 1024, hardDiskVariants);
            hardDiskProgress.waitForCompletion(-1);
            //dvd
            //dvdDisk.createDiffStorage(dvdDisk, hardDiskVariants);

            //attaching the medium devices to the machine
            var sataStorageController = mutable.addStorageController("SATA", StorageBus.SATA);
            var ideStorageController = mutable.addStorageController("IDE", StorageBus.IDE);
            mutable.attachDevice(sataStorageController.getName(), 0, 0, DeviceType.HardDisk, hardDisk);
            mutable.attachDeviceWithoutMedium(ideStorageController.getName(), 1, 1, DeviceType.DVD);
            mutable.saveSettings();
            session.unlockMachine();
            mountDisk();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Waits until a tasks finished
     * <p>
     * Progress object to track the operation completion. Expected result codes:
     * E_UNEXPECTED	Virtual machine not registered.
     * E_INVALIDARG	Invalid session type type.
     * VBOX_E_OBJECT_NOT_FOUND	No machine matching machineId found.
     * VBOX_E_INVALID_OBJECT_STATE	Session already open or being opened.
     * VBOX_E_IPRT_ERROR	Launching process for machine failed.
     * VBOX_E_VM_ERROR	Failed to assign machine to session.
     *
     * @param progress current task monitor
     */
    private void wait(IProgress progress) {
        //make this available for the caller
        this.progress = progress;
        progress.waitForCompletion(-1);
        if (progress.getResultCode() != 0) {
            System.err.println("Operation failed: " + progress.getErrorInfo().getText());
        }
    }

    /**
     * Wait untill the current session is unlocked
     *
     * @param session session
     * @param machine machine
     */
    private void waitToUnlock(ISession session, IMachine machine) {
        session.unlockMachine();
        SessionState sessionState = machine.getSessionState();
        while (!SessionState.Unlocked.equals(sessionState)) {
            sessionState = machine.getSessionState();
            try {
                System.err.println("Waiting for session unlock...[" + sessionState.name() + "][" + machine.getName() + "]");
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for session to be unlocked");
            }
        }
    }

    public void removeVM(String machineName) {
        if (!machineExists(machineName)) {
            return;
        }
        IMachine machine = vbox.findMachine(machineName);
        MachineState state = machine.getState();
        ISession session = boxManager.getSessionObject();
        machine.lockMachine(session, LockType.Shared);
        try {
            if (state.value() >= MachineState.FirstOnline.value() && state.value() <= MachineState.LastOnline.value()) {
                IProgress progress = session.getConsole().powerDown();
                wait(progress);
            }
        } finally {
            waitToUnlock(session, machine);
            System.err.println("Deleting machine " + machineName);
            List<IMedium> media = machine.unregister(CleanupMode.DetachAllReturnHardDisksOnly);
            machine.deleteConfig(media);
        }
    }

    public void powerOffMachine(String machineName) {
        // Get the virtual machine you need to connect to.
        IVirtualBox vbox = boxManager.getVBox();
        IMachine machine = vbox.findMachine(machineName);

        ISession session = null;
        IProgress prog;

        // Determine the status of the virtual machine.
        MachineState state = machine.getState();
        System.out.println(state);

        if (MachineState.Running == state) {
            session = boxManager.getSessionObject();
            machine.lockMachine(session, LockType.Shared);
            prog = session.getConsole().powerDown();
            prog.waitForCompletion(10000);
            session.unlockMachine();
            System.out.println(machineName + " was powered off!");
        } else {
            System.out.println(machineName + " is PoweredOff!");
        }
    }

    public void mountDisk(){
        //getting the VM
        var machinesList = vbox.getMachines();
        int lastMachineIndex = machinesList.size() - 1;
        var machine = machinesList.get(lastMachineIndex);
        String machineName = machine.getName();
        System.out.println("\nAttempting to get VM '" + machineName + "'");

        //mounting vm
        var diskImage = vbox.openMedium("/Users/dennisguzman/Downloads/ubuntu-20.04.1-live-server-amd64.iso", DeviceType.DVD, AccessMode.ReadOnly, false);
        var storageController = machine.getStorageControllerByName("IDE");
        machine.mountMedium(storageController.getName(), 1, 1, diskImage, true);
    }

    public void vmCreationProcess(String name) {
        createVM(name);
        wait(progress);
        launchVM(name);
    }

    public void cleanup() {
        boxManager.cleanup();
    }
}
