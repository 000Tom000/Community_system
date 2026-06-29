package com.tom.community.service.impl;

import com.tom.community.common.BusinessException;
import com.tom.community.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
            你是校园助手，专门为大学生提供学习、选课、考研、实习就业、竞赛科研等方面的帮助。
            回答要求：
            - 简洁实用，条理清晰
            - 结合中国大学实际情况
            - 如果问题超出校园范围，礼貌说明并引导用户去问答中心提问
            - 语气友好亲切，像学长学姐一样
            用中文回答。""";

    public AiServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String chat(List<Map<String, String>> messages) {
        // 构建 OpenAI 兼容的消息列表
        List<Map<String, String>> msgList = new ArrayList<>();
        Map<String, String> sys = new HashMap<>();
        sys.put("role", "system");
        sys.put("content", SYSTEM_PROMPT);
        msgList.add(sys);

        // 只保留最近 10 轮对话，避免 token 超限
        int maxHistory = 20; // 10 user + 10 assistant
        int start = Math.max(0, messages.size() - maxHistory);
        msgList.addAll(messages.subList(start, messages.size()));

        // 构建请求体
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", msgList);
        body.put("temperature", 0.7);
        body.put("max_tokens", 1024);
        body.put("stream", false);

        // 发请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = baseUrl + "/v1/chat/completions";

        try {
            log.info("调用 DeepSeek API, model={}, messages={}", model, msgList.size());
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            Map<String, Object> respBody = response.getBody();
            if (respBody == null) {
                throw new BusinessException("AI 服务返回为空");
            }

            // 解析 OpenAI 格式响应
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) respBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new BusinessException("AI 未返回有效回复");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            log.info("AI 回复成功, length={}", content != null ? content.length() : 0);
            return content != null ? content.trim() : "（AI 返回为空）";

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            String errMsg = e.getMessage();
            if (errMsg != null && errMsg.contains("401")) {
                throw new BusinessException("AI API Key 无效，请检查配置");
            }
            if (errMsg != null && errMsg.contains("429")) {
                throw new BusinessException("AI 调用频率过高，请稍后再试");
            }
            throw new BusinessException("AI 服务暂时不可用: " + (errMsg != null ? errMsg : "未知错误"));
        }
    }
}
