import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;


public class LibraryGUI extends JFrame {
    private Library library;
    private JTable UserTable;
    private JTable dataTable;

    private DefaultTableModel model;
    private DefaultTableModel dataModel;

    private JMenuBar menuBar;
    private int selectedUserId; // variabila pentru utilizatoru curent global
    private LibraryService libraryService;
    private FinesManager finesmanager;
    private Search search;
    private JTextField textField1;
    private JPanel panel1;


    public LibraryGUI(Library library) {
        this.library = library;
        this.libraryService = new LibraryService(library, NotificationService.getInstance()); // initializam libraryservice
        this.search = new Search(library); // constructori ca sa putem folosii functiile din celelalte clase fara a le face statice
        this.finesmanager = new FinesManager(library, libraryService);
        initializeUI();
        loadUserData();
    }


    private void initializeUI() {
        setTitle("Library User Data");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Surname", "Role"});
        UserTable = new JTable(model);
        JScrollPane usersScrollPane = new JScrollPane(UserTable);

        dataModel = new DefaultTableModel();
        dataTable = new JTable(dataModel);
        JScrollPane dataScrollPane = new JScrollPane(dataTable);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, usersScrollPane, dataScrollPane);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

        InitializeMenu();

    }

    private void InitializeMenu() {
        menuBar = new JMenuBar();

        // Books Menu
        JMenu booksMenu = new JMenu("Books");
        booksMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                displayBooks(); //afisam cartile cand avem cursorul pe Books
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        JMenuItem addBook = new JMenuItem("Add Book");
        addBook.addActionListener(e -> showAddBookDialog());
        JMenuItem updateBookQuantity = new JMenuItem("Update Book Quantity");
        updateBookQuantity.addActionListener(e -> showUpdateBookQuantityDialog());
        JMenuItem displayTopBooks = new JMenuItem("Display Top Three Most Popular Books");
        displayTopBooks.addActionListener(e -> showTopThreeBooks());
        booksMenu.add(addBook);
        booksMenu.add(updateBookQuantity);
        booksMenu.add(displayTopBooks);

        // Loans Menu
        JMenu loansMenu = new JMenu("Loans");
        loansMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                displayLoans(); //afisam toate loans cand mouse pe loans
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        JMenuItem addLoan = new JMenuItem("Take a Loan");
        addLoan.addActionListener(e -> showAddLoanDialog()); // adaugam actiune cand apasam pe addloan
        JMenuItem returnBook = new JMenuItem("Return Book");
        returnBook.addActionListener(e -> showReturnBookDialog());

        JMenuItem displayAllLoans = new JMenuItem("Display All Loans for current user");
        displayAllLoans.addActionListener(e -> showLoansForCurrentUser());
        JMenuItem updateLoanDeadline = new JMenuItem("Update Loan Deadline");
        updateLoanDeadline.addActionListener(e -> UpdateLoanDeadline());
        JMenuItem updateLoanStatus = new JMenuItem("Update Loan Status");
        updateLoanStatus.addActionListener(e -> UpdateLoanStatus());
        loansMenu.add(addLoan);
        loansMenu.add(returnBook);
        loansMenu.add(displayAllLoans);
        loansMenu.add(updateLoanDeadline);
        loansMenu.add(updateLoanStatus);


        // Reviews Menu
        JMenu reviewsMenu = new JMenu("Reviews");
        reviewsMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                displayReviews();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        JMenuItem addReview = new JMenuItem("Add Review");
        addReview.addActionListener(e -> showAddReviewDiaolog());
        JMenuItem displayReviews = new JMenuItem("Display Reviews For Book");
        displayReviews.addActionListener(e -> reviewsForABookDialog());
        reviewsMenu.add(addReview);
        reviewsMenu.add(displayReviews);

        // Fines Menu
        JMenu finesMenu = new JMenu("Fines");

        JMenuItem showFine = new JMenuItem("Show User Fines");
        Object showUsersFinesDialog;
        showFine.addActionListener(e -> showUsersFinesDialog());
        JMenuItem payFine = new JMenuItem("Pay Fine");
        payFine.addActionListener(e -> PayFineDialog());
        JMenuItem modifyFine = new JMenuItem("Modify Fine");
        modifyFine.addActionListener(e -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();




            try {
                if (currentUser == null) {

                    throw new Exceptii.UserNotFoundException("Select a user who does the action!");
                } else if (!currentUser.getRole().equals(UserRole.ADMIN)) {
                    throw new Exceptii.UnauthorizedAccessException("You do not have permission to modify fines.");
                }

                displayAllFines();
                JOptionPane.showMessageDialog(this, "Click on the fine in the table you want to edit.", "Select", JOptionPane.INFORMATION_MESSAGE);

                dataTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) { // verificam daca s-a dat click
                            int row = dataTable.getSelectedRow();
                            if (row != -1) {
                                ModifyFineDialog();
                            }

                        }
                    }
                });

            } catch (Exceptii.UserNotFoundException | Exceptii.UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(ex);
            }
        });
        finesMenu.add(payFine);
        finesMenu.add(showFine);
        finesMenu.add(modifyFine);

        // Search Menu
        JMenu searchMenu = new JMenu("Search");
        JMenuItem searchByTitle = new JMenuItem("By Title");
        searchByTitle.addActionListener(e -> showSearchByNameDialog());
        JMenuItem searchByAuthor = new JMenuItem("By Author");
        searchByAuthor.addActionListener(e -> showSearchByAuthorialog());
        searchMenu.add(searchByTitle);
        searchMenu.add(searchByAuthor);

        JMenu notificationMenu = new JMenu("Notifications");
        JMenuItem unreadNotifications = new JMenuItem("Show Unread Notifications");
        unreadNotifications.addActionListener(e -> showUnreadNotificationsDialog());
        notificationMenu.add(unreadNotifications);

        menuBar.add(booksMenu);
        menuBar.add(loansMenu);
        menuBar.add(reviewsMenu);
        menuBar.add(finesMenu);
        menuBar.add(searchMenu);
        menuBar.add(notificationMenu);

        this.setJMenuBar(menuBar);
    }


    private void showUpdateBookQuantityDialog() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            JOptionPane.showMessageDialog(this, "Only logged admins can update book quantities.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JDialog dialog = new JDialog(this, "Update Book Quantity", true);
        dialog.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        JComboBox<Book> bookComboBox = new JComboBox<>();
        JTextField quantityField = new JTextField(5);

        // Populăm combobox-ul cu cărțile disponibile
        library.getAllBooks().forEach(bookComboBox::addItem);  // Adaugarea cartilor in combobox


        inputPanel.add(new JLabel("Select Book:"));
        inputPanel.add(bookComboBox);
        inputPanel.add(new JLabel("New Quantity:"));
        inputPanel.add(quantityField);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            Book selectedBook = (Book) bookComboBox.getSelectedItem();
            try {
                int newQuantity = Integer.parseInt(quantityField.getText());
                selectedBook.setQuantity(newQuantity);
                library.saveBooksToCSV();  // Presupunem că există o metodă pentru a salva modificările într-un fișier CSV
                JOptionPane.showMessageDialog(dialog, "Quantity updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                updateDataDisplay("Books");  // Actualizăm afișarea cărților
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating the book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(updateButton, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void showUnreadNotificationsDialog() {
        try {
            if (selectedUserId <= 0) {

                throw new Exceptii.UserNotFoundException("No user is currently selected.");
            }
            List<Notification> unreadNotifications = NotificationService.getInstance().getUnreadNotifications(selectedUserId);
            if (unreadNotifications.isEmpty()) {
                throw new Exceptii.NoUnreadNotifications("No unread notifications for user ID: " + selectedUserId);

            } else {
                updateNotificationsTable(unreadNotifications);
            }
            dataTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) { //marcam cu click notificarea citita
                        int row = dataTable.getSelectedRow();
                        if (row != -1) {
                            int notificationId = (Integer) dataTable.getValueAt(row, 0);
                            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to mark this notification as read?", "Confirm Read", JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                NotificationService.getInstance().markNotificationAsRead(notificationId);
                                unreadNotifications.clear();
                                unreadNotifications.addAll(NotificationService.getInstance().getUnreadNotifications(selectedUserId));
                                updateNotificationsTable(unreadNotifications); // Refresh table
                            }
                        }
                    }
                }
            });
        } catch (Exceptii.UserNotFoundException | Exceptii.NoUnreadNotifications e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
    }


    private void updateNotificationsTable(List<Notification> notifications) {
        String[] columnNames = {"ID", "Message", "Read"};
        dataModel.setColumnIdentifiers(columnNames);
        dataModel.setRowCount(0); // stergem datele vechi

        for (Notification notification : notifications) {
            dataModel.addRow(new Object[]{notification.getId(), notification.getMessage(), notification.isRead() ? "Yes" : "No"});
        }
    }

    private void displayAllFines() {
        try {
            List<Fine> allFines = new ArrayList<>();
            List<User> allUsers = library.getAllUsers();

            // luam amenzile tuturor userilor
            for (User user : allUsers) {
                allFines.addAll(libraryService.getUserFines(user.getId()));
            }

//punem amenzile in data tabel
            updateDataTableWithAllFines(allFines);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading fines: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDataTableWithAllFines(List<Fine> fines) {
        String[] columnNames = {"Fine ID", "User ID", "Amount", "Paid", "Description", "Book ID"};
        dataModel.setColumnIdentifiers(columnNames);
        dataModel.setRowCount(0);

        for (Fine fine : fines) {
            dataModel.addRow(new Object[]{
                    fine.getId(),
                    fine.getUserId(),
                    String.format("%.2f", fine.getAmount()),
                    fine.isPaid() ? "Yes" : "No",
                    fine.getDescription(),
                    fine.getBookId()
            });
        }
    }

    private void ModifyFineDialog() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        int row = dataTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "No fine selected. Please select a fine first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        int fineId = (Integer) dataModel.getValueAt(row, 0);
        String description = (String) dataModel.getValueAt(row, 4);
        double amount = Double.parseDouble((String) dataModel.getValueAt(row, 2));
        boolean isPaid = "Yes".equals((String) dataModel.getValueAt(row, 3));

        //crearea dialogului
        JDialog modifyDialog = new JDialog(this, "Modify Fine", true);
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField descriptionField = new JTextField(description);
        descriptionField.setEditable(false);

        JTextField amountField = new JTextField(String.format("%.2f", amount));
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"No", "Yes"});
        statusComboBox.setSelectedItem(isPaid ? "Yes" : "No");

        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("Paid:"));
        panel.add(statusComboBox);

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            try {
                double newAmount = Double.parseDouble(amountField.getText());
                boolean newPaidStatus = "Yes".equals(statusComboBox.getSelectedItem());
                // Call the modifyFine method with appropriate action and new value
                finesmanager.modifyFine(currentUser.getId(), fineId, FineModificationAction.UPDATE_AMOUNT, newAmount);
                finesmanager.modifyFine(currentUser.getId(), fineId, FineModificationAction.UPDATE_PAID_STATUS, newPaidStatus);
                JOptionPane.showMessageDialog(modifyDialog, "Fine updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                ReloadAllFines();
                modifyDialog.dispose();
                dataTable.clearSelection(); // Add this line to clear the selection

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(modifyDialog, "Invalid amount format. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(modifyDialog, "Error updating fine: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        modifyDialog.add(panel, BorderLayout.CENTER);
        modifyDialog.add(saveButton, BorderLayout.SOUTH);
        modifyDialog.pack();
        modifyDialog.setLocationRelativeTo(this);
        modifyDialog.setVisible(true);
    }

    private void ReloadAllFines() { // functie cu care dam refresh si uploadam amenzile dupa ce au fost modificate pentru a se vedea modificarile aduse
        try {
            List<Fine> allFines = finesmanager.getAllFines();
            updateDataTableWithAllFines(allFines);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reloading fine data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void PayFineDialog() {
        showUsersFinesDialog();
        if (selectedUserId > 0) {
            List<Fine> fines = libraryService.getUserFines(selectedUserId);
            if (!fines.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Click on a fine you want to pay!");
                dataTable.addMouseListener(payFineMouseListener); // Adaugam listener-ul specific pentru Pay Fine
            }
        }
    }

    private MouseListener payFineMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                int row = dataTable.getSelectedRow();
                if (row != -1) {
                    // luam valoarea ca si string apoi convertim la double
                    String amountStr = (String) dataModel.getValueAt(row, 2);
                    double amount = Double.parseDouble(amountStr);

                    int fineId = (Integer) dataModel.getValueAt(row, 0);
                    boolean isPaid = ((String) dataModel.getValueAt(row, 3)).equals("Yes");

                    if (!isPaid) {
                        int result = JOptionPane.showConfirmDialog(null, "Do you want to pay this fine of $" + amount + "?", "Confirm Fine Payment", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            try {
                                finesmanager.payFine(selectedUserId, fineId);
                                reloadFineData(); // Refresh the fines list

                                JOptionPane.showMessageDialog(null, "Fine paid successfully!");
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Error paying fine: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "This fine is already paid.", "Information", JOptionPane.INFORMATION_MESSAGE);

                    }
                }
            }
        }
    };


    private void reloadFineData() {
        try {
            List<Fine> fines = finesmanager.getFinesForUser(selectedUserId);
            updateDataTableWithFines(fines);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reloading fine data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void showUsersFinesDialog() {
        if (selectedUserId <= 0) {
            JOptionPane.showMessageDialog(this, "No valid user selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Fine> fines = libraryService.getUserFines(selectedUserId);
        if (fines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selected user has no fines.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        updateDataTableWithFines(fines);
    }


    private void updateDataTableWithFines(List<Fine> fines) {
        String[] columnNames = {"Fine ID", "User ID", "Amount", "Paid", "Description", "Book ID"};
        dataModel.setColumnIdentifiers(columnNames);
        dataModel.setRowCount(0);

        for (Fine fine : fines) {
            dataModel.addRow(new Object[]{
                    fine.getId(),
                    fine.getUserId(),
                    String.format("%.2f", fine.getAmount()),
                    fine.isPaid() ? "Yes" : "No",
                    fine.getDescription(),
                    fine.getBookId()
            });
        }
    }


    private void showSearchByAuthorialog() {

        JDialog dialog = new JDialog(this, "Search Book by Author", true);
        dialog.setLayout(new BorderLayout());

        // camp text pt input
        JTextField searchField = new JTextField(20);

        // button search
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            String searchQuery = searchField.getText();
            if (!searchQuery.isEmpty()) {
                searchAndDisplayBooksByAuthor(searchQuery);
                dialog.dispose(); // Close dialog after search
            } else {
                JOptionPane.showMessageDialog(dialog, "Please enter an author", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Panel to hold the components
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Enter an author:"));
        inputPanel.add(searchField);
        inputPanel.add(searchButton);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showTopThreeBooks() {
        try {
            List<BookStats> topBooks = library.displayTopThreeMostPopularBooks();
            DataTableTopThree(topBooks);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error displaying top books: " + ex.getMessage(), "Display Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void DataTableTopThree(List<BookStats> books) {
        String[] columnNames = {"ID", "Title", "Author", "Loans Count", "Most Recent Loan Date"};
        dataModel.setRowCount(0);
        dataModel.setColumnIdentifiers(columnNames);

        for (BookStats book : books) {
            dataModel.addRow(new Object[]{
                    book.getBook().getId(),
                    book.getBook().getTitle(),
                    book.getBook().getAuthor(),
                    book.getLoanCount(),
                    book.getRecentLoanDate().toString()
            });
        }
    }

    private void showSearchByNameDialog() {
        JDialog dialog = new JDialog(this, "Search Book by Name", true);
        dialog.setLayout(new BorderLayout());

        JTextField searchField = new JTextField(20);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            String searchQuery = searchField.getText();
            if (!searchQuery.isEmpty()) {
                searchAndDisplayBooksByName(searchQuery);
                dialog.dispose(); // inchidem dialogul dupa ce am apasat pe search
            } else {
                JOptionPane.showMessageDialog(dialog, "Please enter a book name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Enter Book Name:"));
        inputPanel.add(searchField);
        inputPanel.add(searchButton);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void searchAndDisplayBooksByName(String bookName) {
        try {
            List<Book> foundBooks = search.findBooksByTitle(bookName);
            if (foundBooks.isEmpty()) {
                throw new Exceptii.BookNotFound("Book not found");
            }
            updateDataTableWithBooks(foundBooks);
        } catch (Exceptii.BookNotFound ex) {
            JOptionPane.showMessageDialog(this, "Error searching for books: " + ex.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchAndDisplayBooksByAuthor(String authorName) {
        try {
            List<Book> foundBooks = search.findBooksByAuthor(authorName);
            if (foundBooks.isEmpty()) {
                throw new Exceptii.BookNotFound("Author not found!");
            }
            updateDataTableWithBooks(foundBooks);
        } catch (Exceptii.BookNotFound ex) {
            JOptionPane.showMessageDialog(this, "Error searching for books by author: " + ex.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDataTableWithBooks(List<Book> books) {
        String[] columnNames = {"Book ID", "Title", "Author", "Quantity"};
        dataModel.setColumnIdentifiers(columnNames);
        dataModel.setRowCount(0);

        for (Book book : books) {
            dataModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getQuantity()
            });
        }
    }

    private void reviewsForABookDialog() {

        JDialog dialog = new JDialog(this, "Display Reviews for Book", true);
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JTextField bookIdField = new JTextField();
        JTextField bookNameField = new JTextField();

        panel.add(new JLabel("Book ID (optional):"));
        panel.add(bookIdField);
        panel.add(new JLabel("Book Name (optional):"));
        panel.add(bookNameField);

        JButton displayButton = new JButton("Display Reviews");
        displayButton.addActionListener(e -> {
            try {
                displayReviewsForBook(bookIdField.getText().trim(), bookNameField.getText().trim());
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(displayButton, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void displayReviewsForBook(String bookId, String bookName) {
        dataModel.setRowCount(0);

        try {
            List<Review> reviews = new ArrayList<>();
            if (!bookId.isEmpty()) {
                int bId = Integer.parseInt(bookId);
                reviews.addAll(library.getReviewsForBook(bId));
            } else if (!bookName.isEmpty()) {
                List<Book> books = search.findBooksByTitle(bookName);
                if (books.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No books found with that name.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                for (Book book : books) {
                    reviews.addAll(library.getReviewsForBook(book.getId()));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter either a book ID or a book name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (reviews.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No reviews found for the specified book(s).", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            updateDataDisplayWithReviews(reviews);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid book ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDataDisplayWithReviews(List<Review> reviews) {
        String[] columnNames = {"Review ID", "Book ID", "Book Title", "User ID", "Review Text"};
        dataModel.setRowCount(0);
        dataModel.setColumnIdentifiers(columnNames);

        for (Review review : reviews) {
            Book book = library.getBook(review.getBookId());
            dataModel.addRow(new Object[]{review.getId(), review.getBookId(), book.getTitle(), review.getUserId(), review.getReviewText()});
        }
    }

    private void showAddReviewDiaolog() {

        if (selectedUserId <= 0) {
            JOptionPane.showMessageDialog(this, "Only logged users can leave book reviews.", "Action not permitted", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Add Review", true);
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JTextField userIdField = new JTextField(String.valueOf(selectedUserId));
        userIdField.setEditable(false);

        JTextField bookIdField = new JTextField();
        JTextArea reviewTextField = new JTextArea(5, 20);
        reviewTextField.setLineWrap(true);
        reviewTextField.setWrapStyleWord(true);
        JScrollPane reviewTextScrollPane = new JScrollPane(reviewTextField);

        panel.add(new JLabel("User ID:"));
        panel.add(userIdField);
        panel.add(new JLabel("Book ID:"));
        panel.add(bookIdField);
        panel.add(new JLabel("Review Text:"));
        panel.add(reviewTextScrollPane);

        JButton submitButton = new JButton("Submit Review");
        submitButton.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(bookIdField.getText().trim());
                String reviewText = reviewTextField.getText().trim();
                Review review = new Review(bookId, selectedUserId, reviewText);
                library.addReview(review);
                JOptionPane.showMessageDialog(dialog, "Review added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                updateDataDisplay("Reviews");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid book ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding review: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(submitButton, BorderLayout.SOUTH);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }


    private void displayBooks() {
        List<Book> books = new ArrayList<>(library.getAllBooks());
        String[] columnNames = {"ID", "Title", "Author", "Quantity"};
        setTableModel(columnNames);

        for (Book book : books) {
            dataModel.addRow(new Object[]{book.getId(), book.getTitle(), book.getAuthor(), book.getQuantity()});
        }
    }

    private void displayLoans() {
        List<Loan> loans = new ArrayList<>(library.getAllLoans());
        String[] columnNames = {"ID", "BookId", "UserId", "StartDate", "Deadline", "Status"};
        setTableModel(columnNames);

        for (Loan loan : loans) {
            dataModel.addRow(new Object[]{loan.getId(), loan.getBookId(), loan.getUserId(), loan.getStartDate(), loan.getDeadline(), loan.getStatus()});
        }
    }

    private void displayUsers() {

        List<User> users = new ArrayList<>(library.getAllUsers());
        String[] columnNames = {"ID", "Name", "Surname", "Role"};
        setTableModel(columnNames);

        for (User user : users) {
            dataModel.addRow(new Object[]{user.getId(), user.getName(), user.getSurname(), user.getRole()});
        }


    }


    private void displayReviews() {

        List<Review> reviews = new ArrayList<>(library.getAllReviews());
        String[] columnNames = {"ID", "Book ID", "User ID", "Review Text"};
        setTableModel(columnNames);

        for (Review review : reviews) {

            dataModel.addRow(new Object[]{review.getId(), review.getBookId(), review.getUserId(), review.getReviewText()});
        }


    }

    private void displayUserLoans() {
        List<Loan> loans = new ArrayList<>(library.getUserLoans(selectedUserId));
        String[] columnNames = {"ID", "BookId", "UserId", "StartDate", "Deadline", "Status"};
        setTableModel(columnNames);

        for (Loan loan : loans) {
            dataModel.addRow(new Object[]{loan.getId(), loan.getBookId(), loan.getUserId(), loan.getStartDate(), loan.getDeadline(), loan.getStatus()});
        }
    }

    private void setTableModel(String[] columnNames) {
        dataModel.setRowCount(0);
        dataModel.setColumnIdentifiers(columnNames);
    }

    private void updateDataDisplay(String category) {
        if ("Books".equals(category)) {
            displayBooks();
        } else if ("Loans".equals(category)) {
            displayLoans();
        } else if ("User Loans".equals(category)) {
            displayUserLoans();
        } else if ("Users".equals(category)) {
            displayUsers();
        } else if ("Reviews".equals(category)) {
            displayReviews();
        }

    }


    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updateDataDisplay("Books");  // by default incarcam in tabelul cu date cartile
        }
    }


    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add Book", true);
        JPanel panel = new JPanel(new GridLayout(5, 2)); // GridLayout with 5 rows and 2 columns

        JTextField userIdField = new JTextField(String.valueOf(selectedUserId));
        userIdField.setEditable(false); // Read-only because the user is already selected

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField quantityField = new JTextField();

        panel.add(new JLabel("User ID:"));
        panel.add(userIdField);
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Author:"));
        panel.add(authorField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);

        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(e -> {
            try {
                String title = titleField.getText();
                String author = authorField.getText();
                int quantity = Integer.parseInt(quantityField.getText());
                libraryService.addBook(selectedUserId, title, author, quantity);
                JOptionPane.showMessageDialog(dialog, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                updateDataDisplay("Books");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(addButton, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this); // Center on screen
        dialog.setVisible(true);
    }

    private void UpdateLoanStatus() {
        JDialog dialog = new JDialog(this, "Update Loan Status", true);
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JTextField userIdField = new JTextField(String.valueOf(selectedUserId));
        userIdField.setEditable(false);

        JTextField loanIdField = new JTextField();

        // Create a dropdown for loan statuses
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"ACTIVE", "RETURNED"});
        statusComboBox.setSelectedIndex(0); // default la active


        panel.add(new JLabel("User ID:"));
        panel.add(userIdField);
        panel.add(new JLabel("Loan ID:"));
        panel.add(loanIdField);
        panel.add(new JLabel("New Status:"));
        panel.add(statusComboBox);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            try {
                int loanId = Integer.parseInt(loanIdField.getText());
                String selectedStatus = (String) statusComboBox.getSelectedItem();
                LoanStatus newStatus = "ACTIVE".equals(selectedStatus) ? LoanStatus.ACTIVE : LoanStatus.RETURNED;
                libraryService.updateLoanStatus(selectedUserId, loanId, newStatus);
                JOptionPane.showMessageDialog(dialog, "Loan status updated to " + newStatus, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                updateDataDisplay("Loans");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating loan: " + ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(updateButton, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

    }

    private void UpdateLoanDeadline() {

        JDialog dialog = new JDialog(this, "Update Loan Deadline", true);
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JTextField userIdField = new JTextField(String.valueOf(selectedUserId));
        userIdField.setEditable(false);

        JTextField loanIdField = new JTextField();
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date()); // set to current date

        panel.add(new JLabel("User ID:"));
        panel.add(userIdField);
        panel.add(new JLabel("Loan ID:"));
        panel.add(loanIdField);
        panel.add(new JLabel("New Deadline:"));
        panel.add(dateSpinner);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            try {
                int loanId = Integer.parseInt(loanIdField.getText());
                Date date = (Date) dateSpinner.getValue();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                LocalDate newDeadline = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                libraryService.updateLoanDeadline(selectedUserId, loanId, newDeadline);
                JOptionPane.showMessageDialog(dialog, "Loan deadline updated successfully!", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                updateDataDisplay("Loans");
                finesmanager.updateAndReportFines();
                libraryService.checkAndNotifyOverdueLoans(); // trimitere notificari catre useri


                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating loan: " + ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(updateButton, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showLoansForCurrentUser() {
        if (selectedUserId > 0) {
            updateDataDisplay("User Loans");
            JOptionPane.showMessageDialog(this, "Displaying loans for User ID: " + selectedUserId);
        } else {
            JOptionPane.showMessageDialog(this, "No user selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReturnBookDialog() {
        JDialog dialog = new JDialog(this, "Return Book", true);
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField bookIdField = new JTextField();
        JTextField userIdField = new JTextField(String.valueOf(selectedUserId));
        userIdField.setEditable(false);

        panel.add(new JLabel("Book ID:"));
        panel.add(bookIdField);
        panel.add(new JLabel("User ID:"));
        panel.add(userIdField);

        JButton returnButton = new JButton("Return");
        returnButton.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(bookIdField.getText());
                libraryService.returnBook(selectedUserId, bookId);
                updateDataDisplay("Loans");
                JOptionPane.showMessageDialog(dialog, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Loan Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(returnButton, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void showAddLoanDialog() {
        JDialog dialog = new JDialog(this, "Add Loan", true);
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField bookIdField = new JTextField();
        JTextField userIdField = new JTextField(String.valueOf(selectedUserId));
        userIdField.setEditable(false);

        panel.add(new JLabel("Book ID:"));
        panel.add(bookIdField);
        panel.add(new JLabel("User ID:"));
        panel.add(userIdField);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(bookIdField.getText());
                libraryService.loanBook(selectedUserId, bookId);
                updateDataDisplay("Loans");

                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Loan Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(addButton, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void loadUserData() {
        List<User> users = new ArrayList<>(library.getAllUsers());
        for (User user : users) {
            // Ensure that ID is an Integer
            model.addRow(new Object[]{Integer.valueOf(user.getId()), user.getName(), user.getSurname(), user.getRole().toString()});
        }

        UserTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && UserTable.getSelectedRow() != -1) {
                int selectedRow = UserTable.getSelectedRow();

                int userId = (Integer) model.getValueAt(selectedRow, 0);  // Properly casting as Integer
                String userName = (String) model.getValueAt(selectedRow, 1);
                String userSurname = (String) model.getValueAt(selectedRow, 2);
                UserRole userRole = UserRole.valueOf((String) model.getValueAt(selectedRow, 3));

                // Confirm selection
                int response = JOptionPane.showConfirmDialog(null, "Do you want to switch to the selected user?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    selectedUserId = (Integer) model.getValueAt(selectedRow, 0);  // Obținere ID din model
                    dataModel.setRowCount(0); // stergem datele vechi


                    User selectedUser = new User(userName, userSurname, userRole);
                    selectedUser.setId(userId);
                    SessionManager.getInstance().setCurrentUser(selectedUser);
                    JOptionPane.showMessageDialog(null, "User switched to: " + selectedUser.getName());
                }
            }
        });
    }


    public static void main(String[] args) throws IOException {
        Library library = new Library(); // cand instantiem library se face si load la date
        SwingUtilities.invokeLater(() -> {
            LibraryGUI frame = new LibraryGUI(library);
            frame.setVisible(true);
        });

        NotificationService notificationService = NotificationService.getInstance();
        LibraryService libraryService = new LibraryService(library, notificationService);
        CSVService csvService = CSVService.getInstance();
        FinesManager finesManager = new FinesManager(library, libraryService);
        Search search = new Search(library);

        finesManager.updateAndReportFines(); //generare fisier fines report
        libraryService.checkAndNotifyOverdueLoans(); // trimitere notificari catre useri


        int userId12 = 1;
        int bookId1 = 1;
        int bookId2 = 2;
        int bookId3 = 3;

        // Afisam starea initiala a imprumuturilor pentru utilizator
        List<Loan> initialLoans = library.getUserLoans(userId12);
        System.out.println("Initial loans for user " + userId12 + ": " + initialLoans);

        // Incercam sa returnam cartea 1
        try {
            libraryService.returnBook(bookId1, userId12);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Incercam sa returnam cartea 2
        try {
            libraryService.returnBook(bookId2, userId12);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Incercam sa returnam cartea 3
        try {
            libraryService.returnBook(bookId3, userId12);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Afisam starea imprumuturilor dupa returnare
        List<Loan> updatedLoans = library.getUserLoans(userId12);
        System.out.println("Updated loans for user " + userId12 + ": " + updatedLoans);


    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        textField1 = new JTextField();
        panel1.add(textField1);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}

