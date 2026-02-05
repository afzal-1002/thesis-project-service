package com.pw.edu.pl.master.thesis.user.model.project;

import java.util.List;
import java.util.Map;

public record ProjectPage(int startAt, int maxResults, int total, List<Map<String,Object>> values) {

}
