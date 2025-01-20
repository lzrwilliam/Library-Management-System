import java.time.LocalDate;

public class Loan implements Persistable {
    private static int idCounter = 1;
    private int id;
    private int bookId;
    private int userId;
    private LocalDate startDate;
    private LocalDate deadline;
    private LoanStatus status;

    public Loan(int bookId, int userId) {
        this.id = idCounter++;
        this.bookId = bookId;
        this.userId = userId;
        this.startDate = LocalDate.now();
        this.deadline = startDate.plusDays(10);
        this.status = LoanStatus.ACTIVE;
    }


    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toCSVString() {
        return id + "," + bookId + "," + userId + "," + startDate + "," + deadline + "," + status;
    }

    public static Loan fromCSVString(String[] data) {
        if (data.length < 6) throw new IllegalArgumentException("Invalid data for Loan");
        int id = Integer.parseInt(data[0].trim());
        int bookId = Integer.parseInt(data[1].trim());
        int userId = Integer.parseInt(data[2].trim());
        LocalDate startDate = LocalDate.parse(data[3].trim());
        LocalDate deadline = LocalDate.parse(data[4].trim());
        LoanStatus status = LoanStatus.valueOf(data[5].trim());
        Loan loan = new Loan(bookId, userId);
        loan.id = id; // Set ID manually as it's being loaded from CSV
        loan.startDate = startDate;
        loan.deadline = deadline;
        loan.status = status;
        return loan;
    }

    public int getBookId() {
        return bookId;
    }

    public int getUserId() {
        return userId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }
    public void setDeadline(LocalDate newDeadline) {
        this.deadline = newDeadline;
    }


    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", userId=" + userId +
                ", startDate=" + startDate +
                ", deadline=" + deadline +
                ", status=" + status +
                '}';
    }

}
