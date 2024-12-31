package io.github.aloussase.alexandria.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Book {
    private String title;
    private List<String> authors;
    private String extension;
    private String downloadUrl;
    private String imageUrl;
    private String size;

    public Book(String title) {
        this.title = title;
    }
}
