package com.pw.edu.pl.master.thesis.issues.dto.issue.attachment;

import java.util.Base64;


public record AttachmentDto(
        String filename,
        String contentType,
        long length,
        String base64
) {
    public static AttachmentDto from(DownloadedFile file) {
        return new AttachmentDto(
                file.filename(),
                file.contentType(),
                file.contentLength(),
                Base64.getEncoder().encodeToString(file.bytes())
        );
    }
}
