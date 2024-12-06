package org.example.model;

import com.google.gson.annotations.SerializedName;

public class UpdateMetaData {
    @SerializedName("version")
    private String version;

    @SerializedName("url")
    private String url;

    public UpdateMetaData(String version, String url) {
        this.version = version;
        this.url = url;
    }


    // Getters
    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    // Setters (si los necesitas)
    public void setVersion(String version) {
        this.version = version;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
