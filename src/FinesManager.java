import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinesManager {
    private Map<Integer, List<Fine>> userFines = new HashMap<>();
    private Library library;
    private LibraryService libraryservice;

    public FinesManager(Library library, LibraryService libraryservice) {
        this.library = library;
        this.libraryservice=libraryservice;
        loadFines();
    }

    private void loadFines() {
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(Config.getFullPath("fines_report.txt")))) {
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("Fine ID")) { // sarim peste antet
                    Fine fine = Fine.fromCSVString(line.split(","));
                    List<Fine> finesList = userFines.computeIfAbsent(fine.getUserId(), k -> new ArrayList<>());
                    finesList.add(fine);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading fines: " + e.getMessage());
        }
    }


    public void addFine(Fine fine) throws IOException {
        List<Fine> fines = userFines.computeIfAbsent(fine.getUserId(), k -> new ArrayList<>());
        for (Fine existingFine : fines) {
            if (existingFine.getBookId() == fine.getBookId()) {
                // Dacă exista deja o amenda pentru aceasta carte care nu a fost platita, nu adauga o noua amenda.
                return;
            }
        }
        fines.add(fine);
        appendFineToFile(fine);
    }

    private void appendFineToFile(Fine fine) throws IOException {
        File file = new File(Config.getFullPath("fines_report.txt"));
        boolean writeHeader = file.length() == 0; // True dacă fișierul este gol

        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                out.println("Fine ID, User ID, Amount, Paid, Description, Book ID");  // Scrie antetul dacă fișierul este gol
            }
            out.println(fine.toCSVString());  // Scrie amenda
        }
    }


    public void payFine(int userId, int fineId) throws Exception {
        List<Fine> fines = userFines.getOrDefault(userId, new ArrayList<>());
        Fine fineToUpdate = fines.stream()
                .filter(f -> f.getId() == fineId)
                .findFirst()
                .orElse(null);

        if (fineToUpdate == null ) {
            return; // Daca amenda nu exista sau este deja plătita iesim.
        } else       if(fineToUpdate.isPaid()){throw  new Exceptii.FineAlreadyPaidException("Fine has been already paid!");}

        try {
            libraryservice.returnBook(fineToUpdate.getBookId(), userId);
        } catch (Exceptii.InvalidLoanOperationException e) {
            System.out.println("Loan might already be returned: " + e.getMessage());
        }


        fineToUpdate.setPaid(true);
        rewriteAllFines();
        library.saveOrUpdateLoans("loans.csv");  // Adaugam aceasta linie pentru a actualiza fisierul loans.csv

    }

    private void rewriteAllFines() throws IOException {
        File file = new File(Config.getFullPath("fines_report.txt"));
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("Fine ID, User ID, Amount, Paid, Description, Book ID");  // Scrie antetul întotdeauna când rescriem tot fișierul
            for (List<Fine> fines : userFines.values()) {
                for (Fine fine : fines) {
                    out.println(fine.toCSVString());
                }
            }
        }
    }




    public List<Fine> getAllFines() {
        List<Fine> allFines = new ArrayList<>();
        for (List<Fine> finesList : userFines.values()) {
            allFines.addAll(finesList);
        }
        return allFines;
    }


    public void updateAndReportFines() throws IOException {

        LocalDate today = LocalDate.now();
        for (Loan loan : library.getAllLoans()) {
            if (loan.getDeadline().isBefore(today) && loan.getStatus() == LoanStatus.ACTIVE) {
                long daysOverdue = ChronoUnit.DAYS.between(loan.getDeadline(), today);
                double fineAmount = daysOverdue * 5; // $5 per day overdue
                Fine fine = new Fine(loan.getUserId(), fineAmount, today,
                        String.format("Late return of '%s' due by %s, %d days overdue.",
                                library.getBook(loan.getBookId()).getTitle(), loan.getDeadline(), daysOverdue),loan.getBookId());
                addFine(fine);
            }
        }
    }

//    public List<Fine> getUnpaidFines(int userId) {
//        return userFines.getOrDefault(userId, new ArrayList<>())
//                .stream()
//                .filter(f -> !f.isPaid())
//                .collect(Collectors.toList());
//    }
    public List<Fine> getFinesForUser(int userId) {
        return userFines.getOrDefault(userId, new ArrayList<>());
    }


    public void modifyFine(int adminUserId, int fineId, FineModificationAction action, Object newValue) throws Exception {
        User admin = library.getUser(adminUserId);
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            throw new Exceptii.UnauthorizedAccessException("Only admins can modify fines.");
        }

        Fine fineToModify = null;
        for (List<Fine> fines : userFines.values()) {
            for (Fine fine : fines) {
                if (fine.getId() == fineId) {
                    fineToModify = fine;
                    break;
                }
            }
            if (fineToModify != null) {
                break;
            }
        }

        if (fineToModify == null) {
            throw new Exceptii.FineExceptionNotFound("Fine not found.");
        }

        switch (action) {
            case UPDATE_AMOUNT:
                if (!(newValue instanceof Number)) {
                    throw new Exceptii.InvalidArgument("New value for amount must be a double number.");
                }
                fineToModify.setAmount(Double.valueOf(newValue.toString())); // convertim orice tip numeric la double
                System.out.println("Fine amount updated to " + newValue);
                AuditService.getInstance("auditing.csv").logAction("Fine " + fineToModify.getId() + " amount updated to " + newValue + " by admin " + admin.getName());
                break;
            case UPDATE_PAID_STATUS:
                if (!(newValue instanceof Boolean)) {
                    throw new Exceptii.InvalidArgument("New value for paid status must be a Boolean.");
                }
                fineToModify.setPaid((Boolean) newValue);
                System.out.println("Fine paid status updated to " + newValue);
                AuditService.getInstance("auditing.csv").logAction("Fine " + fineToModify.getId() + " paid status updated to " + newValue + " by admin " + admin.getName());
                break;
            default:
                throw new Exceptii.InvalidArgument("Invalid action specified.");
        }

        // rescriem toate amenzile pentru a vedea modificarile aduse anterior
        rewriteAllFines();
    }

}
