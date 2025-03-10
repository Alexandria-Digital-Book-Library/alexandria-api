package io.github.aloussase.alexandria.application.di;

import io.github.aloussase.alexandria.domain.repository.BookRepository;
import io.github.aloussase.alexandria.infrastructure.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BooksModule {

    @Bean
    public BookRepository provideBookRepository() {
        return new CachingBookRepository(
                new CompositeBookRepository()
                        .with(new AnnasArchiveBooksRepository())
                        .with(new LibgenBooksRepository()));
    }
}
