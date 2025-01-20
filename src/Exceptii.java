import java.io.IOException;

public class Exceptii {
    static abstract class LibraryException extends Exception {
        public LibraryException(String message) {
            super(message);
        }
    }

    static class BookAlreadyLoanedException extends LibraryException {
        public BookAlreadyLoanedException(String message) {
            super(message);
        }
    }

    public static class BookNotAvailableException extends LibraryException {
        public BookNotAvailableException(String message) {
            super(message);
        }
    }

    public static class BookNotFound extends LibraryException {
        public BookNotFound(String message) {
            super(message);
        }
    }

    public static class BookAlreadyExists extends LibraryException {
        public BookAlreadyExists(String message) {
            super(message);
        }
    }


    public static class UserNotFoundException extends LibraryException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedAccessException extends LibraryException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }

    public static class InvalidLoanOperationException extends LibraryException {
        public InvalidLoanOperationException(String message) {
            super(message);
        }
    }

    public static class ReviewNotPermittedException extends LibraryException {
        public ReviewNotPermittedException(String message) {
            super(message);
        }
    }

    public static class FineExceptionNotFound extends LibraryException {
        public FineExceptionNotFound(String message) {
            super(message);
        }
    }

    public static class InvalidArgument extends LibraryException {
        public InvalidArgument(String message) {
            super(message);
        }
    }
    public static class FineNotFoundException extends Exception {
        public FineNotFoundException(String message) {
            super(message);
        }
    }

    public static class NoUnreadNotifications extends Exception {
        public NoUnreadNotifications (String message) {
            super(message);
        }
    }


    public static class FineAlreadyPaidException extends Exception {
        public FineAlreadyPaidException(String message) {
            super(message);
        }
    }

}
