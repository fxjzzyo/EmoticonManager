package com.fxjzzyo.emoticonmanager.bean;

import org.litepal.crud.LitePalSupport;

/**
 * Created by fanlulin on 2019-07-08.
 */
public class EmoticonBean extends LitePalSupport {

    private int id;
    private String emoticonImgURI;
    private String emoticonContent;


    public int getId() {
        return id;
    }

    public void setId(int emoticonId) {
        this.id = emoticonId;
    }

    public String getEmoticonImgURI() {
        return emoticonImgURI;
    }

    public void setEmoticonImgURI(String emoticonImgURI) {
        this.emoticonImgURI = emoticonImgURI;
    }

    public String getEmoticonContent() {
        return emoticonContent;
    }

    public void setEmoticonContent(String emoticonContent) {
        this.emoticonContent = emoticonContent;
    }

    @Override
    public String toString() {
        return "EmoticonBean{" +
                "id=" + id +
                ", emoticonImgURI='" + emoticonImgURI + '\'' +
                ", emoticonContent='" + emoticonContent + '\'' +
                '}';
    }
}
