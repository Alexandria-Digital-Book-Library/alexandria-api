package io.github.aloussase.alexandria.domain.services;

import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository books;

    public BookService(BookRepository books) {
        this.books = books;
    }

    public List<Book> search(String title) {
        if (title.isBlank()) {
            throw new IllegalArgumentException("The book title cannot be blank");
        }

        return books.searchByTitle(title);
    }
}
