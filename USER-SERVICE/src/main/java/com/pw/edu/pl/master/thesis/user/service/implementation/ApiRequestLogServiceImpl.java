package com.pw.edu.pl.master.thesis.user.service.implementation;


import com.pw.edu.pl.master.thesis.user.model.api.ApiRequestLog;
import com.pw.edu.pl.master.thesis.user.repository.ApiRequestLogRepository;
import com.pw.edu.pl.master.thesis.user.service.ApiRequestLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiRequestLogServiceImpl implements ApiRequestLogService {

    private final ApiRequestLogRepository repository;

    public ApiRequestLogServiceImpl(ApiRequestLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApiRequestLog save(ApiRequestLog log) {
        log.setRequestTime(LocalDateTime.now());
        return repository.save(log);
    }

    @Override
    public List<ApiRequestLog> getAll() {
        return repository.findAll();
    }

    @Override
    public ApiRequestLog getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));
    }

    @Override
    public List<ApiRequestLog> getFailedLogs() {
        return repository.findBySuccessFalse();
    }

    @Override
    public List<ApiRequestLog> getByStatusCode(Integer statusCode) {
        return repository.findByStatusCode(statusCode);
    }

    @Override
    public List<ApiRequestLog> getByMethod(String method) {
        return repository.findByMethod(method);
    }

    @Override
    public List<ApiRequestLog> getByTimeRange(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return repository.findByRequestTimeBetween(start, end);
    }

    @Override
    public List<ApiRequestLog> getByFrontendApp(String frontendApp) {
        return repository.findByFrontendApp(frontendApp);
    }

    @Override
    public List<ApiRequestLog> searchByUrl(String keyword) {
        return repository.findByUrlContaining(keyword);
    }

    @Override
    public ApiRequestLog update(Long id, ApiRequestLog updatedLog) {
        ApiRequestLog existing = getById(id);

        existing.setMethod(updatedLog.getMethod());
        existing.setUrl(updatedLog.getUrl());
        existing.setStatusCode(updatedLog.getStatusCode());
        existing.setErrorMessage(updatedLog.getErrorMessage());
        existing.setResponseTimeMs(updatedLog.getResponseTimeMs());
        existing.setUserIdentifier(updatedLog.getUserIdentifier());
        existing.setRequestBody(updatedLog.getRequestBody());
        existing.setFrontendApp(updatedLog.getFrontendApp());
        existing.setSuccess(updatedLog.getSuccess());

        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}