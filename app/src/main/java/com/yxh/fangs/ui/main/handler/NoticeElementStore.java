package com.yxh.fangs.ui.main.handler;

import android.text.TextUtils;

import com.yxh.fangs.ui.main.MapController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class NoticeElementStore {

    private final MapController map;
    private final Map<String, List<Long>> elementIdsByMessageId = new HashMap<>();

    NoticeElementStore(MapController map) {
        this.map = map;
    }

    boolean contains(String msgId) {
        return !TextUtils.isEmpty(msgId) && elementIdsByMessageId.containsKey(msgId);
    }

    void put(String msgId, List<Long> ids) {
        if (TextUtils.isEmpty(msgId) || ids == null || ids.isEmpty()) {
            return;
        }
        elementIdsByMessageId.put(msgId, ids);
    }

    void sync(Set<String> currentMessageIds) {
        if (currentMessageIds == null) {
            currentMessageIds = Collections.emptySet();
        }

        Iterator<Map.Entry<String, List<Long>>> iterator = elementIdsByMessageId.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Long>> entry = iterator.next();
            if (!currentMessageIds.contains(entry.getKey())) {
                removeElements(entry.getValue());
                iterator.remove();
            }
        }
    }

    void clearAll() {
        for (List<Long> ids : elementIdsByMessageId.values()) {
            removeElements(ids);
        }
        elementIdsByMessageId.clear();
    }

    void remove(String msgId) {
        if (TextUtils.isEmpty(msgId)) {
            return;
        }

        List<Long> ids = elementIdsByMessageId.remove(msgId);
        removeElements(ids);
    }

    private void removeElements(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long id : ids) {
            if (id != null) {
                map.removeElement(id);
            }
        }
    }
}
