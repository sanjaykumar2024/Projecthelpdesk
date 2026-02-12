package com.projecthelpdesk.projecthelpdesk.dto;

import java.util.Map;

public class DashboardStats {

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long onHoldTickets;
    private long resolvedTickets;
    private long closedTickets;
    private Map<String, Long> ticketsByPriority;
    private Map<String, Long> ticketsByDepartment;

    public DashboardStats() {
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public long getOnHoldTickets() {
        return onHoldTickets;
    }

    public void setOnHoldTickets(long onHoldTickets) {
        this.onHoldTickets = onHoldTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public Map<String, Long> getTicketsByPriority() {
        return ticketsByPriority;
    }

    public void setTicketsByPriority(Map<String, Long> ticketsByPriority) {
        this.ticketsByPriority = ticketsByPriority;
    }

    public Map<String, Long> getTicketsByDepartment() {
        return ticketsByDepartment;
    }

    public void setTicketsByDepartment(Map<String, Long> ticketsByDepartment) {
        this.ticketsByDepartment = ticketsByDepartment;
    }
}
