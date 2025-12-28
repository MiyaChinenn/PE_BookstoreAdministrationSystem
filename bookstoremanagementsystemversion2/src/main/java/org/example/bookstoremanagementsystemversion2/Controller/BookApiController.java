package org.example.bookstoremanagementsystemversion2.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.example.bookstoremanagementsystemversion2.DAO.BookDAO;
import org.example.bookstoremanagementsystemversion2.Model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BookApiController {

    private static final Logger logger = LoggerFactory.getLogger(BookApiController.class);

    @Autowired
    private BookDAO bookDAO;

    // GET /api/books - Get all books
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        logger.info("Received request for all books at /api/books");
        try {
            bookDAO.getAllBooks();
            List<Book> books = bookDAO.getBooksList();
            logger.info("Successfully fetched {} books", books != null ? books.size() : 0);
            return ResponseEntity.ok(books != null ? books : List.of());
        } catch (Exception e) {
            logger.error("Error fetching all books", e);
            return ResponseEntity.ok(List.of());
        }
    }

    // GET /api/books/{id} - Get book by ID
    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        logger.info("Received request for book with id: {}", id);
        try {
            bookDAO.getAllBooks();
            List<Book> books = bookDAO.getBooksList();
            
            if (books != null) {
                Book book = books.stream()
                    .filter(b -> id.equals(b.getBookId()))
                    .findFirst()
                    .orElse(null);
                
                if (book != null) {
                    logger.info("Found book with id: {}", id);
                    return ResponseEntity.ok(book);
                }
            }
            
            logger.warn("Book with id: {} not found", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching book with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/books/search?query= - Search books
    @GetMapping("/books/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String query) {
        logger.info("Received search request with query: {}", query);
        try {
            bookDAO.getAllBooks();
            List<Book> allBooks = bookDAO.getBooksList();
            
            if (allBooks == null) {
                return ResponseEntity.ok(List.of());
            }
            
            String lowerQuery = query.toLowerCase();
            List<Book> filteredBooks = allBooks.stream()
                .filter(book -> 
                    (book.getName() != null && book.getName().toLowerCase().contains(lowerQuery)) ||
                    (book.getPublisher() != null && book.getPublisher().toLowerCase().contains(lowerQuery)) ||
                    (book.getType() != null && book.getType().toLowerCase().contains(lowerQuery))
                )
                .collect(Collectors.toList());
            
            logger.info("Found {} books for search query: {}", filteredBooks.size(), query);
            return ResponseEntity.ok(filteredBooks);
        } catch (Exception e) {
            logger.error("Error searching books with query: {}", query, e);
            return ResponseEntity.ok(List.of());
        }
    }
    
    // GET /api/books/category/{type} - Get books by category
    @GetMapping("/books/category/{type}")
    public ResponseEntity<List<Book>> getBooksByType(@PathVariable String type) {
        logger.info("Received request for books of type: {}", type);
        try {
            bookDAO.getAllBooks();
            List<Book> allBooks = bookDAO.getBooksList();
            
            if (allBooks == null) {
                return ResponseEntity.ok(List.of());
            }
            
            List<Book> filteredBooks = allBooks.stream()
                .filter(book -> type.equalsIgnoreCase(book.getType()))
                .collect(Collectors.toList());
            
            logger.info("Found {} books for type: {}", filteredBooks.size(), type);
            return ResponseEntity.ok(filteredBooks);
        } catch (Exception e) {
            logger.error("Error fetching books by type: {}", type, e);
            return ResponseEntity.ok(List.of());
        }
    }

    // POST /api/books - Create a new book (for admin use)
    @PostMapping("/books")
    public ResponseEntity<String> createBook(@RequestBody Book book) {
        logger.info("Received request to create book: {}", book.getName());
        try {
            // Basic validation
            if (book.getName() == null || book.getName().trim().isEmpty()) {
                logger.warn("Cannot create book with empty name");
                return ResponseEntity.badRequest().body("Book name is required");
            }
            
            boolean success = bookDAO.createBook(book);
            
            if (success) {
                logger.info("Successfully created book: {}", book.getName());
                return ResponseEntity.status(HttpStatus.CREATED).body("Book created successfully");
            } else {
                logger.warn("Failed to create book: {}", book.getName());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create book");
            }
        } catch (Exception e) {
            logger.error("Error creating book: {}", book.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating book: " + e.getMessage());
        }
    }
}