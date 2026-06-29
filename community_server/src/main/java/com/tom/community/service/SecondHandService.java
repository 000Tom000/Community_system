package com.tom.community.service;

import com.tom.community.model.PageResult;
import com.tom.community.model.SecondHand;

public interface SecondHandService {

    SecondHand create(Long userId, String category, String title, String description,
                      Double price, Double originalPrice, String condition,
                      Boolean isNegotiable, String location, String contact, String imageUrl);

    SecondHand getById(Long id);

    PageResult<SecondHand> list(String category, String keyword, String sort,
                                 Integer page, Integer size);

    void update(Long userId, Long id, String title, String description, String category,
                Double price, Double originalPrice, String condition,
                Boolean isNegotiable, String location, String contact, String imageUrl);

    void delete(Long userId, Long id);

    void markSold(Long userId, Long id);
}
