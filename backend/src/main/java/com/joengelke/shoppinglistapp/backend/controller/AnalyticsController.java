package com.joengelke.shoppinglistapp.backend.controller;

import com.joengelke.shoppinglistapp.backend.model.ShoppingItemActivity;
import com.joengelke.shoppinglistapp.backend.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllShoppingItemActivities() {
        List<ShoppingItemActivity> activityList = analyticsService.getAllShoppingItemActivities();
        return ResponseEntity.ok(activityList);
    }

    @GetMapping
    public ResponseEntity<?> getShoppingItemActivities(
            @RequestParam String shoppingListId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        List<ShoppingItemActivity> activityList = analyticsService.getFilteredShoppingItemActivities(shoppingListId, userId, name, from, to);
        return ResponseEntity.ok(activityList);
    }

    @GetMapping("/activity-names")
    public ResponseEntity<?> getActivityNames(
            @RequestParam String shoppingListId
    ) {
        List<String> nameList = analyticsService.getActivityNames(shoppingListId);
        return ResponseEntity.ok(nameList);
    }
}
