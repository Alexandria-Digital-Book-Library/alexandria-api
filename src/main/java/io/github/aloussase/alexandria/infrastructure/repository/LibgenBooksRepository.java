package io.github.aloussase.alexandria.infrastructure.repository;

import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.repository.BookRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class LibgenBooksRepository implements BookRepository {

    private static class BadRowException extends Exception {
        BadRowException() {
            super("Failed to parse book from row");
        }
    }

    private final static Set<String> acceptableBookFormats = Set.of("pdf", "epub", "mobi", "azw3");

    @Override
    public List<Book> searchByTitle(String title) {
        final var searchUrl = createSearchUrl(title);
        try {
            final var doc = Jsoup.connect(searchUrl).get();
            final var rows = doc.select("tr:not(:first-child)");
            final var books = new ArrayList<Book>();
            // TODO: Paralellize this loop.
            // TODO: Cache search results.
            for (var row : rows) {
                try {
                    final var book = parseRow(row);
                    books.add(book);
                } catch (BadRowException ignored) {
                }
            }
            return books;
        } catch (IOException e) {
            return List.of();
        }
    }

    private Book parseRow(Element row) throws BadRowException, IOException {
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
                .select("td:nth-child(10) > a[href]")
                .stream()
                .map(el -> el.attr("href"))
                .findFirst()
                .orElseThrow(BadRowException::new);

        final var mirrorsDoc = Jsoup.connect(mirrorsPageUrl).get();

        final var downloadUrl = mirrorsDoc
                .select("h2 > a[href]")
                .stream()
                .map(el -> el.attr("href").replace("https", "http"))
                .findFirst()
                .orElseThrow(BadRowException::new);

        final var imageUrl = "https://library.lol" + mirrorsDoc
                .select("img[src]")
                .stream()
                .map(el -> el.attr("src"))
                .findFirst()
                .orElseThrow(BadRowException::new);

        return new Book(title, authors, extension, downloadUrl, imageUrl, size);
    }

    private String createSearchUrl(String title) {
        final var sb = new StringBuilder();
        sb.append("https://libgen.is/search.php?req=");
        sb.append(URLEncoder.encode(title, StandardCharsets.UTF_8));
        sb.append("&res=50");
        sb.append("&column=def");
        sb.append("&sort=year");
        sb.append("&sortmode=DESC");
        return sb.toString();
    }
}
