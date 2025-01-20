
public class Config {
    private static final String BASE_PATH = System.getProperty("user.dir") + "/src/Date/";

    // Method to concatenate base path with file name
    public static String getFullPath(String fileName) {
        return BASE_PATH + fileName;
    }
}
