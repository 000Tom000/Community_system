package com.tom.community.service;

import com.tom.community.model.PageResult;
import com.tom.community.model.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceService {

    /** 上传资源 */
    Resource upload(Long userId, String title, String description, String category,
                    MultipartFile file);

    /** 资源详情 */
    Resource getResource(Long id);

    /** 列表（分页+筛选+排序） */
    PageResult<Resource> listResources(String category, String keyword, String sort,
                                       Integer page, Integer size);

    /** 删除（上传者/管理员） */
    void deleteResource(Long userId, Long resourceId);

    /** 评分 (1-5) */
    void rate(Long userId, Long resourceId, Integer rating);
}
