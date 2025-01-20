import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditService {
    private static AuditService instance;
    private String filePath;

    private AuditService(String filePath) {
        this.filePath = Config.getFullPath("audit.csv");
    }

    public static AuditService getInstance(String filePath) {
        if (instance == null) {
            instance = new AuditService(filePath);
        }
        return instance;
    }

    public void logAction(String actionName) throws IOException {
        File file = new File(filePath);
        boolean writeHeader = file.length() == 0;
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                out.println("Action, Timestamp");  // Write the header if the file is new or empty
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            out.println(actionName + ", " + timestamp);
        }
    }
}
