package com.noffice.service;

import com.noffice.dto.DnDDTO;
import com.noffice.dto.FormResponseDTO;
import com.noffice.dto.FormSchemaSearchDTO;
import com.noffice.entity.FormData;
import com.noffice.entity.FormSchema;
import com.noffice.entity.User;
import com.noffice.repository.FormDataRepository;
import com.noffice.repository.FormSchemaRepository;
import com.noffice.ultils.Constants;
import com.noffice.ultils.FormParser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DnDService {
    @Autowired
    private FormSchemaRepository formSchemaRepository;
    @Autowired
    private FormDataRepository formDataRepository;
    public String publishSchema(DnDDTO DnDDTO, User userDetails){
        try {
            FormSchema formSchema = new FormSchema();
            formSchema.setFormCode(DnDDTO.getId());
            if (DnDDTO.getContent().has("title")) {
                formSchema.setFormName(DnDDTO.getContent().get("title").asText());
            }
            formSchema.setFormContent(DnDDTO.getContent().toString());

            formSchema.setPartnerId(userDetails.getPartnerId());
            formSchema.setIsActive(true);
            formSchema.setIsDeleted(false);
            formSchema.setCreateAt(LocalDateTime.now());
            formSchema.setCreateBy(userDetails.getId());
            formSchema.setUpdateAt(LocalDateTime.now());
            formSchema.setUpdateBy(userDetails.getId());

            formSchemaRepository.save(formSchema);
            return DnDDTO.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FormSchema getContent(String id){
        try {
            return formSchemaRepository.getFormSchema(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String saveContent(DnDDTO DnDDTO, User userDetails){
        try {
            FormData formData = new FormData();
            formData.setFormCode(DnDDTO.getId());
            formData.setFormContent(String.valueOf(DnDDTO.getContent()));
            formData.setPartnerId(userDetails.getPartnerId());
            formData.setIsActive(true);
            formData.setIsDeleted(false);
            formData.setCreateAt(LocalDateTime.now());
            formData.setCreateBy(userDetails.getId());
            formData.setUpdateAt(LocalDateTime.now());
            formData.setUpdateBy(userDetails.getId());
            formDataRepository.save(formData);
            return DnDDTO.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Page<FormSchema> searchFormSchemas(FormSchemaSearchDTO request, UUID partnerId) {

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        return formSchemaRepository.searchFormSchemas(request.getFormName(), request.getFormCode(),request.getStatus(), partnerId, pageable);
    }

    @Transactional
    public String delete(UUID id, User userDetails) {
        String message = "";
        FormSchema role = formSchemaRepository.findByFormSchemaId(id);
        if (role != null) {
            role.setIsDeleted(Constants.IS_DELETED.DELETED);
            role.setDeletedAt(LocalDateTime.now());
            role.setDeletedBy(userDetails.getId());
            formSchemaRepository.save(role);
        } else
            return "error.DndCodeNotExists";
        return message;
    }

    @Transactional
    public String lockUser(UUID id, User userDetails) {
        FormSchema role = formSchemaRepository.findByFormSchemaId(id);
        String message = "";
        if (role != null) {
            role.setIsActive(!role.getIsActive());
            role.setUpdateAt(LocalDateTime.now());
            role.setUpdateBy(userDetails.getId());
        } else
            return "error.DndCodeNotExists";
        return message;
    }

    @Transactional
    public String deleteMuti(List<UUID> ids, User userDetails) {
        String message = "";
        for(UUID id :ids){
            FormSchema role = formSchemaRepository.findByFormSchemaId(id);
            ;
            if (role != null) {
                role.setIsDeleted(Constants.IS_DELETED.DELETED);
                role.setDeletedAt(LocalDateTime.now());
                role.setDeletedBy(userDetails.getId());
                formSchemaRepository.save(role);
            } else
                return "error.DndCodeNotExists";

        }
        return message;
    }

    public List<FormResponseDTO> getResponsesBySchema(String formSchemaId) {
        List<FormData> dataList = formDataRepository.getFormSchema(formSchemaId);
        return dataList.stream()
                .map(data -> FormParser.parseFormContent(data.getFormContent()))
                .collect(Collectors.toList());
    }

    public Map<String, List<String>> summarizeResponses(String formSchemaId) {
        List<FormResponseDTO> responses = getResponsesBySchema(formSchemaId);

        Map<String, List<String>> summary = new LinkedHashMap<>();

        for (FormResponseDTO response : responses) {
            for (Map.Entry<String, String> entry : response.getResponses().entrySet()) {
                summary
                        .computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .add(entry.getValue());
            }
        }

        return summary;
    }

}
