package com.sachin.filemanager.adapters;

public class Model {
    private String heading;
    private String content;

    public Model(String heading, String content) {
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
