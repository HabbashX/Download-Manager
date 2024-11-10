package com.habbashx;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
public class DownloadManagerMain extends Launcher{

    public static void main(String[] args) {

        try {

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("file.txt"));

            for (int i = 0 ; i<=1000 ; i++) {
                bufferedWriter.write("Hello World");
                bufferedWriter.flush();
            }
            bufferedWriter.close();

        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
