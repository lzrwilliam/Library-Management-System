import java.time.LocalDate;

// Clasa folosita pentru afisarea top carti
public class BookStats {
    private Book book;
    private long loanCount;
    private LocalDate recentLoanDate;

    public BookStats(Book book, long loanCount, LocalDate recentLoanDate) {
        this.book = book;
        this.loanCount = loanCount;
        this.recentLoanDate = recentLoanDate;
    }

    public Book getBook() {
        return book;
    }

    public long getLoanCount() {
        return loanCount;
    }

    public LocalDate getRecentLoanDate() {
        return recentLoanDate;
    }
}
