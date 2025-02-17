package com.shop.Repository.docText;

import com.shop.Entity.docxText.DocxText;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocxRepository extends CrudRepository<DocxText, Long> {
    @Query("FROM DocxText d order by d.createDate ")
    List<DocxText> findAllDocxText();
    @Query("FROM DocxText d order by d.createDate ")
    Page<DocxText> findAllDocxTextPageable(Pageable pageable);
}
