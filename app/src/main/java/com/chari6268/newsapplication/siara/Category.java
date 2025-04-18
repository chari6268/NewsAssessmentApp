package com.chari6268.newsapplication.siara;

public class Category {
    private int id;
    private String name;
    private boolean selected;
    private int imageResource;

    public Category(int id, String name, boolean selected, int imageResource) {
        this.id = id;
        this.name = name;
        this.selected = selected;
        this.imageResource = imageResource;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getImageResource() {
        return imageResource;
    }
}