package io.github.aloussase.alexandria.application.controller;

import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.services.BookService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/books")
@RestController
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam("title") String title
    ) {
        return ResponseEntity.ok(bookService.search(title));
    }

}
