package io.github.aloussase.alexandria.infrastructure.repository;

import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.repository.BookRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompositeBookRepository implements BookRepository {
    private final List<BookRepository> components = new ArrayList<>();

    @Override
    public List<Book> searchByTitle(String title) {
        return components
                .stream()
                .map(c -> c.searchByTitle(title))
                .parallel()
                .flatMap(Collection::stream)
                .toList();
    }

    public CompositeBookRepository with(BookRepository component) {
        components.add(component);
        return this;
    }
}
