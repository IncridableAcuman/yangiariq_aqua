package com.aquaai.admin.api;

import com.aquaai.admin.application.dto.AdminDtos.SecurityAuditLogResponse;
import com.aquaai.admin.application.service.SecurityAuditService;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Security Audit Logs", description = "Endpoints for inspecting administrative security and access audit logs")
@SecurityRequirement(name = "bearerAuth")
public class SecurityAuditLogController {

    private final SecurityAuditService securityAuditService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW') or hasRole('ADMIN')")
    @Operation(summary = "Search and filter platform security audit records")
    public ResponseEntity<ApiResponse<PageResponse<SecurityAuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String result,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<SecurityAuditLogResponse> logs = securityAuditService.getAuditLogs(action, targetType, result, page, size);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
