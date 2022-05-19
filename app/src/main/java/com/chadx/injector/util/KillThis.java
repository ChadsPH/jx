package com.chadx.injector.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.Runtime.getRuntime;
import java.util.Map;
import java.util.List;
import java.net.NetworkInterface;
import java.util.Collections;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.util.HashMap;
import android.annotation.TargetApi;
import android.os.Build;
import java.util.Collection;
import java.util.ArrayList;
import android.net.ConnectivityManager;
import java.lang.reflect.Method;
import android.net.LinkProperties;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.regex.Pattern;
import com.services.tunnel.vpn.VpnUtils;

import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;



public class KillThis {
	
	

    public static int findProcessId(String command) throws IOException {

        String[] cmds = {"ps -ef","ps -A","toolbox ps"};

        for (int i = 0; i < cmds.length;i++) {
            Process procPs = getRuntime().exec(cmds[i]);

            BufferedReader reader = new BufferedReader(new InputStreamReader(procPs.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("PID") && line.contains(command)) {
                    String[] lineParts = line.split("\\s+");
                    try {
                        return Integer.parseInt(lineParts[1]); //for most devices it is the second
                    } catch (NumberFormatException e) {
                        return Integer.parseInt(lineParts[0]); //but for samsungs it is the first
                    } finally {
                        try {
                            procPs.destroy();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }

        return -1;
    }

    public static void killProcess(File fileProcBin) throws Exception {
        killProcess(fileProcBin, "-9"); // this is -KILL
    }

    public static int killProcess(File fileProcBin, String signal) throws Exception {

        int procId = -1;
        int killAttempts = 0;

        while ((procId = findProcessId(fileProcBin.getName())) != -1) {
            killAttempts++;
            String pidString = String.valueOf(procId);

            String[] cmds = {"","busybox ","toolbox "};

            for (int i = 0; i < cmds.length;i++) {
                try {
                    getRuntime().exec(cmds[i] + "killall " + signal + " " + fileProcBin.getName
									  ());
                } catch (IOException ioe) {
                }
                try {
                    getRuntime().exec(cmds[i] + "killall " + signal + " " + fileProcBin.getCanonicalPath());
                } catch (IOException ioe) {
                }
            }

            killProcess(pidString, signal);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignored
            }

            if (killAttempts > 4)
                throw new Exception("Cannot kill: " + fileProcBin.getAbsolutePath());
        }

        return procId;
    }

    public static void killProcess(String pidString, String signal) throws Exception {

        String[] cmds = {"","toolbox ","busybox "};

        for (int i = 0; i < cmds.length;i++) {
            try {
                getRuntime().exec(cmds[i] + "kill " + signal + " " + pidString);
            } catch (IOException ioe) {
				//   SkStatus.logDebug(String.format("error killing process: %s, %s", pidString, ioe.getMessage()));
            }
        }
    }

	

}
