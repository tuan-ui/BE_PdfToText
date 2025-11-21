package com.noffice.service;
import com.noffice.entity.Attachs;
import com.noffice.repository.AttachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttachService {

    private final AttachRepository attachRepository;


    @Transactional
    public Attachs create(Attachs attach) {
        return attachRepository.save(attach);
    }

}
