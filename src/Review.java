public class Review implements Persistable {
    private static int idCounter = 1;
    private int id;
    private int bookId;
    private int userId;
    private String reviewText;

    public Review(int bookId, int userId, String reviewText) {
        this.id = idCounter++;
        this.bookId = bookId;
        this.userId = userId;
        this.reviewText = reviewText;
    }


    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toCSVString() {
        return id + "," + bookId + "," + userId + "," + reviewText;
    }


    public static Review fromCSVString(String[] data) {
        if (data.length < 4) throw new IllegalArgumentException("Invalid data for Review");

        try {
            int id = Integer.parseInt(data[0].trim());
            int bookId = Integer.parseInt(data[1].trim());
            int userId = Integer.parseInt(data[2].trim());
            String reviewText = data[3].trim();

            Review review = new Review(bookId, userId, reviewText);
            review.id = id;  // setam id manual pt ca este incarcat din csv
            return review;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error parsing integers from CSV", e);
        }
    }


    public int getBookId() {
        return bookId;
    }


    public int getUserId() {
        return userId;
    }
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getReviewText() {
        return reviewText;
    }

    private static synchronized int getNextId() {
        return idCounter++;
    }

    // Method to set the max ID from loaded reviews
    public static void setMaxId(int id) {
        if (id >= idCounter) {
            idCounter = id + 1;
        }
    }
}
