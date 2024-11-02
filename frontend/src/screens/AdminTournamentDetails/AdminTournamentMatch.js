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

            setMatches(fetchedMatches.map(match => ({
                ...match,
                participants: match.participants.map(participant => ({
                    ...participant,
                    resultText: participant.isWinner ? 1 : 0
                }))
            })));

            const rounds = [...new Set(fetchedMatches.map(match => match.tournamentRoundText))];
            setAvailableRounds(rounds.sort((a, b) => b - a));
        } catch (error) {
            console.error('Error fetching matches:', error);
        }
    };

    useEffect(() => {
        fetchMatches();
    }, [tournamentId]);

    const handleEditClick = (matchId) => {
        setEditingMatchId(matchId);
    };

    const handleWinnerSelection = (matchId, value) => {
        setWinnerSelection(prev => ({ ...prev, [matchId]: value }));
    };

    const handleSaveClick = async (match) => {
        const { tournamentRoundText: roundNumber, id: matchId } = match;
        const winnerName = winnerSelection[matchId];
        console.log(winnerName);
        try {
            await axios.put(`http://localhost:8080/api/tournaments/${tournamentId}/rounds/${roundNumber}/matches/${matchId}/winner`, winnerName);
            setEditingMatchId(null);
            fetchMatches();
        } catch (error) {
            console.error('Error saving match results:', error);
        }
    };

    const handleConfirmResults = async (roundNumber) => {
        try {
            await axios.post(`http://localhost:8080/api/tournaments/${tournamentId}/rounds/${roundNumber}/populateNextRound`);
            fetchMatches();
        } catch (error) {
            console.error('Error confirming results:', error);
        }
    };

    const isRoundResultsComplete = (roundMatches) => {
        return roundMatches.every(match =>
            [0, 0.5, 1].includes(match.participants[0].resultText) &&
            [0, 0.5, 1].includes(match.participants[1].resultText)
        );
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
                                <div className="dropdown-item" onClick={() => setSelectedRound('')}>All rounds</div>
                                {availableRounds.map((round) => (
                                    <div className="dropdown-item" key={round} onClick={() => setSelectedRound(round)}>
                                        Round {round}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {activeView === 'list' ? (
                    availableRounds.map((round) => {
                        const roundMatches = matches.filter(match => match.tournamentRoundText === round);
                        const allResultsEntered = isRoundResultsComplete(roundMatches);
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
                                                <td>{match.participants[0].name}</td>
                                                <td>{match.participants[0].elo || 'N/A'}</td>
                                                <td>{match.participants[0].nationality || 'N/A'}</td>
                                                <td>{match.participants[0].resultText || '-'}</td>
                                                <td className="vs-text">VS</td>
                                                <td>{match.participants[1].resultText || '-'}</td>
                                                <td>{match.participants[1].name}</td>
                                                <td>{match.participants[1].elo || 'N/A'}</td>
                                                <td>{match.participants[1].nationality || 'N/A'}</td>
                                                <td>
                                                    {editingMatchId === match.id ? (
                                                        <select
                                                            onChange={(e) => handleWinnerSelection(match.id, e.target.value)}
                                                            value={winnerSelection[match.id] || ''}
                                                        >
                                                            <option value="">Select</option>
                                                            <option value={match.participants[0].name}>{match.participants[0].name}</option>
                                                            <option value={match.participants[1].name}>{match.participants[1].name}</option>
                                                            <option value="Draw">Draw</option>
                                                        </select>
                                                    ) : (
                                                        winnerSelection[match.id] || '-'
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
                                        className={`confirm-results-button ${allResultsEntered ? 'active' : 'inactive'}`}
                                        onClick={() => handleConfirmResults(round)}
                                        disabled={!allResultsEntered}
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
