package com.habbashx.notification;

import com.habbashx.exception.UnSupportedSystemTray;
import com.habbashx.logger.Logger;

import java.awt.SystemTray;
import java.awt.Image;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.TrayIcon;

/**
 * The DownloadManagerNotification class is responsible for managing and displaying
 * system tray notifications related to the download manager. It provides functionality
 * to push notifications to the user and log messages based on the type of notification.
 */
public class DownloadManagerNotification {

    /**
     * Displays a notification using the system tray and logs the message based on its type.
     *
     * @param message the notification message to display. Must not be null.
     * @param logger  the logger instance for logging the notification message.
     *                Logs at different levels based on the notification type.
     * @param messageType the type of the message to be displayed (e.g., INFO, WARNING, ERROR).
     *                    Utilizes {@link TrayIcon.MessageType}.
     * @throws UnSupportedSystemTray if the system tray is not supported on the current platform.
     */
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

  /**
   * Logs a given message at a specific level based on the provided {@link TrayIcon.MessageType}.
   *
   * @param message the message to be logged
   * @param messageType the type of message, determines the log level (INFO or ERROR)
   * @param logger the logger instance used for logging the message
   */
  public static void logDependOnLevel(String message , TrayIcon.MessageType messageType , Logger logger) {
        if (messageType == TrayIcon.MessageType.INFO) {
            logger.info(message+"\n");
        } else if (messageType == TrayIcon.MessageType.ERROR) {
            logger.error(message+"\n");
        }
  }
}
