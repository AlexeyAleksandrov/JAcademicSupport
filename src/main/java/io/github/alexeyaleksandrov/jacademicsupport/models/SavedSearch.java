package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;

@Entity
@Table(name = "saved_searches")
public class SavedSearch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String searchQuery;

    // Конструкторы, геттеры и сеттеры
    public SavedSearch() {}

    public SavedSearch(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
}