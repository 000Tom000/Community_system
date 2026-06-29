package com.tom.community.service;

import java.util.List;
import java.util.Map;

public interface AiService {

    /**
     * 发送对话消息，返回 AI 回复
     * @param messages 历史消息列表，每条 {role: "user"|"assistant", content: "..."}
     * @return AI 的回复内容
     */
    String chat(List<Map<String, String>> messages);
}
