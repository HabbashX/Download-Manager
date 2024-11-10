package com.habbashx.manager.destinationOrganizer;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DestinationOrganizer {



    private static final Map<String,String> FILE_CATEGORIES = new HashMap<>(){{
        put("exe","\\Programs");
        put("jar","\\Program");
        put("zip","\\compressed");
        put("rar","\\compressed");
        put("tar","\\compressed");
        put("pdf","\\Documents");
        put("docx","\\Documents");
        put("xlsx","\\Documents");
        put("txt","\\Documents");
        put("ppt","\\Documents");
        put("pptx","\\Documents");
        put("doc","\\Documents");
        put("xls","\\Documents");
        put("java","\\Documents");
        put("cpp","\\Documents");
        put("c","\\Documents");
        put("html","\\Documents");
        put("css","\\Documents");
        put("rust","\\Documents");
        put("cs","\\Documents");
        put("json","\\Documents");
        put("xml","\\Documents");
        put("js","\\Documents");
        put("bash","\\Documents");
        put("bat","\\Documents");
        put("cmd","\\Documents");
        put("jpg","\\Images");
        put("jpeg","\\Images");
        put("png","\\Images");
        put("gif","\\Images");
        put("bmp","\\Images");
        put("tif","\\Images");
        put("mp4","\\Videos");
        put("avi","\\Videos");
        put("mov","\\Videos");
        put("mp3","\\musics");
    }};

    private static String organizeFile(String file){

        String fileExtension = extractFileExtension(file);

        String fileCategory = FILE_CATEGORIES.getOrDefault(fileExtension, "Others");
        String ROOT = System.getProperty("user.home")+"\\Downloads";
        return ROOT+fileCategory;
    }

    public static Path organizeFileDestination(String linkURL){
        String extractedFileName = extractFileName(linkURL);

        String organizedDestinationPath = organizeFile(extractedFileName);
        Path downloadsFolder = Path.of(organizedDestinationPath);

        return downloadsFolder.resolve(extractedFileName);
    }

    private static String extractFileName(String urlString) {
        return urlString.substring(urlString.lastIndexOf("/") + 1);
    }

    private static String extractFileExtension(String name) {

        int dotIndex = name.lastIndexOf(".");
        return (dotIndex == -1) ? "" : name.substring(dotIndex + 1).toLowerCase();
    }

}

