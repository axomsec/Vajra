package model;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RequestModel {

    private List<String> interceptedRequests;


    public RequestModel() {
        this.interceptedRequests = new ArrayList<>();
    }


}
