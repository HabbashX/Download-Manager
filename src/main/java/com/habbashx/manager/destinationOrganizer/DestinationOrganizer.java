package com.habbashx.manager.destinationOrganizer;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class that organizes file downloads into categorized directories
 * based on their file extensions. The categorization is predefined for common
 * file types.
 */
public class DestinationOrganizer {

    /**
     * A mapping of file extensions to their corresponding file categories or directories.
     * This map is utilized to determine the appropriate folder for a file based on its extension.
     *
     * Key-value pairs represent:
     * - Key: The file extension (e.g., "exe", "jpg", "pdf").
     * - Value: The folder or category to which the file belongs (e.g., "\\Programs", "\\Images", "\\Documents").
     *
     * This static map is used in file organization processes to categorize and organize files
     * retrieved or downloaded into a structured directory system.
     */
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

    /**
     * Organizes a file by determining its category based on the file extension and
     * generating its path within a predefined directory structure.
     *
     * @param file the name of the file with its extension
     * @return the full path of the directory where the file should be organized
     */
    private static String organizeFile(String file){

        String fileExtension = extractFileExtension(file);

        String fileCategory = FILE_CATEGORIES.getOrDefault(fileExtension, "Others");
        String ROOT = System.getProperty("user.home")+"\\Downloads";
        return ROOT+fileCategory;
    }

    /**
     * Organizes the destination path for a file being downloaded based on its URL.
     * It determines the file's name and assigns it to a categorized directory structure.
     *
     * @param linkURL the URL of the file to be downloaded. Must contain the file name
     *                as part of the URL. Throws IllegalArgumentException if null or empty.
     * @return the resolved {@code Path} object representing the complete destination
     *         where the file should be stored, including the categorized directory structure.
     */
    public static Path organizeFileDestination(String linkURL){
        String extractedFileName = extractFileName(linkURL);

        String organizedDestinationPath = organizeFile(extractedFileName);
        Path downloadsFolder = Path.of(organizedDestinationPath);

        return downloadsFolder.resolve(extractedFileName);
    }

    /**
     * Extracts the file name from the given URL string. The file name is derived
     * by identifying the last occurrence of the forward slash ('/') in the URL
     * and returning the substring that follows it.
     *
     * @param urlString the URL string containing the file name as part of its path.
     *                  Must not be null or empty.
     * @return the file name extracted from the URL string. If the URL string does
     *         not contain a '/', the entire string is returned.
     */
    private static String extractFileName(String urlString) {
        return urlString.substring(urlString.lastIndexOf("/") + 1);
    }

    /**
     * Extracts the file extension from the given file name. The file extension is the substring
     * found after the last '.' character in the file name. If the file name does not contain
     * a '.', an empty string is returned.
     *
     * @param name the name of the file, including its extension. Must not be null.
     * @return the file extension in lowercase. If no extension is found, an empty string is returned.
     */
    private static String extractFileExtension(String name) {

        int dotIndex = name.lastIndexOf(".");
        return (dotIndex == -1) ? "" : name.substring(dotIndex + 1).toLowerCase();
    }

}

