import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;


public class LibraryService {

    private Map<Integer, Book> books = new TreeMap<>();
    private Map<Integer, List<Loan>> userLoans = new HashMap<>();
    private List<Review> reviews = new LinkedList<>();



    private Library library;
    private NotificationService notificationService;


    public LibraryService(Library library,NotificationService notificationService) {
        this.library = library;
        this.notificationService = NotificationService.getInstance();

    }
    public List<Notification> getUnreadNotifications(int userId) {
        return NotificationService.getInstance().getUnreadNotifications(userId);
    }

    public List<Fine> getUserFines(int userId) {
        return library.getUserFines(userId);
    }

//    public void displayAllLoans() throws IOException {
//        System.out.println("All Loans:");
//        Map<Integer, List<Loan>> allLoans = library.getAllUserLoans();  // This method should exist in Library to get all loans
//        AuditService.getInstance("auditing.csv").logAction("All existing loans have benn showed");
//
//        for (Map.Entry<Integer, List<Loan>> entry : allLoans.entrySet()) {
//            int userId = entry.getKey();
//            List<Loan> loans = entry.getValue();
//            System.out.println("User ID " + userId + " has the following loans:");
//            for (Loan loan : loans) {
//                System.out.println("Loan ID: " + loan.getId() + ", Book ID: " + loan.getBookId() +
//                        ", Start Date: " + loan.getStartDate() + ", Due Date: " + loan.getDeadline() +
//                        ", Status: " + loan.getStatus());
//            }
//        }
//    }


    public void checkAndNotifyOverdueLoans() throws IOException {
        LocalDate today = LocalDate.now();
        System.out.println("Checking for overdue loans...");
        List<Loan> allLoans = library.getAllLoans(); // Make sure this method pulls all loans
        for (Loan loan : allLoans) {
            if (loan.getDeadline().isBefore(today) && loan.getStatus() == LoanStatus.ACTIVE) {
                String message = String.format("Reminder: Your loan for '%s' was due on %s.", library.getBook(loan.getBookId()).getTitle(), loan.getDeadline());
                notificationService.sendNotification(loan.getUserId(), message);
                System.out.println("Notification sent for overdue loan ID " + loan.getId());
                AuditService.getInstance("auditing.csv").logAction("Notification sent for overdue loan ID " + loan.getId());

            }
        }
    }






    // functii pentru manipularea colectiilor
    public void addBook(Book book) {
        books.put(book.getId(), book);
    }


    public void loanBook(int userId, int bookId) throws Exception {
        Book book = library.getBook(bookId);
        if (book == null) {
            throw new Exception("Book not found!");
        }
        User user = library.getUser(userId);
        if (user == null) {
            throw new Exceptii.UserNotFoundException("User with ID: " + userId + " not found.");
        }
        synchronized (this) {
//            if (book.getQuantity() <= 0) {
//                throw new Exception("No copies left for loan.");
//            }
            List<Loan> userLoans = library.getUserLoans(userId);
            boolean alreadyLoaned = userLoans.stream().anyMatch(loan -> loan.getBookId() == bookId && loan.getStatus() == LoanStatus.ACTIVE);
            if (!alreadyLoaned) {
               // library.updateBookQuantity(bookId, -1);  // Decrement the book's quantity avem deja in addloan

                Loan newLoan = new Loan(bookId, userId);
                newLoan.setStatus(LoanStatus.ACTIVE);
                library.addLoan(newLoan);

                appendLoanToCSV(newLoan);
                AuditService.getInstance("auditing.csv").logAction("User " + userId + " has loaned " + bookId + " to " + book.getTitle());



            } else {
                throw new Exceptii.BookAlreadyLoanedException("This user has already borrowed this book.");
            }
        }
    }





    public void addReview(Review review) {
        reviews.add(review);
    }

    // Metode care arată utilizarea acestor colecții
//    public void displayBooks() {
//        books.values().forEach(book -> System.out.println(book.toCSVString()));
//    }

    public void displayReviews() {
        reviews.forEach(review -> System.out.println(review.toCSVString()));
    }


