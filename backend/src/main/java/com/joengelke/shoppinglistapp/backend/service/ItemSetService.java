package com.joengelke.shoppinglistapp.backend.service;

import com.joengelke.shoppinglistapp.backend.dto.FileResourceDTO;
import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import com.joengelke.shoppinglistapp.backend.model.ItemSetItem;
import com.joengelke.shoppinglistapp.backend.repository.ItemSetRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ItemSetService {

    private final ItemSetRepository itemSetRepository;
    private final GridFsTemplate gridFsTemplate;

    public ItemSetService(ItemSetRepository itemSetRepository, GridFsTemplate gridFsTemplate) {
        this.itemSetRepository = itemSetRepository;
        this.gridFsTemplate = gridFsTemplate;
    }

    public List<ItemSet> getAllItemSetsByIds(List<String> itemSetIds) {
        return itemSetRepository.findAllById(itemSetIds);
    }

    public ItemSet createItemSet(ItemSet itemSet) {
        return itemSetRepository.save(itemSet);
    }

    public ItemSet updateItemSet(ItemSet newItemSet) {
        ItemSet itemSet = itemSetRepository.findById(newItemSet.getId())
                .orElseThrow(() -> new NoSuchElementException("Item set not found"));

        itemSet.setName(newItemSet.getName());
        itemSet.setItemList(newItemSet.getItemList());
        itemSet.setReceiptFileId(newItemSet.getReceiptFileId());
        return itemSetRepository.save(itemSet);
    }

    public List<ItemSetItem> getItemSetItemsById(String itemSetId) {
        ItemSet itemSet = itemSetRepository.findById(itemSetId)
                .orElseThrow(() -> new NoSuchElementException("Item set not found"));
        return itemSet.getItemList();
    }

    public String saveReceiptFile(MultipartFile receiptFile) {
        String receiptFileId = "";
        if(receiptFile != null && !receiptFile.isEmpty()) {
            try{
                ObjectId fileId = gridFsTemplate.store(
                        receiptFile.getInputStream(),
                        receiptFile.getOriginalFilename(),
                        receiptFile.getContentType()
                );
                receiptFileId = fileId.toString();
            } catch (IOException e) {
                throw new RuntimeException("Failed to store receipt file", e);
            }
        }
        return receiptFileId;
    }

    public void deleteReceiptFile(String fileId) {
        try {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(fileId)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file with id: " + fileId, e);
        }
    }

    public FileResourceDTO getReceiptFileResource(String itemSetId) throws IOException {
        ItemSet itemSet = itemSetRepository.findById(itemSetId)
                .orElseThrow(() -> new RuntimeException("ItemSet not found"));

        String fileIdStr = itemSet.getReceiptFileId();
        if (fileIdStr == null) {
            throw new RuntimeException("No receipt file for this ItemSet");
        }

        ObjectId fileId = new ObjectId(fileIdStr);
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));

        if (gridFSFile == null) {
            throw new RuntimeException("File not found");
        }

        GridFsResource resource = gridFsTemplate.getResource(gridFSFile);

        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (gridFSFile.getMetadata() != null && gridFSFile.getMetadata().getString("_contentType") != null) {
            contentType = gridFSFile.getMetadata().getString("_contentType");
        }

        return new FileResourceDTO(
                new InputStreamResource(resource.getInputStream()),
                contentType,
                gridFSFile.getFilename()
        );
    }

    public void deleteItemSetById(String itemSetId) {
        ItemSet itemSet = itemSetRepository.findById(itemSetId)
                .orElseThrow(() -> new NoSuchElementException("Item set not found"));
        if (itemSet.getReceiptFileId() != null) {
            deleteReceiptFile(itemSet.getReceiptFileId());
        }
        itemSetRepository.deleteById(itemSetId);
    }
}
