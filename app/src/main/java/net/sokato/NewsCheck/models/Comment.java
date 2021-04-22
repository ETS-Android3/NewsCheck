package net.sokato.NewsCheck.models;

import com.google.firebase.firestore.CollectionReference;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Comment {

    @SerializedName("authorID")
    @Expose
    private String authorID;

    @SerializedName("authorName")
    @Expose
    private String author;

    @SerializedName("authorStatus")
    @Expose
    private String AuthorStatus;

    @SerializedName("commentText")
    @Expose
    private String commentText;

    @SerializedName("date")
    @Expose
    private String date;

    @SerializedName("order")
    @Expose
    private int order;

    @SerializedName("dbPath")
    @Expose
    private CollectionReference parent;

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorStatus() {
        return AuthorStatus;
    }

    public void setAuthorStatus(String authorStatus) {
        AuthorStatus = authorStatus;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public CollectionReference getParent() {
        return parent;
    }

    public void setParent(CollectionReference parent) {
        this.parent = parent;
    }
}
