import java.util.List;
import java.util.stream.Collectors;

public class Search {
    private Library library;

    public Search(Library library) {
        this.library = library;
    }

    public List<Book> findBooksByTitle(String title) {
        return library.getAllBooks().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> findBooksByAuthor(String author) {
        return library.getAllBooks().stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }
}
