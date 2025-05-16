package com.example.Diplom.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "invoice")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @JsonIgnore // Исключаем рекурсивную сериализацию компании
    private Company company;

    @Column(name = "date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @Column(name = "file_name")
    private String fileName;

    @Lob
    @Column(name = "pdf_file", columnDefinition = "LONGBLOB")
    @JsonIgnore // Исключаем большие бинарные данные из сериализации
    private byte[] pdfFile;

    @Column(name = "file_type")
    private String fileType;
}