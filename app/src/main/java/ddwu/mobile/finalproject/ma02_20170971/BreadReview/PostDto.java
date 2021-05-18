package ddwu.mobile.finalproject.ma02_20170971.BreadReview;

import java.io.Serializable;

public class PostDto implements Serializable {
    private long _id;
    private String store;
    private String location;
    private int status; // 1: 재방문 0: 재방문안할예정
    private String contents;
    private String img;

    public PostDto() {}

    public PostDto(long _id, String store, String location, int status, String contents, String img) {
        this._id = _id;
        this.store = store;
        this.location = location;
        this.status = status;
        this.contents = contents;
        this.img = img;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
