
public class Notification {
    private static int idCounter = 1;
    private int id;
    private int userId;
    private String message;
    private boolean read;

    public Notification(int userId, String message) {
        this.id = idCounter++;
        this.userId = userId;
        this.message = message;
        this.read = false;
    }

    public void markAsRead() {
        this.read = true;
    }

    public int getId() {
        return id;
    }


    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }
}