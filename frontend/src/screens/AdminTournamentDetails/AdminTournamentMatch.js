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
    const [currentRound, setCurrentRound] = useState(null); // To store the current round of the tournament
    const [activeView, setActiveView] = useState('diagram');
    const [editingMatchId, setEditingMatchId] = useState(null);
    const [participant1Result, setParticipant1Result] = useState('');
    const [participant2Result, setParticipant2Result] = useState('');

    // Fetch tournament details, including currentRound
    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}`);
                const tournamentData = response.data;
                setTournamentTitle(tournamentData.name || 'Tournament');
                setCurrentRound(tournamentData.currentRound); // Set currentRound from the tournament data
            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };
        fetchTournamentDetails();
    }, [tournamentId]);

    // Function to fetch all matches for the tournament
    const fetchMatches = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/matches`);
            const fetchedMatches = response.data;

            const allMatches = fetchedMatches.map(match => ({
                ...match,
                participants: match.participants.map(participant => ({
                    ...participant,
                    resultText: participant.isWinner ? 1 : 0 // Set based on `isWinner`
                }))
            }));

            setMatches(allMatches);
            setAvailableRounds([...new Set(fetchedMatches.map(match => match.tournamentRoundText))]);
        } catch (error) {
            console.error('Error fetching matches:', error);
        }
    };

    // Fetch matches initially
    useEffect(() => {
        fetchMatches();
    }, [tournamentId]);

    const handleEditClick = (matchId, p1Result, p2Result) => {
        setEditingMatchId(matchId);
        setParticipant1Result(p1Result);
        setParticipant2Result(p2Result);
    };

    const handleDeleteMatch = async (match) => {
        const { tournamentRoundText: roundNumber, id: matchId } = match;
        try {
            await axios.delete(`http://localhost:8080/api/tournaments/${tournamentId}/rounds/${roundNumber}/matches/${matchId}`);
            fetchMatches(); // Refresh matches after deletion
        } catch (error) {
            console.error('Error deleting match:', error);
        }
    };

    const handleResultChange = (participant, value) => {
        if (participant === 'participant1') setParticipant1Result(value);
        if (participant === 'participant2') setParticipant2Result(value);
    };

    const handleSaveClick = async (match) => {
        const { tournamentRoundText: roundNumber, id: matchId } = match;
        try {
            await axios.put(`http://localhost:8080/api/tournaments/${tournamentId}/rounds/${roundNumber}/matches/${matchId}/winner`, {
                participant1: participant1Result === '1' || participant1Result === '0.5',
                participant2: participant2Result === '1' || participant2Result === '0.5',
            });
            setEditingMatchId(null);
            fetchMatches(); // Refresh matches after update
        } catch (error) {
            console.error('Error saving match results:', error);
        }
    };

    const handleConfirmResults = async (roundNumber) => {
        try {
            await axios.post(`http://localhost:8080/api/tournaments/${tournamentId}/rounds/${roundNumber}/populateNextRound`);
            fetchMatches(); // Refresh matches after confirmation
        } catch (error) {
            console.error('Error confirming results:', error);
        }
    };

    const handleSearch = (e) => setSearchTerm(e.target.value);
    const toggleDropdown = () => setIsDropdownVisible(!isDropdownVisible);

    const handleRoundSelection = (round) => {
        setSelectedRound(round);
        setIsDropdownVisible(false);
    };

    const handleListViewClick = () => setActiveView('list');
    const handleDiagramViewClick = () => setActiveView('diagram');

    const isRoundResultsComplete = (roundMatches) => {
        return roundMatches.every(match =>
            match.participants[0].resultText !== null && match.participants[1].resultText !== null
        );
    };

    return (
        <div>
            <Header tournamentTitle={tournamentTitle} playerCount={playerCount} />
            <div className="admin-tournament-match">
                <div className="controls-container">
                    <div className="view-buttons">
                        <button className={`list-view-button ${activeView === 'list' ? 'active' : ''}`} onClick={handleListViewClick}>
                            <img src={require('../../assets/images/List-view.png')} alt="List View" className="view-icon" />
                        </button>
                        <button className={`image-view-button ${activeView === 'diagram' ? 'active pink' : ''}`} onClick={handleDiagramViewClick}>
                            <img src={require('../../assets/images/Image-view.png')} alt="Image View" className="view-icon" />
                        </button>
                    </div>

                    <div className="search-bar">
                        <input type="text" placeholder="Search for a round/participant" value={searchTerm} onChange={handleSearch} />
                        <img src={require('../../assets/images/Search.png')} alt="Search Icon" className="search-icon" />
                    </div>

                    <button className="filter-button">
                        <img src={require('../../assets/images/Adjust.png')} alt="Filter Icon" className="filter-icon" />
                        <span>Filter</span>
                    </button>

                    <div className="dropdown">
                        <button className="order-button" onClick={toggleDropdown}>
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
                                            <th>Location</th>
                                            <th>Player 1</th>
                                            <th>ELO</th>
                                            <th>Nationality</th>
                                            <th>Result</th>
                                            <th></th>
                                            <th>Result</th>
                                            <th>Player 2</th>
                                            <th>ELO</th>
                                            <th>Nationality</th>
                                            <th>State</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {roundMatches.map((match, index) => (
                                            <tr key={index} className={editingMatchId === match.id ? 'editing-row' : ''}>
                                                <td>{index + 1}</td>
                                                <td>{new Date(match.startTime).toLocaleString('en-US', {
                                                        year: 'numeric', month: 'long', day: 'numeric',
                                                        hour: '2-digit', minute: '2-digit', second: '2-digit',
                                                        hour12: true
                                                    }).replace(',', '')}</td>
                                                <td>{match.location || 'N/A'}</td>
                                                <td>{match.participants[0].name}</td>
                                                <td>{match.participants[0].elo || 'N/A'}</td>
                                                <td>{match.participants[0].nationality || 'N/A'}</td>
                                                <td>{editingMatchId === match.id ? <input className="result-input" type="number" value={participant1Result} onChange={(e) => handleResultChange('participant1', e.target.value)} /> : match.participants[0].resultText}</td>
                                                <td className="vs-text">VS</td>
                                                <td>{editingMatchId === match.id ? <input className="result-input" type="number" value={participant2Result} onChange={(e) => handleResultChange('participant2', e.target.value)} /> : match.participants[1].resultText}</td>
                                                <td>{match.participants[1].name}</td>
                                                <td>{match.participants[1].elo || 'N/A'}</td>
                                                <td>{match.participants[1].nationality || 'N/A'}</td>
                                                <td>{match.state}</td>
                                                <td className="action-buttons">
                                                    {editingMatchId === match.id ? (
                                                        <>
                                                            <button className="save-button" onClick={() => handleSaveClick(match)}>✔</button>
                                                            <button className="cancel-button" onClick={() => handleDeleteMatch(match)}>✖</button>
                                                        </>
                                                    ) : (
                                                        <button className="edit-button" onClick={() => handleEditClick(match.id, match.participants[0].resultText, match.participants[1].resultText)}>✎</button>
                                                    )}
                                                </td>
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
