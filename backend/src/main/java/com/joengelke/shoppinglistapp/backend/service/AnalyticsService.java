package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.repository.ShoppingItemActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final ShoppingItemActivityRepository shoppingItemActivityRepository;

    public AnalyticsService(ShoppingItemActivityRepository shoppingItemActivityRepository) {
        this.shoppingItemActivityRepository = shoppingItemActivityRepository;
    }
}
