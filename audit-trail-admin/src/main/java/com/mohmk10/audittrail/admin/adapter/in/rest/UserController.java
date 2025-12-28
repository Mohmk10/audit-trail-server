package com.mohmk10.audittrail.admin.adapter.in.rest;

import com.mohmk10.audittrail.admin.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.admin.application.UserService;
import com.mohmk10.audittrail.admin.aspect.Audited;
import com.mohmk10.audittrail.admin.domain.AdminAction;
import com.mohmk10.audittrail.admin.domain.Role;
import com.mohmk10.audittrail.admin.domain.User;
import com.mohmk10.audittrail.admin.domain.UserStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Audited(action = AdminAction.USER_CREATED, resourceType = "User", resourceId = "#result.body.id")
    public ResponseEntity<UserResponse> createUser(
            @PathVariable String tenantId,
            @Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                tenantId,
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.role()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @PathVariable String tenantId,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Role role,
            Pageable pageable) {
        Page<User> users = userService.findByFilters(tenantId, status, role, pageable);
        return ResponseEntity.ok(users.map(UserResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable String tenantId,
            @PathVariable UUID id) {
        return userService.findById(id)
                .filter(user -> user.getTenantId().equals(tenantId))
                .map(user -> ResponseEntity.ok(UserResponse.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Audited(action = AdminAction.USER_UPDATED, resourceType = "User", resourceId = "#id")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id, request.firstName(), request.lastName(), request.role());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/{id}/password")
    @Audited(action = AdminAction.USER_PASSWORD_CHANGED, resourceType = "User", resourceId = "#id")
    public ResponseEntity<Void> changePassword(
            @PathVariable String tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    @Audited(action = AdminAction.USER_UPDATED, resourceType = "User", resourceId = "#id")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable String tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        User user = userService.updateStatus(id, request.status());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/{id}/activate")
    @Audited(action = AdminAction.USER_ACTIVATED, resourceType = "User", resourceId = "#id")
    public ResponseEntity<UserResponse> activateUser(
            @PathVariable String tenantId,
            @PathVariable UUID id) {
        User user = userService.updateStatus(id, UserStatus.ACTIVE);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/{id}/deactivate")
    @Audited(action = AdminAction.USER_DEACTIVATED, resourceType = "User", resourceId = "#id")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable String tenantId,
            @PathVariable UUID id) {
        User user = userService.updateStatus(id, UserStatus.INACTIVE);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/{id}/lock")
    @Audited(action = AdminAction.USER_LOCKED, resourceType = "User", resourceId = "#id")
    public ResponseEntity<UserResponse> lockUser(
            @PathVariable String tenantId,
            @PathVariable UUID id) {
        User user = userService.updateStatus(id, UserStatus.LOCKED);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/{id}/unlock")
    @Audited(action = AdminAction.USER_UNLOCKED, resourceType = "User", resourceId = "#id")
    public ResponseEntity<UserResponse> unlockUser(
            @PathVariable String tenantId,
            @PathVariable UUID id) {
        User user = userService.updateStatus(id, UserStatus.ACTIVE);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/{id}")
    @Audited(action = AdminAction.USER_DELETED, resourceType = "User", resourceId = "#id")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String tenantId,
            @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
