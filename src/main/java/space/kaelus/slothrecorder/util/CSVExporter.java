package space.kaelus.slothrecorder.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CSVExporter {
    private final File file;
    private PrintWriter writer;

    public CSVExporter(String label, String customName) {
        Path dir = Paths.get("slothac_data");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = (customName != null ? customName : label) + "_" + timestamp + ".csv";
        this.file = dir.resolve(fileName).toFile();

        try {
            this.writer = new PrintWriter(new FileWriter(file, true));
            writer.println("is_cheating,delta_yaw,delta_pitch,accel_yaw,accel_pitch,jerk_yaw,jerk_pitch,gcd_error_yaw,gcd_error_pitch");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeRow(String row) {
        if (writer != null) {
            writer.println(row);
        }
    }

    public void flush() {
        if (writer != null) {
            writer.flush();
        }
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }

    public String getFilePath() {
        return file.getAbsolutePath();
    }
}
