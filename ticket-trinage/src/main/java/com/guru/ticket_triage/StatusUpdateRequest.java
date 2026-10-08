package com.guru.ticket_triage;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull TicketStatus status) {
}