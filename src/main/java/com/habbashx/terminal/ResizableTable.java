package com.habbashx.terminal;

import java.util.List;

public class ResizableTable {

    public static void printTable(List<String[]> rows){

        int[] columnWidth = calculateColumnWidth(rows);

        for (int i = 0 ; i < rows.size() ; i++){
            if (i == 0) {
                printLine(columnWidth);
            }
            printRow(rows.get(i),columnWidth);
            printLine(columnWidth);
        }
    }

    private static void printRow(String[] row , int[] columnWidth) {
        System.out.print("|");

        for (int i = 0 ;i <row.length ; i++){
            System.out.print(" "+row[i]);

            int padding = columnWidth[i] - row[i].length();

            for (int j = 0 ; j< padding ; j++){
                System.out.print(" ");
            }
            System.out.print(" |");
        }
        System.out.println();
    }

    private static void printLine(int[] columnWidth) {
        System.out.print("+");

        for (int width : columnWidth){
            for (int i = 0 ; i < width +2 ;i++){
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }

    private static int[] calculateColumnWidth(List<String[]> rows) {

        int columns = rows.get(0).length;
        int[] columnsWidths = new int[columns];

        for (String[] row : rows) {
            for (int i = 0 ; i<row.length ; i++){

                columnsWidths[i] = Math.max(columnsWidths[i],row[i].length());
            }
        }
        return columnsWidths;
    }
}
