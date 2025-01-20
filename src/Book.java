import java.util.Arrays;

public class Book implements Persistable {
    private static int idCounter = 1;
    private int id;
    private String title;
    private String author;
    private int quantity;  // nr carti disponibile


    public Book(String title, String author, int quantity) {
        this.id = idCounter++;
        this.title = title;
        this.author = author;
        this.quantity=quantity;
    }
@Override
    public int getId() {
        return id;
    }
    @Override
    public String toCSVString() {
    return id + "," + title + "," + author +","+quantity;
    }
    public String toString() {
        return String.format("%s by %s", title, author);  // Afișează titlul și autorul cărții
    }

    public static Book fromCSVString(String[] data) {
        if (data.length < 4) throw new IllegalArgumentException("Invalid data for Book");
        int id = Integer.parseInt(data[0].trim());
        String title = data[1].trim();
        String author = data[2].trim();
        int quantity = Integer.parseInt(data[3].trim());

        Book book = new Book(title, author,quantity);
        book.id = id; //setarea manuala a id cand e incarcata din csv
        return book;
    }





    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
