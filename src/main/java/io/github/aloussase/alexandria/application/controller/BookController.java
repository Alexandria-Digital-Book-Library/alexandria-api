package io.github.aloussase.alexandria.application.controller;

import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.services.BookService;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/api/books")
@RestController
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @RateLimiter(name = "books-backend", fallbackMethod = "tooManyRequests")
    @GetMapping
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam("title") String title
    ) {
        return ResponseEntity.ok(bookService.search(title));
    }

    private ResponseEntity<List<Book>> tooManyRequests(String title, RequestNotPermitted ex) {
        log.warn("too many requests for {}", title);
        return ResponseEntity.status(429).build();
    }
}
