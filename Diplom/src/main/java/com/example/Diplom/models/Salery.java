package com.example.Diplom.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "salery")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Salery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIncludeProperties({"id", "firstName", "lastName", "email"}) // Включаем только нужные поля
    private User user;

    @Column(name = "date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @Column(name = "paid")
    private String paid;

    @Column(name = "sum")
    private Double sum;

    @Column(name = "rate")
    private Double rate;
}