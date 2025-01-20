

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class Library {
    private Map<Integer, Book> books = new HashMap<>();
    private Map<Integer, User> users = new HashMap<>();
    private Map<Integer, List<Loan>> loans = new HashMap<>();
    private Map<Integer, List<Review>> reviews = new HashMap<>();
private FinesManager finesManager;
    public Library() {
        try {
            loadBooks();
            loadUsers();
            loadLoans();
            loadReviews();
            this.finesManager = new FinesManager(this, new LibraryService(this, NotificationService.getInstance()));

        } catch (IOException e) {
            System.err.println("Failed to load data: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addBook(Book book) {
        books.put(book.getId(), book);
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    public void addLoan(Loan loan) throws Exception {
        Book book = getBook(loan.getBookId());
        if (book == null) {
            throw new Exceptii.BookNotFound("Book not found with ID: " + loan.getBookId());
        }
        if (book.getQuantity() <= 0) {
            throw new Exceptii.BookNotAvailableException("No copies left for loan for Book ID: " + loan.getBookId());
        }

        List<Loan> userLoans = loans.getOrDefault(loan.getUserId(), new ArrayList<>());
        if (userLoans.stream().anyMatch(l -> l.getBookId() == loan.getBookId() && l.getStatus() == LoanStatus.ACTIVE)) {
            throw new Exceptii.BookAlreadyLoanedException("User ID " + loan.getUserId() + " has already borrowed Book ID " + loan.getBookId());
        }

        book.setQuantity(book.getQuantity() - 1);  // Decrement the book's quantity
        userLoans.add(loan);
        loans.put(loan.getUserId(), userLoans);
        System.out.println("Loan added for user " + loan.getUserId() + " "+loan ); // Debugging

    }


    public Book getBook(int bookId) {
        return books.get(bookId);
    }

    public void addReview(Review review) throws Exception {
        addReview(review, false); // Apelam varianta supraincarcata cu `false` ca implicit
    }



    public void addReview(Review review, boolean isLoadedFromCSV) throws Exception {
        if (!isLoadedFromCSV) {
            List<Loan> userLoans = loans.getOrDefault(review.getUserId(), new ArrayList<>());
            boolean hasLoanedBook = userLoans.stream()
                    .anyMatch(loan -> loan.getBookId() == review.getBookId());


            if (!hasLoanedBook) {
                throw new Exceptii.ReviewNotPermittedException("User ID " + review.getUserId() + " has not loaned Book ID " + review.getBookId() + " and cannot leave a review.");
            }
        }

        List<Review> bookReviews = reviews.getOrDefault(review.getBookId(), new ArrayList<>());
        boolean reviewExists = false;


// Check if the user has already reviewed this book
        for (Review existingReview : bookReviews) {
            if (existingReview.getUserId() == review.getUserId()) {
                existingReview.setReviewText(review.getReviewText());  // Update the existing review text
                reviewExists = true;
                break;
            }
        }

// If no existing review, add the new one
        if (!reviewExists) {
            bookReviews.add(review);
        }

        reviews.put(review.getBookId(), bookReviews);

// Update the CSV file with the new review data only if it's not a load operation
        if (!isLoadedFromCSV) {
            saveReviewsToCSV();
        }
    }

    public User getUser(int userId) {
        return users.get(userId);
    }

    public List<Loan> getUserLoans(int userId) {
        return loans.getOrDefault(userId, new ArrayList<>());
    }

//   // public void displayAllBooks() {
//        books.values().forEach(book -> System.out.println(book.toCSVString()));
//    }

//    public void displayAllUsers() {
//        users.values().forEach(user -> System.out.println(user.toCSVString()));
//    }

    public synchronized void updateBookQuantity(int bookId, int change) throws Exception {
        Book book = getBook(bookId);
        if (book != null) {
            int newQuantity = book.getQuantity() + change;
            if (newQuantity < 0) {
                throw new Exceptii.BookNotAvailableException("Insufficient number of books available.");
            }
            book.setQuantity(newQuantity);
        } else {
            throw new Exceptii.BookNotFound("Book not found.");
        }
    }

    public Collection<Book> getAllBooks() {
        return books.values();
    }

    public List<Loan> getAllLoans() {
        return loans.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<Review> getAllReviews() {
        return reviews.values().stream()
                .flatMap(List::stream) // Flatten the list of lists to a single list
                .collect(Collectors.toList());
    }




    private void loadLoans() throws Exception {
        Map<Integer, Loan> loansData = CSVService.getInstance().loadFromCSV("loans.csv", Loan::fromCSVString);

        for (Loan loan : loansData.values()) {
            try {
                Book book = getBook(loan.getBookId());
                if (book == null) {
                    throw new Exceptii.BookNotFound("Book not found for loan ID: " + loan.getId());
                }
                // Verifica daca exista exemplare disponibile inainte de a incerca sa adaugi imprumutul
                if (book.getQuantity() <= 0) {
                    throw new Exceptii.InvalidLoanOperationException("Not enough copies of book ID " + book.getId() + " available for loan ID " + loan.getId());
                }
                System.out.println("Loaded loan: " + loan);  // Debugging

                // Add loan decrements the quantity if the book is available
                addLoan(loan);
            } catch (Exception e) {
                System.out.println("Error loading loan: " + e.getMessage());
                throw e; // dam thorw mai departe ca sa ajunga in main si sa oprim executia
            }
        }
    }


    public Map<Integer, List<Loan>> getAllUserLoans() {
        return new HashMap<>(loans);  // returnam copie casa prevenim modificari externe
    }


    public void loadReviews(String filePath) throws Exception {
        Function<String[], Review> mapper = Review::fromCSVString;
        Map<Integer, Review> reviewsMap = CSVService.getInstance().loadFromCSV(filePath, mapper);
        for (Review review : reviewsMap.values()) {
            addReview(review, true);  // adaugam flag ca sa indicam ca review e incarcat din csv
        }
    }


    public List<Review> getReviewsForBook(int bookId) throws Exceptii.BookNotAvailableException {
        if (!books.containsKey(bookId)) {
            throw new Exceptii.BookNotAvailableException("Book with ID: " + bookId + " not found.");
        }
        List<Review> bookReviews = reviews.getOrDefault(bookId, new ArrayList<>());
        if (bookReviews.isEmpty()) {
            System.out.println("No reviews available for Book ID: " + bookId); // afisare in main consola
        } else {
            System.out.println("Reviews for Book ID: " + bookId + ":");
            for (Review review : bookReviews) {
                System.out.println(review.getReviewText() + " - User ID: " + review.getUserId());
            }
        }
        return bookReviews;

    }


    public void saveReviewsToCSV() throws IOException {
        List<Review> allReviews = reviews.values().stream()
                .flatMap(List::stream) // Flatten the list of lists to a single list
                .collect(Collectors.toList());
        CSVService.getInstance().saveToCSV("reviews.csv", allReviews, "ID,BookId,UserId,ReviewText");
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values()); // returneaza lista cu toti users
    }



    public void saveBooksToCSV() throws IOException {
        CSVService.getInstance().saveToCSV("books.csv", books.values(), "ID,Title,Author,Quantity");
    }


    public boolean bookExists(String title, String author) {
        return books.values().stream()
                .anyMatch(book -> book.getTitle().equalsIgnoreCase(title) && book.getAuthor().equalsIgnoreCase(author));
    }


    public List<BookStats> displayTopThreeMostPopularBooks() {
        Map<Integer, Long> bookFrequency = new HashMap<>();
        Map<Integer, LocalDate> mostRecentLoanStartDate = new HashMap<>();

        for (List<Loan> loanList : loans.values()) {
            for (Loan loan : loanList) {
                bookFrequency.merge(loan.getBookId(), 1L, Long::sum);
                mostRecentLoanStartDate.merge(loan.getBookId(), loan.getStartDate(), (oldDate, newDate) -> newDate.isAfter(oldDate) ? newDate : oldDate);
            }
        }

        List<BookStats> topBooks = bookFrequency.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()
                        .thenComparing(e -> mostRecentLoanStartDate.get(e.getKey()), Comparator.reverseOrder()))
                .limit(3)
                .map(e -> new BookStats(books.get(e.getKey()), e.getValue(), mostRecentLoanStartDate.get(e.getKey())))
                .collect(Collectors.toList());

        return topBooks;}


    public Loan findLoanById(int loanId) {
        for (List<Loan> loanList : loans.values()) {
            for (Loan loan : loanList) {
                if (loan.getId() == loanId) {
                    return loan;
                }
            }
        }
        return null; // Return null if the loan is not found
    }



    public void saveOrUpdateLoans(String filePath) throws IOException {
        List<Loan> allLoans = getAllLoans();
        String header = "ID,BookId,UserId,StartDate,Deadline,Status";
        CSVService.getInstance().saveToCSV(filePath, allLoans, header);  // folosim csvservice pentru scriere
    }




    private void loadBooks() throws IOException {
        Map<Integer, Book> loadedBooks = CSVService.getInstance().loadFromCSV("books.csv", Book::fromCSVString);
        loadedBooks.values().forEach(this::addBook);
    }

    private void loadUsers() throws IOException {
        Map<Integer, User> loadedUsers = CSVService.getInstance().loadFromCSV("users.csv", User::fromCSVString);
        loadedUsers.values().forEach(this::addUser);
    }
    public List<Fine> getUserFines(int userId) {
        return finesManager.getFinesForUser(userId);
    }




    private void loadReviews() throws Exception {
        this.loadReviews("reviews.csv");
    }

}









