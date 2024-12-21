package model;

import java.util.concurrent.atomic.AtomicInteger;

// Class representing HTTP history entry
public class HttpHistoryEntryModel {
    int id;
    String host;
    String method;
    String url;
    String params;
    boolean edited;
    int statusCode;
    int length;
    String mimeType;
    String extension;
    String title;
    boolean tls;
    String ip;
    String time;
    int listenerPort;

    public HttpHistoryEntryModel(int id, String host, String method, String url, String params, boolean edited,
                                 int statusCode, int length, String mimeType, String extension, String title,
                                 boolean tls, String ip, String time, int listenerPort) {
        this.id = id;
        this.host = host;
        this.method = method;
        this.url = url;
        this.params = params;
        this.edited = edited;
        this.statusCode = statusCode;
        this.length = length;
        this.mimeType = mimeType;
        this.extension = extension;
        this.title = title;
        this.tls = tls;
        this.ip = ip;
        this.time = time;
        this.listenerPort = listenerPort;
    }

    // Method to convert this object into an Object[] for table row
    public Object[] toTableRow() {
        return new Object[]{id, host, method, url, params, edited, statusCode, length, mimeType,
                extension, title, tls, ip, time, listenerPort};
    }

    public void setStatusCode(Integer status) {
        this.statusCode = status;
    }
}
