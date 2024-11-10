package com.habbashx.notification;

import com.habbashx.exception.UnSupportedSystemTray;
import com.habbashx.logger.Logger;

import java.awt.SystemTray;
import java.awt.Image;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.TrayIcon;

public class DownloadManagerNotification {

    public static void pushNotification(String message , Logger logger , TrayIcon.MessageType messageType) {

        if (SystemTray.isSupported()) {
            SystemTray systemTray = SystemTray.getSystemTray();

            Image image = Toolkit.getDefaultToolkit().createImage("./icon.png");
            TrayIcon trayIcon = new TrayIcon(image,"DownloadManager");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip(message);

            try {
                systemTray.add(trayIcon);
            }catch (AWTException e) {
                logger.error(e.getMessage());
            }
            trayIcon.displayMessage("DownloadManager",message,messageType);
            logDependOnLevel(message,messageType,logger);
        } else {
            throw new UnSupportedSystemTray("your system not supported");
        }
    }

  public static void logDependOnLevel(String message , TrayIcon.MessageType messageType , Logger logger) {
        if (messageType == TrayIcon.MessageType.INFO) {
            logger.info(message+"\n");
        } else if (messageType == TrayIcon.MessageType.ERROR) {
            logger.error(message+"\n");
        }
  }
}
