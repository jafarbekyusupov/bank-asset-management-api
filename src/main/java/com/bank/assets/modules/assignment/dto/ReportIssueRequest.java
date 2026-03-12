package com.bank.assets.modules.assignment.dto;
import jakarta.validation.constraints.NotBlank;

public record ReportIssueRequest(@NotBlank String description) {}
