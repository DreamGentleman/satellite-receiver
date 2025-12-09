package com.yxh.fangs.bean;

import java.util.ArrayList;
import java.util.List;

public class CacheMessage {
    private List<MessageResponse.MessageItem> data;

    public List<MessageResponse.MessageItem> getData() {
        if (data == null) {
            return new ArrayList<>();
        }
        return data;
    }
}
