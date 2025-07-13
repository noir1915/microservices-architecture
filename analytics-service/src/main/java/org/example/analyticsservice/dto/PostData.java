package org.example.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostData {
    private Long userId;
    private boolean published;
    private String content;
    private String rejectionReason;
}
