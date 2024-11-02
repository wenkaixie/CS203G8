import React from 'react';
import './UserTournamentMatch.css';

const UserTournamentMatchTable = () => {
    const participantsData = [
        {
            "nationality": "Singapore",
            "joinedAt": { "seconds": 1730044800, "nanos": 919000000 },
            "name": "Alice",
            "elo": 2866,
            "authId": "GJnDtAZVzbSdGrWLjIx9ZV8cbgo1"
        },
        {
            "nationality": "Vietnam",
            "joinedAt": { "seconds": 1730044800, "nanos": 919000000 },
            "name": "Bob",
            "elo": 2866,
            "authId": "Oz5AXlGVdDeO1B5IsjPrfTOU3Al1"
        },
        {
            "nationality": "China",
            "joinedAt": { "seconds": 1730044800, "nanos": 919000000 },
            "name": "Charlie",
            "elo": 2866,
            "authId": "YMFPwRdSR1VeVPR5PokUKxJSQdE2"
        },
        {
            "nationality": "South Korea",
            "joinedAt": { "seconds": 1730044800, "nanos": 919000000 },
            "name": "David",
            "elo": 2866,
            "authId": "fCpbs06mE3RpaatJv9oxflvu5K23"
        }
    ];

    const matches = [
        {
            "id": 1,
            "name": "Match 1",
            "nextMatchId": 4,
            "tournamentRoundText": 1,
            "startTime": "2024-10-16T04:48:27.446487Z",
            "state": "PENDING",
            "participants": [
                { "id": "1", "authId": "GJnDtAZVzbSdGrWLjIx9ZV8cbgo1", "name": "Alice", "resultText": "", "elo": 2866, "nationality": "Singapore", "isWinner": true },
                { "id": "2", "authId": "Oz5AXlGVdDeO1B5IsjPrfTOU3Al1", "name": "Bob", "resultText": "", "elo": 2866, "nationality": "Vietnam", "isWinner": false }
            ]
        },
        {
            "id": 2,
            "name": "Match 2",
            "nextMatchId": 4,
            "tournamentRoundText": 1,
            "startTime": "2024-10-16T04:49:27.446487Z",
            "state": "PENDING",
            "participants": [
                { "id": "3", "authId": "YMFPwRdSR1VeVPR5PokUKxJSQdE2", "name": "Charlie", "resultText": "", "elo": 2866, "nationality": "China", "isWinner": true },
                { "id": "4", "authId": "fCpbs06mE3RpaatJv9oxflvu5K23", "name": "David", "resultText": "", "elo": 2866, "nationality": "South Korea", "isWinner": false }
            ]
        },
        {
            "id": 3,
            "name": "Match 3",
            "nextMatchId": 4,
            "tournamentRoundText": 2,
            "startTime": "2024-10-16T04:50:27.446487Z",
            "state": "PENDING",
            "participants": [
                { "id": "1", "authId": "GJnDtAZVzbSdGrWLjIx9ZV8cbgo1", "name": "Alice", "resultText": "", "elo": 2866, "nationality": "Singapore", "isWinner": true },
                { "id": "3", "authId": "YMFPwRdSR1VeVPR5PokUKxJSQdE2", "name": "Charlie", "resultText": "", "elo": 2866, "nationality": "China", "isWinner": false }
            ]
        },
        {
            "id": 4,
            "name": "Match 4",
            "nextMatchId": 6,
            "tournamentRoundText": 2,
            "startTime": "2024-10-16T04:51:27.446487Z",
            "state": "PENDING",
            "participants": [
                { "id": "2", "authId": "Oz5AXlGVdDeO1B5IsjPrfTOU3Al1", "name": "Bob", "resultText": "", "elo": 2866, "nationality": "Vietnam", "isWinner": false },
                { "id": "4", "authId": "fCpbs06mE3RpaatJv9oxflvu5K23", "name": "David", "resultText": "", "elo": 2866, "nationality": "South Korea", "isWinner": true }
            ]
        },
        {
            "id": 5,
            "name": "Match 5",
            "nextMatchId": 6,
            "tournamentRoundText": 3,
            "startTime": "2024-10-16T04:52:27.446487Z",
            "state": "PENDING",
            "participants": [
                { "id": "1", "authId": "GJnDtAZVzbSdGrWLjIx9ZV8cbgo1", "name": "Alice", "resultText": "", "elo": 2866, "nationality": "Singapore", "isWinner": true },
                { "id": "4", "authId": "fCpbs06mE3RpaatJv9oxflvu5K23", "name": "David", "resultText": "", "elo": 2866, "nationality": "South Korea", "isWinner": false }
            ]
        },
        {
            "id": 6,
            "name": "Match 6",
            "nextMatchId": 0,
            "tournamentRoundText": 3,
            "startTime": "2024-10-16T04:53:27.446487Z",
            "state": "PENDING",
            "participants": [
                { "id": "2", "authId": "Oz5AXlGVdDeO1B5IsjPrfTOU3Al1", "name": "Bob", "resultText": "", "elo": 2866, "nationality": "Vietnam", "isWinner": true },
                { "id": "3", "authId": "YMFPwRdSR1VeVPR5PokUKxJSQdE2", "name": "Charlie", "resultText": "", "elo": 2866, "nationality": "China", "isWinner": true }
            ]
        }
    ];

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

    // Calculate total points for each participant and sort by total points in descending order
    const participantsWithPoints = participantsData.map(participant => {
        const points = calculatePoints(participant.authId);
        const totalPoints = Object.values(points).reduce((acc, curr) => acc + (curr || 0), 0);
        return { ...participant, points, totalPoints };
    }).sort((a, b) => b.totalPoints - a.totalPoints);

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
                            <td>{participant.name}</td>
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