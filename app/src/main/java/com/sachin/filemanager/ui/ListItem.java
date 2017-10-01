package com.sachin.filemanager.ui;

public class ListItem {
    private String heading;
    private String content;

    public ListItem(String heading, String content) {
        this.heading = heading;
        this.content = content;
    }

    public String getHeading() {
        return heading;
    }

    public String getContent() {
        return content;
    }
}
