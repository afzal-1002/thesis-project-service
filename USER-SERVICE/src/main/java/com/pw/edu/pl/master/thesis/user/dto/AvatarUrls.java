package com.pw.edu.pl.master.thesis.user.dto;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class AvatarUrls {
    private String url48x48;
    private String url24x24;
    private String url16x16;
    private String url32x32;
}
