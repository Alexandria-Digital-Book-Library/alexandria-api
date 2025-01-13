package io.github.aloussase.alexandria.application.di;

import io.github.aloussase.alexandria.domain.repository.BookRepository;
import io.github.aloussase.alexandria.infrastructure.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class BooksModule {

    @Bean
    public BookRepository provideBookRepository(JdbcTemplate jdbcTemplate) {
        return new StatisticsGatheringBookRepository(
                jdbcTemplate,
                new CachingBookRepository(
                        new CompositeBookRepository()
                                .with(new AnnasArchiveBooksRepository())
                                .with(new LibgenBooksRepository())));
    }
}
