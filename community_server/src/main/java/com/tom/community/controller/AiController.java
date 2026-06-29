package com.tom.community.controller;

import com.tom.community.common.Result;
import com.tom.community.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /** 发送消息，获取 AI 回复 */
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(
            @RequestHeader("X-Token") String token,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");
        if (messages == null || messages.isEmpty()) {
            return Result.error(400, "消息列表不能为空");
        }

        String reply = aiService.chat(messages);
        Map<String, String> result = new HashMap<>();
        result.put("reply", reply);
        return Result.success(result);
    }
}
