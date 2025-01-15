package io.github.aloussase.alexandria.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private String title;
    private List<String> authors;
    private String extension;
    private String downloadUrl;
    private String imageUrl;
    private String size;
    private boolean shouldOpenBrowser = false;

    public Book(String title) {
        this.title = title;
    }
}
