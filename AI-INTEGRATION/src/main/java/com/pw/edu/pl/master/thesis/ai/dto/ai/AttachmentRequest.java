package com.pw.edu.pl.master.thesis.ai.dto.ai;

import lombok.Data;

@Data
public class AttachmentRequest {

    private byte[] fileBytes;
    private String filename;
    private String mimeType;
}
