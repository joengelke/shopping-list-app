package com.joengelke.shoppinglistapp.backend.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.InputStreamResource;

@Getter
@Setter
public class FileResourceDTO {
    private InputStreamResource inputStreamResource;
    private String contentType;
    private String filename;

    public FileResourceDTO(InputStreamResource resource, String contentType, String filename) {
        this.inputStreamResource = resource;
        this.contentType = contentType;
        this.filename = filename;
    }
}
