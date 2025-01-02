package io.github.aloussase.alexandria;

import io.github.aloussase.alexandria.application.di.BooksModule;
import io.github.aloussase.alexandria.domain.models.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BooksModule.class})
@SpringBootTest(classes = AlexandriaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AlexandriaApplicationTests {

    @LocalServerPort
    private int port;

    private final TestRestTemplate template = new TestRestTemplate();

    private final ParameterizedTypeReference<List<Book>> booksTypeReference =
            new ParameterizedTypeReference<>() {
            };


    private String createSearchUrlFor(String title) {
        return "http://localhost:" + port + "/api/books?title=" + title;
    }

    @DirtiesContext
    @Test
    void testSearchingForBookProvidingEmptyTitleReturnsBadRequest() {
        final var uri = createSearchUrlFor("");

        final var response = template.exchange(uri, HttpMethod.GET, null, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @DirtiesContext
    @Test
    void testSearchingBookWithValidTitleReturnsListOfBooks() {
        final var uri = createSearchUrlFor("lions");

        final var response = template.exchange(uri, HttpMethod.GET, null, booksTypeReference);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getBody()).isNotEmpty();
    }

    @DirtiesContext
    @Test
    void testSearchingForBookTwiceMakesSecondRequestFaster() {
        final var uri = createSearchUrlFor("lions");

        final var start0 = System.currentTimeMillis();
        template.exchange(uri, HttpMethod.GET, null, booksTypeReference);
        final var end0 = System.currentTimeMillis();
        final var elapsed0 = end0 - start0;

        final var start1 = System.currentTimeMillis();
        template.exchange(uri, HttpMethod.GET, null, booksTypeReference);
        final var end1 = System.currentTimeMillis();
        final var elapsed1 = end1 - start1;

        assertThat(elapsed1).isLessThan(elapsed0);
    }

}
