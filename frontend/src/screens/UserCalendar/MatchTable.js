import React from 'react';
import './MatchTable.css';
import { useNavigate } from 'react-router-dom';
import { getAuth } from "firebase/auth";

const MatchTable = ({ ongoingMatches = [], upcomingMatches = [], pastMatches = [] }) => {
  const navigate = useNavigate();
  const auth = getAuth();

  const handleRowClick = (tournamentId) => {
    navigate(`/user/tournament/${tournamentId}/overview`);
  };

  const isPlayerRegistered = (tournament) => {
    if (tournament.users && tournament.users.includes(auth.currentUser.uid)) {
      return 'Registered';
    }

    const currentTime = new Date();
    const startTime = new Date(tournament.startDatetime);
    const timeDiff = startTime - currentTime;
    const oneDayInMilliseconds = 24 * 60 * 60 * 1000;

    return timeDiff > oneDayInMilliseconds ? 'Open' : 'Closed';
  };

  const renderTournamentRows = (matches) => {
    return matches.map((tournament, index) => {
      const registrationStatus = isPlayerRegistered(tournament);
      return (
        <tr key={tournament.tid} onClick={() => handleRowClick(tournament.tid)}>
          <td>{index + 1}</td>
          <td>{tournament.name}</td>
          <td>
            {new Date(tournament.startDatetime).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })} — 
            {new Date(tournament.endDatetime).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
          </td>
          <td>{tournament.location}</td>
          <td>{tournament.capacity}</td>
          <td>{tournament.eloRequirement}</td>
          <td>${tournament.prize}</td>
          <td className={`status-${registrationStatus.toLowerCase()}`}>
            {registrationStatus}
          </td>
        </tr>
      );
    });
  };

  const renderTournamentTable = (title, matches, noDataMessage) => (
    <div>
      <h3>{title}</h3>
      <table className="match-table">
        <thead>
          <tr>
            <th>No</th>
            <th>Name</th>
            <th>Date</th>
            <th>Location</th>
            <th>Slots</th>
            <th>ELO Requirement</th>
            <th>Prize Amount</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {matches.length > 0 ? (
            renderTournamentRows(matches)
          ) : (
            <tr>
              <td colSpan="8" className="no-tournaments-message">
                {noDataMessage}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );

  return (
    <div className="match-table-container">
      {/* Always render each group, showing either the tournaments or a "No tournaments available" message */}
      {renderTournamentTable("Ongoing Tournaments", ongoingMatches, "No ongoing tournaments available")}
      {renderTournamentTable("Upcoming Tournaments", upcomingMatches, "No upcoming tournaments available")}
      {renderTournamentTable("Past Tournaments", pastMatches, "No past tournaments available")}
    </div>
  );
};

export default MatchTable;