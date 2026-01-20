package com.pw.edu.pl.master.thesis.ai.dto.ai.deepseek;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One entry in the "messages" array
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekMessage {
    private String role;
    private String content;
}
