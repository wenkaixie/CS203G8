import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './UserTournamentMatch.css';

const UserTournamentMatchTable = ( {matches} ) => {
    const [participantsData, setParticipantsData] = useState([]);
    const { tournamentId } = useParams();

    const navigate = useNavigate();
    
    useEffect(() => {
        const fetchParticipantsData = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/users`);
                setParticipantsData(response.data);
            } catch (error) {
                console.error('Error fetching participants data:', error);
            }
        };

        fetchParticipantsData();
    }, []);

    const uniqueRounds = [...new Set(matches.map(match => match.tournamentRoundText))].sort();

    const calculatePoints = (authId) => {
        const points = {};
        uniqueRounds.forEach(round => points[round] = null);
        matches.forEach(match => {
            const participant = match.participants.find(p => p.authId === authId);
            const otherParticipant = match.participants.find(p => p.authId !== authId);
            if (participant) {
                if (participant.isWinner && otherParticipant && otherParticipant.isWinner) {
                    points[match.tournamentRoundText] = 0.5;
                } else {
                    points[match.tournamentRoundText] = participant.isWinner ? 1 : 0;
                }
            }
        });
        return points;
    };

    const participantsWithPoints = participantsData.map(participant => {
        const points = calculatePoints(participant.authId);
        const totalPoints = Object.values(points).reduce((acc, curr) => acc + (curr || 0), 0);
        return { ...participant, points, totalPoints };
    }).sort((a, b) => {
        if (b.totalPoints !== a.totalPoints) {
            return b.totalPoints - a.totalPoints; // Sort by totalPoints in descending order
        } else {
            return b.elo - a.elo; // Sort by elo in descending order if totalPoints are the same
        }
    });

    const handleGoToProfile = (authID) => {
        navigate(`/user/profile/${authID}`);
    }


    return (
        <div>
            <table className='matches-table'>
                <thead>
                    <tr>
                        <th>No</th>
                        <th>Player</th>
                        <th>Nationality</th>
                        <th>ELO</th>
                        {uniqueRounds.map(round => (
                            <th key={round}>Round {round}</th>
                        ))}
                        <th>Total Points</th>
                    </tr>
                </thead>
                <tbody>
                    {participantsWithPoints.map((participant, index) => (
                        <tr key={participant.authId}>
                            <td>{index + 1}</td>
                            <td className='participant-name' onClick={() => handleGoToProfile(participant.authId)}>{participant.name || 'N/A'}</td>
                            <td>{participant.nationality}</td>
                            <td>{participant.elo}</td>
                            {uniqueRounds.map(round => (
                                <td key={round}>{participant.points[round] !== null ? participant.points[round] : "-"}</td>
                            ))}
                            <td>{participant.totalPoints}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default UserTournamentMatchTable;