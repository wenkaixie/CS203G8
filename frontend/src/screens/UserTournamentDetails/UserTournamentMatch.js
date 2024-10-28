import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './UserTournamentMatch.css';
import Header from './UserDetailsHeader';
import { useParams } from 'react-router-dom';
import UserTournamentMatchDiagram from './UserTournamentMatchDiagram';

const UserTournamentMatch = () => {
    const { tournamentId } = useParams();
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [selectedRound, setSelectedRound] = useState(''); // Default: empty string (all rounds)
    const [availableRounds, setAvailableRounds] = useState([]); // To hold unique round numbers
    const [tournamentTitle, setTournamentTitle] = useState('Tournament');
    const [playerCount, setPlayerCount] = useState(0);
    const [activeView, setActiveView] = useState('diagram');

    // Fetch tournament details
    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(
                    `http://localhost:8080/api/tournaments/${tournamentId}`
                );
                const tournamentData = response.data;
                setTournamentTitle(tournamentData.name || 'Tournament');
                setPlayerCount(tournamentData.users?.length || 0);
            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };
        fetchTournamentDetails();
    }, [tournamentId]);

    // Fetch participant information (rating and nationality)
    const fetchParticipantInfo = async (uid) => {
        try {
            const response = await axios.get(`http://localhost:9090/user/getUser/${uid}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching user data for ${uid}:`, error);
            return { rating: 'N/A', nationality: 'N/A' }; // Return default values if the API call fails
        }
    };

    // Fetch all matches for the tournament and enhance participant data
    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const response = await axios.get(
                    `http://localhost:8080/api/tournaments/${tournamentId}/matches`
                );
                const fetchedMatches = response.data;

                // Fetch additional information
                const enhancedMatches = await Promise.all(fetchedMatches.map(async (match) => {
                    const participant1Data = await fetchParticipantInfo(match.participants[0].id);
                    const participant2Data = await fetchParticipantInfo(match.participants[1].id);

                    return {
                        ...match,
                        participants: [
                            { ...match.participants[0], ...participant1Data },
                            { ...match.participants[1], ...participant2Data }
                        ]
                    };
                }));

                setMatches(enhancedMatches);

                // Extract unique round numbers from matches using `tournamentRoundText`
                const rounds = [...new Set(enhancedMatches.map(match => match.tournamentRoundText))];
                setAvailableRounds(rounds);
            } catch (error) {
                console.error('Error fetching matches:', error);
                setMatches([]);
            }
        };
        fetchMatches();
    }, [tournamentId]);

    const handleSearch = (e) => setSearchTerm(e.target.value);
    const toggleDropdown = () => setIsDropdownVisible(!isDropdownVisible);

    const handleRoundSelection = (round) => {
        setSelectedRound(round);
        setIsDropdownVisible(false);
    };

    const handleListViewClick = () => setActiveView('list');
    const handleDiagramViewClick = () => setActiveView('diagram');

    return (
        <div>
            <Header tournamentTitle={tournamentTitle} playerCount={playerCount} />
            <div className="user-tournament-match">
                <div className="controls-container">
                    <div className="view-buttons">
                        <button
                            className={`list-view-button ${activeView === 'list' ? 'active' : ''}`}
                            onClick={handleListViewClick}
                        >
                            <img
                                src={require('../../assets/images/List-view.png')}
                                alt="List View"
                                className="view-icon"
                            />
                        </button>
                        <button
                            className={`image-view-button ${activeView === 'diagram' ? 'active pink' : ''}`}
                            onClick={handleDiagramViewClick}
                        >
                            <img
                                src={require('../../assets/images/Image-view.png')}
                                alt="Image View"
                                className="view-icon"
                            />
                        </button>
                    </div>

                    <div className="search-bar">
                        <input
                            type="text"
                            placeholder="Search for a round/participant"
                            value={searchTerm}
                            onChange={handleSearch}
                        />
                        <img
                            src={require('../../assets/images/Search.png')}
                            alt="Search Icon"
                            className="search-icon"
                        />
                    </div>

                    <button className="filter-button">
                        <img
                            src={require('../../assets/images/Adjust.png')}
                            alt="Filter Icon"
                            className="filter-icon"
                        />
                        <span>Filter</span>
                    </button>

                    <div className="dropdown">
                        <button className="order-button" onClick={toggleDropdown}>
                            {selectedRound === '' ? 'All rounds' : `Round ${selectedRound}`}
                        </button>
                        {isDropdownVisible && (
                            <div className="dropdown-content">
                                <div className="dropdown-item" onClick={() => handleRoundSelection('')}>
                                    All rounds
                                </div>
                                {availableRounds.map((round) => (
                                    <div
                                        className="dropdown-item"
                                        key={round}
                                        onClick={() => handleRoundSelection(round)}
                                    >
                                        Round {round}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {activeView === 'list' ? (
                    availableRounds.map((round) => (
                        <div key={round}>
                            <h2 className="round-title">Round {round}</h2>
                            <table className="matches-table">
                                <thead>
                                    <tr>
                                        <th>No</th>
                                        <th>Date</th>
                                        <th>Location</th>
                                        <th>Player 1</th>
                                        <th>Rating</th>
                                        <th>Nationality</th>
                                        <th>Result</th>
                                        <th></th>
                                        <th>Result</th>
                                        <th>Player 2</th>
                                        <th>Rating</th>
                                        <th>Nationality</th>
                                        <th>State</th> {/* New column for match state */}
                                    </tr>
                                </thead>
                                <tbody>
                                    {matches.filter((match) => match.tournamentRoundText === round).length > 0 ? (
                                        matches
                                            .filter((match) => match.tournamentRoundText === round)
                                            .map((match, index) => (
                                                <tr key={index}>
                                                    <td>{index + 1}</td>
                                                    <td>{new Date(match.startTime).toLocaleDateString()}</td>
                                                    <td>{match.location || 'N/A'}</td>
                                                    <td>{match.participants[0].name}</td>
                                                    <td>{match.participants[0].rating}</td>
                                                    <td>{match.participants[0].nationality}</td>
                                                    <td>{match.participants[0].resultText}</td>
                                                    <td className="vs-text">VS</td>
                                                    <td>{match.participants[1].resultText}</td>
                                                    <td>{match.participants[1].name}</td>
                                                    <td>{match.participants[1].rating}</td>
                                                    <td>{match.participants[1].nationality}</td>
                                                    <td>{match.state}</td> 
                                                </tr>
                                            ))
                                    ) : (
                                        <tr>
                                            <td colSpan="13">No matches for this round.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    ))
                ) : (
                    <UserTournamentMatchDiagram />
                )}
            </div>
        </div>
    );
};

export default UserTournamentMatch;
