

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Test {
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\journaldev.txt"))) {
            System.out.println(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

