package com.migo.backend.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "friends")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Friend {
    @Id
    private String id;
    
    @DocumentReference
    private User userA;

    @DocumentReference
    private User userB;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt; 
}
