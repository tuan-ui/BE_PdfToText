package com.shop.Entity.docxText;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.sql.Clob;
import java.time.LocalDateTime;

@Entity
@Table(name = "PDF_To_Texts")
@Getter
@Setter
@NoArgsConstructor
public class DocxText {
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emp_seq")
    @SequenceGenerator(name = "emp_seq", sequenceName = "DOCTEXT_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;
    @Column(name = "FILE_NAME")
    private String fileName;
    @Lob
    @Column(name = "DESCRIPTION")
    @JsonIgnore
    private Clob description;
    @Column(name = "PATH")
    private String path;
    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

}
