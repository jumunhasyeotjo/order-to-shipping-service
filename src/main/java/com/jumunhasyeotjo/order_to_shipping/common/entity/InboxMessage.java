package com.jumunhasyeotjo.order_to_shipping.common.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inbox_message", uniqueConstraints = {
    @UniqueConstraint(name = "uk_inbox_event_id", columnNames = "eventId")
})
@NoArgsConstructor
public class InboxMessage extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private String eventKey;

    @Column(nullable = false)
    private String eventType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;
    private LocalDateTime processedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InboxStatus status;

    private String errorMessage;

    @Builder
    public InboxMessage(String eventKey, String eventType, String payload){
        this.eventKey =eventKey;
        this.eventType = eventType;
        this.payload = payload;
        this.status = InboxStatus.RECEIVED;
    }
    public void completed(){
        this.status = InboxStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void markProcessed(){
        this.status = InboxStatus.PROCESSING;
    }

    public void markFailed(String errorMessage){
        this.status = InboxStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}