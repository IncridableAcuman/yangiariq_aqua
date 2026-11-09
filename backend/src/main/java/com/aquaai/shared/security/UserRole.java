package com.aquaai.shared.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum UserRole {
    SUPER_ADMIN,
    ADMIN,
    OPERATOR,
    FARMER;

    public String getAuthority() {
        return "ROLE_" + name();
    }

    public Set<String> getDefaultPermissions() {
        return switch (this) {
            case SUPER_ADMIN -> Arrays.stream(PlatformPermission.values())
                    .map(PlatformPermission::name)
                    .collect(Collectors.toSet());
            case ADMIN -> Arrays.stream(PlatformPermission.values())
                    .filter(p -> p != PlatformPermission.ROLE_DELETE && p != PlatformPermission.SYSTEM_SETTINGS_MANAGE)
                    .map(PlatformPermission::name)
                    .collect(Collectors.toSet());
            case OPERATOR -> Set.of(
                    PlatformPermission.USER_VIEW.name(),
                    PlatformPermission.FARM_VIEW.name(),
                    PlatformPermission.FIELD_VIEW.name(),
                    PlatformPermission.SENSOR_VIEW.name(),
                    PlatformPermission.SENSOR_MANAGE.name(),
                    PlatformPermission.WATER_ALLOCATION_VIEW.name(),
                    PlatformPermission.WATER_ALLOCATION_MANAGE.name(),
                    PlatformPermission.IRRIGATION_VIEW.name(),
                    PlatformPermission.IRRIGATION_MANAGE.name(),
                    PlatformPermission.REPORT_VIEW.name(),
                    PlatformPermission.REPORT_EXPORT.name(),
                    PlatformPermission.PILOT_VIEW.name(),
                    PlatformPermission.PILOT_MANAGE.name(),
                    PlatformPermission.INCIDENT_VIEW.name(),
                    PlatformPermission.INCIDENT_MANAGE.name()
            );
            case FARMER -> Set.of(
                    PlatformPermission.FARM_VIEW.name(),
                    PlatformPermission.FIELD_VIEW.name(),
                    PlatformPermission.FIELD_MANAGE.name(),
                    PlatformPermission.SENSOR_VIEW.name(),
                    PlatformPermission.IRRIGATION_VIEW.name(),
                    PlatformPermission.IRRIGATION_MANAGE.name(),
                    PlatformPermission.REPORT_VIEW.name(),
                    PlatformPermission.PILOT_VIEW.name(),
                    PlatformPermission.INCIDENT_VIEW.name()
            );
        };
    }
}
