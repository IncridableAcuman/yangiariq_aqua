package com.aquaai.shared.security;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum PlatformPermission {

    // User Management
    USER_VIEW("USER_VIEW", "View Users", "Allows viewing user directory and profiles", "USER_MANAGEMENT"),
    USER_CREATE("USER_CREATE", "Create Users", "Allows provisioning new platform user accounts", "USER_MANAGEMENT"),
    USER_UPDATE("USER_UPDATE", "Update Users", "Allows editing profile, organization, and contact details", "USER_MANAGEMENT"),
    USER_ACTIVATE("USER_ACTIVATE", "Activate Users", "Allows activating suspended or pending accounts", "USER_MANAGEMENT"),
    USER_DEACTIVATE("USER_DEACTIVATE", "Deactivate Users", "Allows deactivating or blocking user accounts", "USER_MANAGEMENT"),
    USER_ROLE_ASSIGN("USER_ROLE_ASSIGN", "Assign Roles", "Allows changing role assignments for users", "USER_MANAGEMENT"),
    USER_DELETE("USER_DELETE", "Delete Users", "Allows soft-deleting and terminating user accounts", "USER_MANAGEMENT"),

    // Role Management
    ROLE_VIEW("ROLE_VIEW", "View Roles", "Allows viewing platform roles and assigned permissions", "ROLE_MANAGEMENT"),
    ROLE_CREATE("ROLE_CREATE", "Create Roles", "Allows defining new operational roles", "ROLE_MANAGEMENT"),
    ROLE_UPDATE("ROLE_UPDATE", "Update Roles", "Allows modifying role descriptions and permissions", "ROLE_MANAGEMENT"),
    ROLE_DELETE("ROLE_DELETE", "Delete Roles", "Allows deleting custom non-system roles", "ROLE_MANAGEMENT"),
    PERMISSION_VIEW("PERMISSION_VIEW", "View Permissions", "Allows inspecting the system permission catalog", "ROLE_MANAGEMENT"),
    PERMISSION_ASSIGN("PERMISSION_ASSIGN", "Assign Permissions", "Allows granting or revoking permissions from roles", "ROLE_MANAGEMENT"),

    // Farm & Field Management
    FARM_VIEW("FARM_VIEW", "View Farms", "Allows viewing registered farms and boundaries", "FARM_FIELD_MANAGEMENT"),
    FARM_MANAGE("FARM_MANAGE", "Manage Farms", "Allows registering and updating farm boundaries", "FARM_FIELD_MANAGEMENT"),
    FIELD_VIEW("FIELD_VIEW", "View Fields", "Allows viewing field parcels and soil metadata", "FARM_FIELD_MANAGEMENT"),
    FIELD_MANAGE("FIELD_MANAGE", "Manage Fields", "Allows creating, updating, and surveying parcels", "FARM_FIELD_MANAGEMENT"),

    // Sensor Management
    SENSOR_VIEW("SENSOR_VIEW", "View Sensors", "Allows viewing IoT telemetry and sensor health", "SENSOR_MANAGEMENT"),
    SENSOR_MANAGE("SENSOR_MANAGE", "Manage Sensors", "Allows provisioning, calibrating, and updating sensors", "SENSOR_MANAGEMENT"),

    // Water Management
    WATER_ALLOCATION_VIEW("WATER_ALLOCATION_VIEW", "View Water Allocations", "Allows viewing canal quotas and turnouts", "WATER_MANAGEMENT"),
    WATER_ALLOCATION_MANAGE("WATER_ALLOCATION_MANAGE", "Manage Water Allocations", "Allows configuring regional water dispatch", "WATER_MANAGEMENT"),
    IRRIGATION_VIEW("IRRIGATION_VIEW", "View Irrigation Plans", "Allows inspecting smart irrigation schedules", "WATER_MANAGEMENT"),
    IRRIGATION_MANAGE("IRRIGATION_MANAGE", "Manage Irrigation Plans", "Allows generating and approving irrigation plans", "WATER_MANAGEMENT"),

    // Reporting
    REPORT_VIEW("REPORT_VIEW", "View Reports", "Allows accessing regional and farm performance reports", "REPORTING"),
    REPORT_EXPORT("REPORT_EXPORT", "Export Reports", "Allows exporting water accounting and audit reports", "REPORTING"),

    // Governance
    AUDIT_LOG_VIEW("AUDIT_LOG_VIEW", "View Audit Logs", "Allows inspecting administrative security audit logs", "GOVERNANCE"),
    SYSTEM_SETTINGS_VIEW("SYSTEM_SETTINGS_VIEW", "View System Settings", "Allows inspecting runtime environment configuration", "GOVERNANCE"),
    SYSTEM_SETTINGS_MANAGE("SYSTEM_SETTINGS_MANAGE", "Manage System Settings", "Allows modifying system configuration flags", "GOVERNANCE"),

    // Pilot Management
    PILOT_VIEW("PILOT_VIEW", "View Pilot Programs", "Allows viewing field pilots and ground-truth validation records", "PILOT_MANAGEMENT"),
    PILOT_MANAGE("PILOT_MANAGE", "Manage Pilot Programs", "Allows creating, approving, and activating field pilots", "PILOT_MANAGEMENT"),

    // Accountability
    INCIDENT_VIEW("INCIDENT_VIEW", "View Incidents", "Allows viewing water dispute, theft, and equipment incidents", "ACCOUNTABILITY"),
    INCIDENT_MANAGE("INCIDENT_MANAGE", "Manage Incidents", "Allows investigating incidents and tracking corrective actions", "ACCOUNTABILITY");

    private final String code;
    private final String name;
    private final String description;
    private final String category;

    PlatformPermission(String code, String name, String description, String category) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public static Set<String> allCodes() {
        return Arrays.stream(values())
                .map(PlatformPermission::getCode)
                .collect(Collectors.toSet());
    }
}
