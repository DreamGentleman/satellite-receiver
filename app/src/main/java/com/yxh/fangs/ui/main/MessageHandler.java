package com.yxh.fangs.ui.main;

import com.yxh.fangs.bean.Last24HoursBean;

public interface MessageHandler {
    boolean canHandle(String type);

    void handle(Last24HoursBean.RowsBean msg, String level);
}
