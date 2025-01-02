package io.github.aloussase.alexandria;

import io.github.aloussase.alexandria.application.controller.BookController;
import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.repository.BookRepository;
import io.github.aloussase.alexandria.domain.services.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class UnitTests {
    @Test
    void testSearchingForABookByItsTitleReturnsBooksContainingThatTitle() {
        // Arrange
        final var bookRepository = mock(BookRepository.class);
        when(bookRepository.searchByTitle("lions")).thenReturn(
                List.of(
                        new Book("The lions kingdom"),
                        new Book("A tale of two lions")
                )
        );

        final var bookService = new BookService(bookRepository);
        final var controller = new BookController(bookService);

        // Act
        final var result = controller.searchBooks("lions");

        // Assert
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).hasSize(2);

        verify(bookRepository).searchByTitle("lions");
    }

    @Test
    void testSearchingABookByTitleProvidingAnEmptyTitleThrowsAnException() {
        // Arrange
        final var bookRepository = mock(BookRepository.class);
        final var bookService = new BookService(bookRepository);
        final var controller = new BookController(bookService);

        // Act
        final Supplier<ResponseEntity<List<Book>>> result = () -> controller.searchBooks("");

        // Assert
        assertThatThrownBy(result::get)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
    }
}
