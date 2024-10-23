package com.app.tournament.events;

import org.springframework.context.ApplicationEvent;

public class TournamentClosedEvent extends ApplicationEvent {

    private final String tournamentId;

    public TournamentClosedEvent(Object source, String tournamentId) {
        super(source);
        this.tournamentId = tournamentId;
    }

    public String getTournamentId() {
        return tournamentId;
    }
}
