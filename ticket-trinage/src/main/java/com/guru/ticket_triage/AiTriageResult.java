package com.guru.ticket_triage;

public record AiTriageResult(TicketCategory category,
                             TicketPriority priority,
                             String suggestedReply) {
}