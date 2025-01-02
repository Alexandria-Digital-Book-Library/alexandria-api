package io.github.aloussase.alexandria.infrastructure.repository;

import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.repository.BookRepository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CachingBookRepository implements BookRepository {
    private final ConcurrentHashMap<String, List<Book>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final BookRepository inner;

    public CachingBookRepository(BookRepository inner) {
        this.inner = inner;
    }

    @Override
    public List<Book> searchByTitle(String title) {
        final var cachedBooks = cache.get(title);
        if (cachedBooks != null) {
            return cachedBooks;
        } else {
            final var books = inner.searchByTitle(title);
            cache.put(title, books);
            executor.schedule(
                    () -> cache.remove(title),
                    5,
                    TimeUnit.MINUTES);
            return books;
        }
    }
}