    public void addBook(int userId, String title, String author, int quantity) throws Exception {
        User user = library.getUser(userId);
        if (user == null) {
            throw new Exceptii.UserNotFoundException("User with ID: " + userId + " not found.");
        }
        if (user.getRole() != UserRole.ADMIN) {
            throw new Exceptii.UnauthorizedAccessException("Only admins can add books.");
        }

        if (library.bookExists(title, author)) {
            throw new Exceptii.BookAlreadyExists("A book with the same title and author already exists.");
        }

        Book newBook = new Book(title, author, quantity);
        library.addBook(newBook);  // Adding book to the library's collection
        library.saveBooksToCSV();  // Update the CSV file with the new book data
        AuditService.getInstance("auditing.csv").logAction("Add Book: " + newBook.getTitle() + " by user" +user.getName());

    }


    public void returnBook(int userId, int bookId) throws Exception {
        System.out.println("Attempting to return book with ID: " + bookId + " for user ID: " + userId);
        List<Loan> loansForUser = library.getUserLoans(userId);
        boolean foundActiveLoan = false;

        for (Loan loan : loansForUser) {
            System.out.println("Checking loan: " + loan);  // Debugging
            if (loan.getBookId() == bookId && loan.getStatus() == LoanStatus.ACTIVE) {
                loan.setStatus(LoanStatus.RETURNED);
                library.updateBookQuantity(bookId, 1);  // Increment the book's quantity if it was active
                foundActiveLoan = true;
                AuditService.getInstance("auditing.csv").logAction("Book returned by user " + userId + ": " + library.getBook(bookId).getTitle());
                break;
            }
        }

        if (foundActiveLoan) {
            library.saveOrUpdateLoans("loans.csv");  // Save updates to the CSV file
            System.out.println("Loan successfully returned and saved.");  // Debugging
        } else {
            boolean alreadyReturned = loansForUser.stream()
                    .anyMatch(loan -> loan.getBookId() == bookId && loan.getStatus() == LoanStatus.RETURNED);
            if (alreadyReturned) {
                System.out.println("The book has already been returned by this user.");
            } else {
                throw new Exceptii.InvalidLoanOperationException("No active loan found for this book and user combination.");
            }
        }
    }









//    public void displayLoanedBooksByUser(int userId) {
//        List<Loan> loans = library.getUserLoans(userId);
//        boolean hasActiveLoans = false;
//        for (Loan loan : loans) {
//            if (loan.getStatus() == LoanStatus.ACTIVE) {
//                Book book = library.getBook(loan.getBookId());
//                if (book != null) {
//                    System.out.println("Book ID: " + book.getId() + ", Title: " + book.getTitle());
//                    hasActiveLoans = true;
//                }
//            }
//        }
//        if (!hasActiveLoans) {
//            System.out.println("No active loans for User ID: " + userId);
//        }
//    }



    private void appendLoanToCSV(Loan loan) throws IOException {
        String header = "ID,BookId,UserId,StartDate,Deadline,Status";
        CSVService.getInstance().appendToCSV("loans.csv", loan, header);
    }




    public void updateLoanDeadline(int adminUserId, int loanId, LocalDate newDeadline) throws Exception {
        User admin = library.getUser(adminUserId);
        if (admin == null) {
            throw new Exceptii.UserNotFoundException("User not found.");
        }
        if (admin.getRole() != UserRole.ADMIN) {
            throw new Exceptii.UnauthorizedAccessException("Only admins can update loan details.");
        }

        Loan loan = library.findLoanById(loanId);
        if (loan == null) {
            throw new Exceptii.InvalidLoanOperationException("Loan not found.");
        }


        loan.setDeadline(newDeadline);
        System.out.println("Loan deadline updated to " + newDeadline);
        AuditService.getInstance("auditing.csv").logAction("Loan " + loanId + " updated to " + newDeadline +" by admin "  +adminUserId);

        library.saveOrUpdateLoans("loans.csv");
    }

    // Metoda pentru actualizarea statusului unui împrumut
    public void updateLoanStatus(int adminUserId, int loanId, LoanStatus newStatus) throws Exception {
        User admin = library.getUser(adminUserId);
        if (admin == null) {
            throw new Exceptii.UserNotFoundException("User not found.");
        }
        if (admin.getRole() != UserRole.ADMIN) {
            throw new Exceptii.UnauthorizedAccessException("Only admins can update loan details.");
        }

        Loan loan = library.findLoanById(loanId);
        if (loan == null) {
            throw new Exceptii.InvalidLoanOperationException("Loan not found.");
        }

        loan.setStatus(newStatus);
        System.out.println("Loan status updated to " + newStatus);

        library.saveOrUpdateLoans("loans.csv");

    }


}


