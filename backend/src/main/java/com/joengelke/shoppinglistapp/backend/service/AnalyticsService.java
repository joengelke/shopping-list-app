package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.model.ShoppingItemActivity;
import com.joengelke.shoppinglistapp.backend.repository.ShoppingItemActivityRepository;
import com.mongodb.lang.Nullable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AnalyticsService {

    private final ShoppingItemActivityRepository shoppingItemActivityRepository;
    private final MongoTemplate mongoTemplate;

    public AnalyticsService(ShoppingItemActivityRepository shoppingItemActivityRepository, MongoTemplate mongoTemplate) {
        this.shoppingItemActivityRepository = shoppingItemActivityRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void addItemAnalyticsEvent(String shoppingListId, String userId, ShoppingItem shoppingItem, String actionType) {
        ShoppingItemActivity itemActivity = new ShoppingItemActivity(
                shoppingListId,
                userId,
                shoppingItem.getId(),
                shoppingItem.getName(),
                shoppingItem.getAmount(),
                shoppingItem.getUnit(),
                Instant.now(),
                actionType);

        if(shoppingItem.getAmount() == 0) {
            itemActivity.setAmount(1);
        }
        shoppingItemActivityRepository.save(itemActivity);
    }

    public List<ShoppingItemActivity> getAllShoppingItemActivities() {
        return shoppingItemActivityRepository.findAll();
    }

    public List<ShoppingItemActivity> getAllShoppingItemActivitiesByList(String shoppingListId){
        return shoppingItemActivityRepository.findAllByListId(shoppingListId);
    }

    public List<ShoppingItemActivity> getAllShoppingItemActivitiesByUser(String userId){
        return shoppingItemActivityRepository.findAllByUserId(userId);
    }

    public List<ShoppingItemActivity> getFilteredShoppingItemActivities(String shoppingListId, @Nullable String userId, @Nullable String name, @Nullable Instant from, @Nullable Instant to) {
        Criteria criteria = Criteria.where("listId").is(shoppingListId);

        if (userId != null) {
            criteria = criteria.and("userId").is(userId);
        }

        if (name != null) {
            criteria = criteria.and("name").is(name);
        }

        if (from != null && to != null) {
            criteria = criteria.and("timestamp").gte(from).lte(to);
        } else if (from != null) {
            criteria = criteria.and("timestamp").gte(from);
        } else if (to != null) {
            criteria = criteria.and("timestamp").lte(to);
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, ShoppingItemActivity.class);
    }

    public List<String> getActivityNames(String shoppingListId) {
        Query query = new Query(Criteria.where("listId").is(shoppingListId));
        return mongoTemplate.findDistinct(query, "name", ShoppingItemActivity.class, String.class);
    }
}
