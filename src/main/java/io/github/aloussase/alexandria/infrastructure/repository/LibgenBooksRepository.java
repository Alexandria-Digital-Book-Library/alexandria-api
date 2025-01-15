package io.github.aloussase.alexandria.infrastructure.repository;

import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class LibgenBooksRepository implements BookRepository {

    private static class BadRowException extends Exception {
        BadRowException() {
            super("Failed to parse book from row");
        }

        BadRowException(String message) {
            super(message);
        }
    }

    private final static Set<String> acceptableBookFormats = Set.of("pdf", "epub", "mobi", "azw3");

    @Override
    public List<Book> searchByTitle(String title) {
        final var searchUrl = createSearchUrl(title);
        try {
            final var doc = Jsoup.connect(searchUrl).get();
            return doc
                    .select("tr:not(:first-child)")
                    .stream()
                    .map(this::parseRow)
                    .parallel()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (IOException ex) {
            log.error("Failed to get books from Libgen", ex);
            return List.of();
        }
    }

    private Optional<Book> parseRow(Element row) {
        try {
            final var authors = row
                    .select("td:nth-child(2) > a")
                    .textNodes()
                    .stream()
                    .map(TextNode::text)
                    .toList();

            final var title = row
                    .select("td:nth-child(3) > a[title]")
                    .stream()
                    .findFirst()
                    .map(Element::ownText)
                    .orElseThrow(BadRowException::new);

            final var extension = row.select("td:nth-child(9)")
                    .textNodes()
                    .stream()
                    .map(TextNode::text)
                    .filter(acceptableBookFormats::contains)
                    .findFirst()
                    .orElseThrow(BadRowException::new);

            final var size = row.select("td:nth-child(8)")
                    .textNodes()
                    .stream()
                    .map(TextNode::text)
                    .findFirst()
                    .orElseThrow(BadRowException::new);

            final var mirrorsPageUrl = row
                    .select("td:nth-child(11) > a[href]")
                    .stream()
                    .map(el -> el.attr("href"))
                    .findFirst()
                    .orElseThrow(() -> new BadRowException("Failed to parse mirrors page url"));

            final var assetsRoot = "https://libgen.li";
            final var mirrorsDoc = Jsoup.connect(mirrorsPageUrl).get();

            final var downloadUrl = mirrorsDoc
                    .select("a[href]")
                    .stream()
                    .map(el -> assetsRoot + el.attr("href"))
                    .findFirst()
                    .orElseThrow(() -> new BadRowException("Failed to parse book download url"));

            final var imageUrl = assetsRoot + mirrorsDoc
                    .select("img[src]")
                    .stream()
                    .skip(1) // logo
                    .map(el -> el.attr("src"))
                    .findFirst()
                    .orElseThrow(() -> new BadRowException("Failed to parse book image url"));

            final var book = new Book(title, authors, extension, downloadUrl, imageUrl, size);

            return Optional.of(book);
        } catch (BadRowException e) {
            return Optional.empty();
        } catch (IOException e) {
            log.error("Failed to parse book from Anna's Archive: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String createSearchUrl(String title) {
        final var sb = new StringBuilder();
        sb.append("http://libgen.is/search.php?req=");
        sb.append(URLEncoder.encode(title, StandardCharsets.UTF_8));
        sb.append("&res=50");
        sb.append("&column=def");
        sb.append("&sort=year");
        sb.append("&sortmode=DESC");
        return sb.toString();
    }
}
