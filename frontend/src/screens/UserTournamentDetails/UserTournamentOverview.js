import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import './UserTournamentOverview.css';
import UserDetailsHeader from './UserDetailsHeader';

const UserTournamentOverview = () => {
    const { tournamentId } = useParams();
    const [numberOfPlayers, setNumberOfPlayers] = useState(0);
    const [tournamentData, setTournamentData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchTournamentData = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}`);
                setTournamentData(response.data);
                setNumberOfPlayers(response.data.users ? response.data.users.length : 0);
                setLoading(false);
            } catch (error) {
                setError(error.message);
                setLoading(false);
            }
        };

        fetchTournamentData();
    }, [tournamentId]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    // Tournament format string
    const tournamentFormat = `${tournamentData?.name || "This tournament"} is a ${tournamentData?.capacity || numberOfPlayers}-player single-elimination knockout tournament. Players compete head-to-head in a series of matches, with the winner advancing to the next round, and the loser being eliminated. The tournament follows standard chess rules, with time controls of 90 minutes for the first 40 moves, followed by 30 minutes for the remainder of the game, with an additional 30 seconds per move starting from move one. The ultimate goal is to determine the champion through progressive elimination of participants.`;

    // Calculate prize breakdown based on total prize
    const calculatePrizeBreakdown = (totalPrize) => {
        let total = 0;

        if (typeof totalPrize === 'string') {
            total = parseFloat(totalPrize.replace(/[^0-9.-]+/g, '')); 
        } else if (typeof totalPrize === 'number') {
            total = totalPrize; 
        }

        return [
            { place: "1st", amount: `$${(total * 0.40).toLocaleString()}` },
            { place: "2nd", amount: `$${(total * 0.25).toLocaleString()}` },
            { place: "3rd", amount: `$${(total * 0.15).toLocaleString()}` },
            { place: "4th", amount: `$${(total * 0.10).toLocaleString()}` },
            { place: "5th", amount: `$${(total * 0.05).toLocaleString()}` },
        ];
    };

    // Calculate the breakdown or use default if not provided
    const prizeBreakdown = tournamentData?.prize ? calculatePrizeBreakdown(tournamentData.prize) : [];

    return (
        <div>
            <UserDetailsHeader
                activeTab="overview"
                tournamentTitle={tournamentData?.name || "Tournament Overview"}
                playerCount={numberOfPlayers}
            />

            <div className="tournament-overview">
                <div className="tournament-description">
                    <p>{tournamentData?.description || "No description available for this tournament."}</p>
                </div>
                
                {/* Tournament Format Section */}
                <div className="tournament-details">
                    <div className="detail-row">
                        <strong>Dates:</strong> 
                 
                            {new Date(tournamentData?.startDatetime).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })} —  
                            {' '}
                            {new Date(tournamentData?.endDatetime).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
              
                    </div>
                    <div className="detail-row">
                        <strong>Tournament type:</strong> {tournamentData?.type || "Swiss"}
                    </div>
                    <div className="detail-row">
                        <strong>Total Participants:</strong> {tournamentData?.capacity || "N/A"}
                    </div>
                    <div className="detail-row">
                        <strong>Current Number of Players:</strong> {numberOfPlayers}
                    </div>
                    <div className="detail-row">
                        <strong>Location:</strong> {tournamentData?.location || "Budapest, Hungary"}
                    </div>
                    <div className="detail-row">
                        <strong>Format:</strong> 
                        <p>{tournamentFormat}</p>
                    </div>
                    
                    <div className="detail-row">
                        <strong>Prizes:</strong>
                        <div className="prizes">
                            <div className="prize-total-box">
                                <div className="prize-icon">💰</div>
                                <div className="prize-amount">${tournamentData?.prize || "$0"}</div>
                                <div className="prize-label">Total prize pool</div>
                            </div>
                            <div className="prize-breakdown-box">
                                <table className="prize-table">
                                    <tbody>
                                        {prizeBreakdown.length > 0 ? (
                                            prizeBreakdown.map((prize, index) => (
                                                <tr key={index}>
                                                    <td className="prize-place">{prize.place}</td>
                                                    <td className="prize-amount-text">{prize.amount}</td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan="2">No prize breakdown available</td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div className="organizer-container">
                        <div className="detail-row">
                            <strong>Organizer:</strong> {tournamentData?.organizer || "World Chess Organization"}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserTournamentOverview;
