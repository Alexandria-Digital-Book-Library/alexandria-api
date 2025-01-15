package io.github.aloussase.alexandria.infrastructure.repository;

import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public class AnnasArchiveBooksRepository implements BookRepository {

    private final static String BASE_URL = "https://annas-archive.org";

    private final static Pattern EXTENSION_PATTERN = Pattern.compile("\\.(epub|pdf|azw3|mobi)");
    private final static Pattern SIZE_PATTERN = Pattern.compile("(\\d+\\.\\d+MB)");

    @Override
    public List<Book> searchByTitle(String title) {
        final var searchUrl = BASE_URL + "/search?q=" + title;

        final Document doc;
        try {
            doc = Jsoup.connect(searchUrl).get();
        } catch (IOException e) {
            log.debug("failed to connect to Anna's Archive: {}", e.getMessage());
            return List.of();
        }

        return doc
                .select("#aarecord-list > div")
                .stream()
                .map(this::parseRow)
                .parallel()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Book> parseRow(Element row) {
        if (!row.hasText()) {
            // NOTE: This happens because Anna's uses JavaScript to dynamically render content.
            // We'll need to use something like HtmlUnit or Selenium in order to render the JavaScript.
            return Optional.empty();
        }

        final var downloadsDocUrl = BASE_URL + row.select("a").attr("href");

        final Document downloadsDoc;
        try {
            downloadsDoc = Jsoup.connect(downloadsDocUrl).get();
        } catch (IOException e) {
            log.debug("Failed to connect to annas archive: {}", e.getMessage());
            return Optional.empty();
        }

        final var downloadsPanel = downloadsDoc.selectFirst("#md5-panel-downloads > div:nth-child(2)");
        if (downloadsPanel == null) {
            log.debug("failed to get download panel for row: {} {}", row.text(), downloadsDocUrl);
            return Optional.empty();
        }

        final var downloadUrl = downloadsPanel.select("a")
                .stream()
                .skip(2)
                .findFirst()
                .map(el -> el.attr("href"))
                .map(path -> BASE_URL + path)
                .orElse(null);
        if (downloadUrl == null) {
            log.debug("failed to get download url for row: {}", row.text());
            return Optional.empty();
        }

        final var imageUrl = row.select("img").attr("src");

        final var metadata = row.select("a > div:nth-child(2)");
        final var bookTitle = metadata.select("h3").text();
        final var authors = metadata.select("div:nth-child(4)").text().split(", ");

        final var extensionAndSize = metadata.select("div:nth-child(1)").text();

        final var extensionMatcher = EXTENSION_PATTERN.matcher(extensionAndSize);
        if (!extensionMatcher.find()) {
            log.debug("failed to get book extension for row: {}", row.text());
            return Optional.empty();
        }

        final var extension = extensionMatcher.group(1);

        final var sizeMatcher = SIZE_PATTERN.matcher(extensionAndSize);
        if (!sizeMatcher.find()) {
            log.debug("failed to get book size for row: {}", row.text());
            return Optional.empty();
        }

        final var size = sizeMatcher.group(1);

        final var book = new Book(bookTitle, Arrays.asList(authors), extension, downloadUrl, imageUrl, size, true);

        return Optional.of(book);
    }
}
