import { React , useState }  from 'react';
import './MatchTable.css';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { getAuth } from "firebase/auth";

const MatchTable = ( {date, matches} ) => {

  const navigate = useNavigate();
  const auth = getAuth();

  const handleRowClick = (tournamentId) => {
    navigate(`/user/tournament/${tournamentId}/overview`);
  };

  const isPlayerRegistered = (tournament) => {
    // Check if current user is in the users list
    if (tournament.users != null && tournament.users.includes(auth.currentUser.uid)) {
        return 'Registered';
    }

    // Get the current time and tournament start time
    const currentTime = new Date();
    const startTime = new Date(tournament.startDatetime);

    // Calculate the difference in time between now and the tournament start time
    const timeDiff = startTime - currentTime; // Time difference in milliseconds
    const oneDayInMilliseconds = 24 * 60 * 60 * 1000; // 1 day in milliseconds

    // If more than 1 day is remaining before the tournament starts
    if (timeDiff > oneDayInMilliseconds) {
        return 'Open';
    }

    // If less than 1 day is remaining or the tournament has already started
    return 'Closed';
};

  return (
    <div className="match-table-container">
      <h3>{new Date(date).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: '2-digit' })}</h3>
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
        {matches.map((tournament, index) => {
            const registrationStatus = isPlayerRegistered(tournament); // 'Registered', 'Open', or 'Closed'

            return (
                <tr key={tournament.tid} onClick={() => handleRowClick(tournament.tid)}>
                    <td>{index + 1}</td>
                    <td>{tournament.name}</td>
                    <td>
                        {new Date(tournament.startDatetime).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })} —  
                        {' '}
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
        })}
        </tbody>
      </table>
    </div>
  );
};

export default MatchTable;