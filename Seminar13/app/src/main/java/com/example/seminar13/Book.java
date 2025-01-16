package com.example.seminar13;

public class Book {
    private String title;
    private String author;
    private int year;
    private boolean availableOnline;

    public Book() {}

    public Book(String title, String author, int year, boolean availableOnline) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.availableOnline = availableOnline;
    }

    // Getters și setters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public boolean isAvailableOnline() { return availableOnline; }
}

