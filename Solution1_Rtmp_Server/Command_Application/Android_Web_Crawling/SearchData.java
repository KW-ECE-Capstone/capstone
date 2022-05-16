package com.example.crawling_sample;

//URL로 부터 가지고 온 데이터를 객체화 시켜서 리사이클러뷰에 넣어주기 위한 클래스입니다.

public class SearchData {
    String videoId;
    String title;
    String ImageUrl;
    String publishedAt;

    public SearchData(String videoId, String title, String ImageUrl,
                      String publishedAt) {
        super();
        this.videoId = videoId;
        this.title = title;
        this.ImageUrl = ImageUrl;
        this.publishedAt = publishedAt;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.ImageUrl = imageUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
}


