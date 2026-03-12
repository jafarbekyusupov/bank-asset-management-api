package com.bank.assets.common.enums;

public enum UserStatus {
    PENDING,            // registered but email not yet verified
    PENDING_APPROVAL,   // NOTE: deprecated; email verified, awaiting admin approval
    ACTIVE,             // approved and can log in
    SUSPENDED           // blocked by admin
}
