import java.io.*;

import java.util.*;

import java.util.function.Function;

public class CSVService {
    private static CSVService instance;

    private CSVService() {}

    public static CSVService getInstance() {
        if (instance == null) {
            instance = new CSVService();
        }
        return instance;
    }



    public <T extends Persistable> Map<Integer, T> loadFromCSV(String fileName, Function<String[], T> mapper) throws IOException {
        Map<Integer, T> items = new HashMap<>();
        String filePath = Config.getFullPath(fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // dam skip la header fisierului
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] data = line.split(",");
                    T item = mapper.apply(data);
                    items.put(item.getId(), item);
                    System.out.println("Loaded: " + item.toCSVString()); // vedem ce date se incarca

                }
            }
        }

        return items;
    }

    public void saveToCSV(String fileName, Collection<? extends Persistable> items, String header) throws IOException {
        String filePath = Config.getFullPath(fileName);

        try (PrintWriter out = new PrintWriter(new FileWriter(filePath))) {
            out.println(header);  // Scrie antetul fisierului
            for (Persistable item : items) {
                out.println(item.toCSVString());  // Scrie fiecare element in fisierul CSV
            }
        }
    }

    public void appendToCSV(String fileName, Persistable item, String header) throws IOException {

        String filePath = Config.getFullPath(fileName);

        File file = new File(filePath);
        boolean writeHeader = file.length() == 0;

        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                out.println(header);  // Write the header if the file is new or empty
            }
            out.println(item.toCSVString());  // Append the new item
        }
    }





}




