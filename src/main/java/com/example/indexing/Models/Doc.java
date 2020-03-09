package com.example.indexing.Models;

/**
 * Created by Enzo Cotter on 3/6/20.
 */
public class Doc {
    int docId;
    int docLength;
    String title;

    public Doc(int docId, int docLength, String title) {
        this.docId = docId;
        this.docLength = docLength;
        this.title=title;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getDocLength() {
        return docLength;
    }

    public void setDocLength(int docLength) {
        this.docLength = docLength;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
