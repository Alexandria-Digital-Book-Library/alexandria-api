package io.github.aloussase.alexandria.domain.repository;

import io.github.aloussase.alexandria.domain.models.Book;

import java.util.List;

public interface BookRepository {

    /**
     * Search books by the given title
     *
     * @param title The title to search by
     * @return A list of books matching the given title
     */
    List<Book> searchByTitle(String title);

}
