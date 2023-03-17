package es.udc.redes.webserver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HandleRequest {
    private static final String rp = "."+File.separator+"p1-files";


    public static byte[] parse(String s, ServerThread sth) {
        String errpath=rp+File.separator+"error400.html";
        File errfile= new File(errpath);
        byte[] error = readFile(errpath);

        String[] lines = s.split("\\r?\\n|\\r");
        String[] tokens = lines[0].split(" ");

        if (tokens.length < 3){
            if(!errfile.exists()){
                errfile= CreateErr(errpath,400);
                error = readFile(errpath);
            }
            return convertToByte(Head(tokens,true,false,false,sth,errfile),error);
        }
        else{

            switch (tokens[0]) {
                case "GET":
                    return Get(tokens, lines, sth);
                case "HEAD":
                    return Head(tokens, false, false,false, sth,null).getBytes();
            }
        }
        if(!errfile.exists()){
            errfile= CreateErr(errpath,400);
            error = readFile(errpath);
        }
        return convertToByte(Head(tokens,true,false,false,sth,errfile),error);
    }

    public static byte[] convertToByte(String header, byte[] content) {
        int i, j;
        byte[] answer;
        // Copyes both the header and the resource in the output byte array
        byte[] stringBytes = header.getBytes();
        if (content != null) {
            answer = new byte[stringBytes.length + content.length];
            for (i = 0; i < stringBytes.length; i++) answer[i] = stringBytes[i];
            j = i;
            for (i = j; i < answer.length; i++) answer[i] = content[i - j];
        } else {
            answer = stringBytes;
        }

        return answer;

    }

    public static byte[] readFile(String path) {
        File file = new File(path);
        if (!file.exists() || !file.canRead()) return null;
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            return null;
        }
    }

    public static String ifModifiedSince(String[] lines) {
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].contains("If-Modified-Since")) {
                StringBuilder sb = new StringBuilder(lines[i]);
                sb.delete(0, 19);
                return sb.toString();
            }
        }
        return null;
    }

    public static void updateLog(String[] request, boolean valid, String answer, long size, ServerThread sth) {
        int i = 0;
        String accessLogDir = rp + File.separator + "access_log.txt";
        String errorsLogDir = rp + File.separator + "errors_log.txt";
        String req;
        if (valid) req = accessLogDir; else req = errorsLogDir;
        try {
            Files.write(Paths.get(req), ("Petition received:").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            while (i < request.length && i < 3) {
                Files.write(Paths.get(req), (" "+request[i]).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                i++;
            }
            Files.write(Paths.get(req), (System.lineSeparator()+"From: "+sth.getSocket().getRemoteSocketAddress().toString()+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            Files.write(Paths.get(req), (String.format("Date: %tc", sth.getDate())+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            Files.write(Paths.get(req), ("Server answer: "+answer+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            if (valid) Files.write(Paths.get(req), ("Sent resource size: "+ size +System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            Files.write(Paths.get(req), System.lineSeparator().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println("ERROR: CANNOT WRITE LOG FILE");
            System.err.println("Error: " + e.getMessage());
        }

    }

    public static File CreateErr(String path,int err){
        String content= "<html>\n" +
                "   <head>\n" +
                "      <title>\n" +
                "         ERROR "+err+"\n" +
                "      </title>\n" +
                "   </head>\n" +
                "   <body>\n" +
                "      <p>ERROR "+err+"</p>\n" +
                "   </body>\n" +
                "</html>";
        try {
            File outputFile = new File(path);
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write(content);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println("ERROR: CANNOT CREATE FILE");
            System.err.println("Error: " + e.getMessage());
        }
        return new File(path);
    }


    public static byte[] Get(String[] request, String[] lines, ServerThread sth) {
        String filepath = request[1];

        String path = rp + filepath;

        if (new File(rp).isDirectory()) {
            File file = new File(path);
            if (!file.exists()) {
                String errpath=rp+File.separator+"error404.html";
                File errfile= new File(errpath);
                byte[] error = readFile(errpath);
                if(!errfile.exists()){

                    errfile= CreateErr(errpath,404);
                    error= readFile(errpath);
                }
                return convertToByte(Head(request,false,true,false,sth,errfile),error);
            }
        }
        File rfile = new File(path);
        String ifMod = ifModifiedSince(lines);




        if (ifMod != null) {
            Date filedate= new Date(rfile.lastModified());
            Date modDate = null;
            try {
                SimpleDateFormat parser= new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                modDate = parser.parse(ifMod);

            } catch (ParseException e) {
                SimpleDateFormat parser= new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.ENGLISH);
                try {
                    modDate = parser.parse(ifMod);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
            if (!filedate.before(modDate)) {
                return convertToByte(Head(request,false,false,true,sth,null),null);
            }
        }

        byte[] content = readFile(path);

        return convertToByte(Head(request,false,false,false,sth,null),content);

    }

    public static String Head(String[] request,boolean err400,boolean err404,boolean mod,ServerThread sth,File errfile){

        StringBuilder header = new StringBuilder("HTTP/1.0 ");
        String answ;

        long size = 0;

        boolean valid = false;
        String rPh =rp;
        if(request.length >=2){
            String filepath = request[1];
            rPh=rPh.concat(filepath);
        }

        File rfile= new File(rPh);

        if(mod){
            header.append("304 Not Modified");
            answ = "304 Not Modified";
            valid=true;
        }else if (err400) {header.append("400 Bad Request");
            answ = "400 Bad Request";
        } else if (err404 || !rfile.exists()) {
            header.append("404 Not Found");
            answ = "404 Not Found";
        }else if((rfile.isDirectory() || !rfile.canRead())){
            header.append("403 Forbidden");
            answ = "403 Forbidden";
        }else {
            header.append("200 OK");
            answ = "200 OK";
            valid=true;
        }
        header.append(System.lineSeparator());

        header.append(String.format("Date: %tc", new Date()));
        header.append(System.lineSeparator());

        header.append("Server: ");
        header.append("WebServer_667");
        header.append(System.lineSeparator());

        if (!err400 && !err404 && !mod){
            if (!rfile.isDirectory() && rfile.canRead()) {

                // Content length
                header.append("Content-Length: ");
                header.append(rfile.length());
                header.append(System.lineSeparator());
                size = rfile.length();
                try {
                    // Content type
                    header.append("Content-Type: ");
                    header.append(Files.probeContentType(rfile.toPath()));
                    header.append(System.lineSeparator());
                    // Last modified line
                    header.append("Last-Modified: ");
                    header.append(new Date(rfile.lastModified()));
                    header.append(System.lineSeparator());
                } catch (IOException e) {
                    System.out.println("CANNOT GET FILE PATH");
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }else if(err400 || err404){
            header.append("Content-Length: ");
            header.append(errfile.length());
            header.append(System.lineSeparator());
            try {
                // Content type
                header.append("Content-Type: ");
                header.append(Files.probeContentType(errfile.toPath()));
                header.append(System.lineSeparator());

            } catch (IOException e) {
                System.out.println("CANNOT GET FILE PATH");
                System.err.println("Error: " + e.getMessage());
            }
        }

        header.append(System.lineSeparator());

        updateLog(request, valid, answ, size, sth);
        return header.toString();

    }
}
