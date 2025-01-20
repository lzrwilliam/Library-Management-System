import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Fine implements  Persistable {
    private static int nextId = 1; // counter ca sa ne asiguram de id unic
    private int fineId;

    private int userId;
    private double amount;
    private LocalDate dueDate;
    private boolean paid;
    private String description;
    private int bookId;


    public Fine(int userId, double amount, LocalDate dueDate, String description,int Bookid) {
        this.fineId = nextId++;

        this.userId = userId;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paid = false;
        this.description = description;
        this.bookId=Bookid;
    }

    public void markAsPaid() {
        this.paid = true;
    }
    public String getDescription() {
        return description;
    }
    public int getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }
    public int getId() {
        return fineId;
    }
    public String toCSVString() {
        return fineId + "," + userId + "," + amount + "," + paid + "," + description + "," + bookId;
    }



    public void setAmount(double amount) {
        this.amount = amount;
    }


    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public int getBookId() {
        return bookId;
    }
    public static Fine fromCSVString(String[] data) {
        if (data.length < 6) {
            throw new IllegalArgumentException("Invalid data for Fine");
        }

        int fineId = Integer.parseInt(data[0].trim());
        int userId = Integer.parseInt(data[1].trim());
        double amount = Double.parseDouble(data[2].trim());
        boolean paid = Boolean.parseBoolean(data[3].trim());

        // Concateneaza toate partile descrierii incepand cu indexul 4 pana la penultimul index
        String description = String.join(",", Arrays.copyOfRange(data, 4, data.length - 1)).trim();

        int bookId = Integer.parseInt(data[data.length - 1].trim());

        Fine fine = new Fine(userId, amount, LocalDate.now(), description, bookId);
        fine.setFineId(fineId);
        fine.setPaid(paid);
        return fine;
    }

    public void setFineId(int fineId) {
        this.fineId = fineId;
    }



}


