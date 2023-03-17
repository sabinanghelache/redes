package es.udc.redes.tutorial.copy;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Copy {

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("java es.udc.redes.tutorial.copy.Copy <source file> <destination file>");
            System.exit(-1);
        }

        try (FileInputStream inputStream = new FileInputStream(args[0]); FileOutputStream outputStream = new FileOutputStream(args[1])) {

            int c;
            while ((c = inputStream.read()) != -1) {
                outputStream.write(c);
            }
        }

    }
}
