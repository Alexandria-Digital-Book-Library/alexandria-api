package io.github.aloussase.alexandria.infrastructure.repository;

import io.github.aloussase.alexandria.domain.models.Book;
import io.github.aloussase.alexandria.domain.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class AnnasArchiveBooksRepository implements BookRepository {

    private final static String BASE_URL = "https://annas-archive.org";

    private final static Pattern EXTENSION_PATTERN = Pattern.compile("\\.(epub|pdf|azw3|mobi)");
    private final static Pattern SIZE_PATTERN = Pattern.compile("(\\d+\\.\\d+MB)");

    @Override
    public List<Book> searchByTitle(String title) {
        final var searchUrl = BASE_URL + "/search?q=" + title;
        try {
            final var doc = Jsoup.connect(searchUrl).get();
            final var rows = doc.select("#aarecord-list > div");
            final var books = new ArrayList<Book>();

            for (final var row : rows) {
                final var downloadsDocUrl = BASE_URL + row.select("a").attr("href");
                final var downloadsDoc = Jsoup.connect(downloadsDocUrl).get();
                final var downloadsPanel = downloadsDoc.selectFirst("#md5-panel-downloads > div:nth-child(2)");
                if (downloadsPanel == null)
                    continue;

                final var downloadUrl = downloadsPanel.select("a")
                        .stream()
                        .skip(2)
                        .findFirst()
                        .map(el -> el.attr("href"))
                        .map(path -> BASE_URL + path)
                        .orElse(null);
                if (downloadUrl == null)
                    continue;

                final var imageUrl = row.select("img").attr("src");

                final var metadata = row.select("a > div:nth-child(2)");
                final var bookTitle = metadata.select("h3").text();
                final var authors = metadata.select("div:nth-child(4)").text().split(", ");

                final var extensionAndSize = metadata.select("div:nth-child(1)").text();

                final var extensionMatcher = EXTENSION_PATTERN.matcher(extensionAndSize);
                if (!extensionMatcher.find())
                    continue;

                final var extension = extensionMatcher.group(1);

                final var sizeMatcher = SIZE_PATTERN.matcher(extensionAndSize);
                if (!sizeMatcher.find())
                    continue;

                final var size = sizeMatcher.group(1);

                final var book = new Book(bookTitle, Arrays.asList(authors), extension, downloadUrl, imageUrl, size);

                books.add(book);
            }

            return books;
        } catch (IOException e) {
            log.warn("Failed to connect to annas archive: {}", e.getMessage());
            return List.of();
        }
    }
}
