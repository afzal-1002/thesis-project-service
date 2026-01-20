package com.pw.edu.pl.master.thesis.issues.dto.issue.attachment;

public record DownloadedFile(
        byte[] bytes,
        String contentType,
        String filename,
        long contentLength
) {}
