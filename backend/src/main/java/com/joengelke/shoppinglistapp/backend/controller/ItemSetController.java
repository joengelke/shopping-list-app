package com.joengelke.shoppinglistapp.backend.controller;

import com.joengelke.shoppinglistapp.backend.dto.FileResourceDTO;
import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import com.joengelke.shoppinglistapp.backend.service.ItemSetService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/itemset")
public class ItemSetController {
    private final ItemSetService itemSetService;

    public ItemSetController(ItemSetService itemSetService) {
        this.itemSetService = itemSetService;
    }
}
