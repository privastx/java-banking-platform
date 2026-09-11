package com.example.bankingplatform.support;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketDto {
    private Long id;
    private Long customerId;
    private String subject;
    private String description;
    private SupportCategory category;
    private SupportPriority priority;
    private SupportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
