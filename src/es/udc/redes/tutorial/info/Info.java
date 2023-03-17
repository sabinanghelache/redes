package es.udc.redes.tutorial.info;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Info {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java es.udc.redes.tutorial.info.Info <relative path>");
            System.exit(-1);
        }

        String filePath = "p0-files/" + args[0];
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("Error: File does not exist.");
            System.exit(-1);
        }

        long size = file.length();
        Date lastModifiedDate = new Date(file.lastModified());
        String name = file.getName();
        String extension = "";
        String fileType = "";

        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = name.substring(dotIndex + 1);
        }

        if (file.isDirectory()) {
            fileType = "directory";
        } else if (extension.equalsIgnoreCase("txt")) {
            fileType = "text";
        } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif")) {
            fileType = "image";
        } else {
            fileType = "unknown";
        }

        String absolutePath = file.getAbsolutePath();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastModifiedDateString = dateFormat.format(lastModifiedDate);

        System.out.println("Size: " + size + " bytes");
        System.out.println("Last modified date: " + lastModifiedDateString);
        System.out.println("Name: " + name);
        System.out.println("Extension: " + extension);
        System.out.println("File type: " + fileType);
        System.out.println("Absolute path: " + absolutePath);
    }
}
