import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './AdminTournamentMatch.css';
import Header from './AdminTournamentHeader';
import { useParams } from 'react-router-dom';
import AdminTournamentMatchDiagram from './AdminTournamentMatchDiagram';

const AdminTournamentMatch = () => {
    const { tournamentId } = useParams();
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [selectedRound, setSelectedRound] = useState('');
    const [availableRounds, setAvailableRounds] = useState([]);
    const [tournamentTitle, setTournamentTitle] = useState('Tournament');
    const [playerCount, setPlayerCount] = useState(0);
    const [currentRound, setCurrentRound] = useState(null);
    const [activeView, setActiveView] = useState('diagram');
    const [editingMatchId, setEditingMatchId] = useState(null);
    const [winnerSelection, setWinnerSelection] = useState({});
    const [isConfirmButtonActive, setIsConfirmButtonActive] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');

    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}`);
                const tournamentData = response.data;
                setTournamentTitle(tournamentData.name || 'Tournament');
                setCurrentRound(tournamentData.currentRound);
            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };
        fetchTournamentDetails();
    }, [tournamentId]);

    const fetchMatches = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/matches`);
            const fetchedMatches = response.data;

            setMatches(
                fetchedMatches.map(match => ({
                    ...match,
                    participants: match.participants.map(participant => ({
                        ...participant,
                        displayResult: match.result === null
                            ? '-' // If no result yet, display '-'
                            : match.draw
                                ? 0.5 // If match is a draw, display 0.5
                                : participant.isWinner
                                    ? 1 // If participant is the winner, display 1
                                    : 0 // If participant is not the winner and it's not a draw, display 0
                    })),
                    draw: match.draw // Keep track of draw status for the match
                }))
            );

            const rounds = [...new Set(fetchedMatches.map(match => match.tournamentRoundText))];
            setAvailableRounds(rounds.sort((a, b) => b - a));

            checkConfirmButtonStatus(fetchedMatches, currentRound);
        } catch (error) {
            console.error('Error fetching matches:', error);
        }
    };

    useEffect(() => {
        fetchMatches();
    }, [tournamentId, currentRound]);

    const checkConfirmButtonStatus = (matchesToCheck, round) => {
        const roundMatches = matchesToCheck.filter(match => match.tournamentRoundText === round);
        const allWinnersSelected = roundMatches.every(match => winnerSelection[match.id]);
        setIsConfirmButtonActive(allWinnersSelected);
    };

    const handleEditClick = (matchId) => {
        setEditingMatchId(matchId);
    };

    const handleWinnerSelection = (matchId, selectedOption) => {
        setWinnerSelection(prev => ({ ...prev, [matchId]: selectedOption }));

        setMatches(prevMatches => prevMatches.map(match => {
            if (match.id === matchId) {
                const isDraw = selectedOption === "Draw";
                const updatedParticipants = match.participants.map((participant) => ({
                    ...participant,
                    displayResult: isDraw ? 0.5 : participant.isWinner ? 1 : 0,
                    isWinner: !isDraw && selectedOption === `Player ${participant === match.participants[0] ? 1 : 2}`
                }));
                return { ...match, participants: updatedParticipants, draw: isDraw };
            }
            return match;
        }));

        checkConfirmButtonStatus(matches, selectedRound); // Recheck status after selection
    };

    const handleSaveClick = (match) => {
        setEditingMatchId(null);
    };

    const handleConfirmResults = async (roundNumber) => {
        const matchResults = {};
        matches
            .filter(match => match.tournamentRoundText === roundNumber)
            .forEach(match => {
                const result = winnerSelection[match.id];
                if (result === 'Player 1') matchResults[match.id] = 'PLAYER1_WIN';
                else if (result === 'Player 2') matchResults[match.id] = 'PLAYER2_WIN';
                else if (result === 'Draw') matchResults[match.id] = 'DRAW';
            });

        try {
            await axios.put(
                `http://localhost:8080/api/tournaments/${tournamentId}/rounds/${roundNumber}/matches/results`,
                matchResults
            );

            await axios.post(`http://localhost:8080/api/tournaments/${tournamentId}/rounds/${roundNumber}/populateNextRound`);
            fetchMatches();

            setSuccessMessage('The next round has been created successfully.');
            setTimeout(() => setSuccessMessage(''), 3000);
        } catch (error) {
            console.error('Error confirming results:', error);
        }
    };

    const confirmSubmission = (roundNumber) => {
        const isConfirmed = window.confirm("Are you sure you want to submit the results?");
        if (isConfirmed) {
            handleConfirmResults(roundNumber);
        }
    };

    const handleRoundSelection = (round) => {
        setSelectedRound(round);
        setIsDropdownVisible(false);
        checkConfirmButtonStatus(matches, round);
    };

    return (
        <div>
            <Header tournamentTitle={tournamentTitle} playerCount={playerCount} />
            <div className="admin-tournament-match">
                <div className="controls-container">
                    <div className="view-buttons">
                        <button className={`list-view-button ${activeView === 'list' ? 'active' : ''}`} onClick={() => setActiveView('list')}>
                            <img src={require('../../assets/images/List-view.png')} alt="List View" className="view-icon" />
                        </button>
                        <button className={`image-view-button ${activeView === 'diagram' ? 'active pink' : ''}`} onClick={() => setActiveView('diagram')}>
                            <img src={require('../../assets/images/Image-view.png')} alt="Image View" className="view-icon" />
                        </button>
                    </div>

                    <div className="search-bar">
                        <input type="text" placeholder="Search for a round/participant" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                        <img src={require('../../assets/images/Search.png')} alt="Search Icon" className="search-icon" />
                    </div>

                    <button className="filter-button">
                        <img src={require('../../assets/images/Adjust.png')} alt="Filter Icon" className="filter-icon" />
                        <span>Filter</span>
                    </button>

                    <div className="dropdown">
                        <button className="order-button" onClick={() => setIsDropdownVisible(!isDropdownVisible)}>
                            {selectedRound === '' ? 'All rounds' : `Round ${selectedRound}`}
                        </button>
                        {isDropdownVisible && (
                            <div className="dropdown-content">
                                <div className="dropdown-item" onClick={() => handleRoundSelection('')}>All rounds</div>
                                {availableRounds.map((round) => (
                                    <div className="dropdown-item" key={round} onClick={() => handleRoundSelection(round)}>
                                        Round {round}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {successMessage && (
                    <div className="success-message">
                        {successMessage}
                    </div>
                )}

                {activeView === 'list' ? (
                    availableRounds
                        .filter(round => selectedRound === '' || round === selectedRound)
                        .map((round) => {
                            const roundMatches = matches.filter(match => match.tournamentRoundText === round);
                            const isCurrentRound = round === currentRound;

                            return (
                                <div key={round}>
                                    <h2 className="round-title">Round {round}</h2>
                                    <table className="matches-table">
                                        <thead>
                                            <tr>
                                                <th>No</th>
                                                <th>Date</th>
                                                <th>Player 1</th>
                                                <th>ELO</th>
                                                <th>Nationality</th>
                                                <th>Result</th>
                                                <th></th>
                                                <th>Result</th>
                                                <th>Player 2</th>
                                                <th>ELO</th>
                                                <th>Nationality</th>
                                                <th>Winner</th>
                                                <th>State</th>
                                                {isCurrentRound && <th>Actions</th>}
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {roundMatches.map((match, index) => (
                                                <tr key={index} className={editingMatchId === match.id ? 'editing-row' : ''}>
                                                    <td>{index + 1}</td>
                                                    <td>{new Date(match.startTime).toLocaleString()}</td>
                                                    <td>{match.participants?.[0]?.name || '-'}</td>
                                                    <td>{match.participants?.[0]?.elo || '-'}</td>
                                                    <td>{match.participants?.[0]?.nationality || '-'}</td>
                                                    <td>{match.participants?.[0]?.displayResult ?? '-'}</td>
                                                    <td className="vs-text">VS</td>
                                                    <td>{match.participants?.[1]?.displayResult ?? '-'}</td>
                                                    <td>{match.participants?.[1]?.name || '-'}</td>
                                                    <td>{match.participants?.[1]?.elo || '-'}</td>
                                                    <td>{match.participants?.[1]?.nationality || '-'}</td>
                                                    <td>
                                                        {editingMatchId === match.id ? (
                                                            <select
                                                                onChange={(e) => handleWinnerSelection(match.id, e.target.value)}
                                                                value={winnerSelection[match.id] || ''}
                                                            >
                                                                <option value="">Select</option>
                                                                <option value="Player 1">Player 1</option>
                                                                <option value="Player 2">Player 2</option>
                                                                <option value="Draw">Draw</option>
                                                            </select>
                                                        ) : (
                                                            match.draw
                                                                ? 'Draw'
                                                                : match.participants[0].isWinner
                                                                    ? 'Player 1'
                                                                    : match.participants[1].isWinner
                                                                        ? 'Player 2'
                                                                        : '-'
                                                        )}
                                                    </td>
                                                    <td>{match.state}</td>
                                                    {isCurrentRound && (
                                                        <td className="action-buttons">
                                                            {editingMatchId === match.id ? (
                                                                <>
                                                                    <button className="save-button" onClick={() => handleSaveClick(match)}>✔</button>
                                                                    <button className="cancel-button" onClick={() => setEditingMatchId(null)}>✖</button>
                                                                </>
                                                            ) : (
                                                                <button className="edit-button" onClick={() => handleEditClick(match.id)}>✎</button>
                                                            )}
                                                        </td>
                                                    )}
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                    {isCurrentRound && (
                                        <button
                                            className={`confirm-results-button ${isConfirmButtonActive ? 'active' : 'inactive'}`}
                                            onClick={() => confirmSubmission(round)}
                                            disabled={!isConfirmButtonActive}
                                        >
                                            Confirm results
                                        </button>
                                    )}
                                </div>
                            );
                        })
                ) : (
                    <AdminTournamentMatchDiagram />
                )}
            </div>
        </div>
    );
};

export default AdminTournamentMatch;

