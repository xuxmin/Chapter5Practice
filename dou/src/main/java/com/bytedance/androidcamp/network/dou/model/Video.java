package com.bytedance.androidcamp.network.dou.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Video {

    @SerializedName("student_id") private String studentId;
    @SerializedName("user_name") private String userName;
    @SerializedName("image_url") private String imageUrl;
    @SerializedName("video_url") private String videoUrl;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public static class GetVideoResponse {
        @SerializedName("feeds")
        private List<Video> feeds;

        @SerializedName("success")
        private Boolean success;

        public List<Video> getVideos(){
            return feeds;
        }

        public void setVideos(List<Video> videos) {
            this.feeds = videos;
        }

        public Boolean getSuccess(){
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }
    }

    public static class PostVideoResponse {
        @SerializedName("result") private List<String> result;
        @SerializedName("url") private String url;
        @SerializedName("success") private Boolean success;

        public void setResult(List<String> result) {
            this.result = result;
        }

        public List<String> getResult(){
            return this.result;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl() {
            return this.url;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public Boolean getSuccess() {
            return this.success;
        }

        @Override public String toString() {
            return "response{" +
                    "result='"  + '\'' +
                    ", url='" + url + '\'' +
                    ", success=" + success.toString() +
                    '}';
        }
    }
}
