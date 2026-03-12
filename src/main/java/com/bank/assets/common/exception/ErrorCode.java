package com.bank.assets.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // auth
    USER_NOT_FOUND("User not found"),
    USER_ALREADY_EXISTS("User with this email already exists"),
    INVALID_CREDENTIALS("Invalid email or password"),
    ACCOUNT_PENDING("Account not yet verified. Check your email for the OTP code."),
    ACCOUNT_PENDING_APPROVAL("Account is awaiting admin approval."),
    ACCOUNT_SUSPENDED("This account has been suspended"),
    OTP_NOT_FOUND("OTP not found or already expired"),
    OTP_EXPIRED("OTP has expired"),
    OTP_ALREADY_USED("OTP has already been used"),
    OTP_INVALID("Invalid OTP code"),

    // assets
    ASSET_NOT_FOUND("Asset not found"),
    ASSET_SERIAL_EXISTS("An asset with this serial number already exists"),
    INVALID_STATUS_TRANSITION("This status transition is not allowed"),
    ASSET_ALREADY_ASSIGNED("Asset is already assigned"),
    ASSET_NOT_ASSIGNED("Asset is not currently assigned"),
    ASSIGNMENT_REQUEST_NOT_FOUND("Assignment request not found"),
    ASSIGNMENT_REQUEST_ALREADY_PENDING("You already have a pending request for this asset"),
    ASSIGNMENT_REQUEST_NOT_PENDING("This request has already been reviewed"),

    // org
    BRANCH_NOT_FOUND("Branch not found"),
    DEPARTMENT_NOT_FOUND("Department not found"),
    CATEGORY_NOT_FOUND("Asset category not found"),
    CATEGORY_ALREADY_EXISTS("A category with this name already exists"),
    TYPE_NOT_FOUND("Asset type not found"),
    TYPE_ALREADY_EXISTS("An asset type with this name already exists in this category"),

    // general
    FORBIDDEN("You do not have permission to perform this action"),
    VALIDATION_ERROR("Validation failed");

    private final String message;
}
