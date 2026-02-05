package com.pw.edu.pl.master.thesis.user.service;


import com.pw.edu.pl.master.thesis.user.model.api.ApiRequestLog;

import java.time.LocalDateTime;
import java.util.List;

public interface ApiRequestLogService {

    /* CREATE */
    ApiRequestLog save(ApiRequestLog log);

    /* READ */
    List<ApiRequestLog> getAll();

    ApiRequestLog getById(Long id);

    List<ApiRequestLog> getFailedLogs();

    List<ApiRequestLog> getByStatusCode(Integer statusCode);

    List<ApiRequestLog> getByMethod(String method);

    List<ApiRequestLog> getByTimeRange(
            LocalDateTime start,
            LocalDateTime end
    );

    List<ApiRequestLog> getByFrontendApp(String frontendApp);

    List<ApiRequestLog> searchByUrl(String keyword);

    /* UPDATE */
    ApiRequestLog update(Long id, ApiRequestLog updatedLog);

    /* DELETE */
    void delete(Long id);
}