package com.company;

import org.virtualbox_6_1.IMachine;
import org.virtualbox_6_1.IMedium;
import org.virtualbox_6_1.IMediumFormat;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        VBoxManager vBoxMgr = new VBoxManager();
        Scanner input = new Scanner(System.in);
        try {
            System.out.println("Welcome!");
            System.out.println("What is the name of the machine to be launched?");
            String machineName = input.nextLine();

            vBoxMgr.launchVM(machineName);
            System.out.println("done, press Enter...");
            int ch = System.in.read();

        } catch (IOException e) {
            e.printStackTrace();
        }
        vBoxMgr.cleanup();
    }
}
