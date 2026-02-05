package com.pw.edu.pl.master.thesis.user.repository;

import com.pw.edu.pl.master.thesis.user.model.api.ApiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ApiRequestLogRepository extends JpaRepository<ApiRequestLog, Long> {
    List<ApiRequestLog> findBySuccessFalse();

    List<ApiRequestLog> findByStatusCode(Integer statusCode);

    List<ApiRequestLog> findByMethod(String method);

    List<ApiRequestLog> findByRequestTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<ApiRequestLog> findByFrontendApp(String frontendApp);

    List<ApiRequestLog> findByUrlContaining(String keyword);
}
