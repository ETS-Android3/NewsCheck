package net.sokato.NewsCheck.models;

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

}
