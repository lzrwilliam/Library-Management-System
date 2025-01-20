import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {


        Library library = new Library();
        NotificationService notificationService = NotificationService.getInstance();  // Ensure using the singleton instance
        LibraryService libraryService = new LibraryService(library, notificationService);
        CSVService csvService = CSVService.getInstance();
        FinesManager finesManager = new FinesManager(library,libraryService);
        Search search = new Search(library);



        try {

            int userId12 = 1;
            int bookId1 = 1;
            int bookId2 = 2;
            int bookId3 = 3;

            // Afisam starea initiala a imprumuturilor pentru utilizator
            List<Loan> initialLoans = library.getUserLoans(userId12);
            System.out.println("Initial loans for user " + userId12 + ": " + initialLoans);

            // Incercam sa returnam cartea 1
            try {
                libraryService.returnBook( userId12,bookId1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // Incercam sa returnam cartea 2
            try {
                libraryService.returnBook( userId12,bookId2);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // Incercam sa returnam cartea 3
            try {
                libraryService.returnBook(userId12,bookId3 );
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // Afisam starea imprumuturilor dupa returnare
            List<Loan> updatedLoans = library.getUserLoans(userId12);
            System.out.println("Updated loans for user " + userId12 + ": " + updatedLoans);






//            System.out.println("Overdue notifications sent if any.");
//            libraryService.checkAndNotifyOverdueLoans();  // Check for overdue loans and notify users
//
//            int userId = 1; // Example user ID
//            List<Notification> unreadNotifications = NotificationService.getInstance().getUnreadNotifications(userId);
//            if (unreadNotifications.isEmpty()) {
//                System.out.println("No new notifications for User ID: " + userId);
//            } else {
//               // Notification firstNotification = unreadNotifications.getFirst();
//              //  firstNotification.markAsRead();
//                unreadNotifications = NotificationService.getInstance().getUnreadNotifications(userId);
//
//                System.out.println("Unread Notifications for User ID: " + userId + ":");
//                unreadNotifications.forEach(notification -> System.out.println(notification.getMessage()));
//            }
       //    finesManager.updateAndReportFines();
//libraryService.displayReviews();
        }
//        catch (IOException e) {
//            System.err.println("Error loading data: " + e.getMessage());
//        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}