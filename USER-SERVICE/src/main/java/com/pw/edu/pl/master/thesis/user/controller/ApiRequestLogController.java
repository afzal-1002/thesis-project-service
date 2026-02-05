package com.pw.edu.pl.master.thesis.user.controller;


import com.pw.edu.pl.master.thesis.user.model.api.ApiRequestLog;
import com.pw.edu.pl.master.thesis.user.service.ApiRequestLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/wut/logs")
public class ApiRequestLogController {

    private final ApiRequestLogService service;

    public ApiRequestLogController(ApiRequestLogService service) {
        this.service = service;
    }

    /* CREATE */
    @PostMapping
    public ApiRequestLog create(@RequestBody ApiRequestLog log) {
        return service.save(log);
    }

    /* READ */
    @GetMapping
    public List<ApiRequestLog> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ApiRequestLog getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/failed")
    public List<ApiRequestLog> getFailedLogs() {
        return service.getFailedLogs();
    }

    @GetMapping("/status/{code}")
    public List<ApiRequestLog> getByStatus(@PathVariable Integer code) {
        return service.getByStatusCode(code);
    }

    @GetMapping("/method/{method}")
    public List<ApiRequestLog> getByMethod(@PathVariable String method) {
        return service.getByMethod(method);
    }

    @GetMapping("/frontend/{app}")
    public List<ApiRequestLog> getByFrontend(@PathVariable String app) {
        return service.getByFrontendApp(app);
    }

    @GetMapping("/search")
    public List<ApiRequestLog> searchByUrl(
            @RequestParam String keyword
    ) {
        return service.searchByUrl(keyword);
    }

    @GetMapping("/range")
    public List<ApiRequestLog> getByTimeRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        return service.getByTimeRange(start, end);
    }

    /* UPDATE */
    @PutMapping("/{id}")
    public ApiRequestLog update(
            @PathVariable Long id,
            @RequestBody ApiRequestLog log
    ) {
        return service.update(id, log);
    }

    /* DELETE */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}