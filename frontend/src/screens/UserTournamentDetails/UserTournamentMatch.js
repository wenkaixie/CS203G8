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
    const [sortBy, setSortBy] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [selectedRound, setSelectedRound] = useState(''); // Default: empty string
    const [availableRounds, setAvailableRounds] = useState([]);
    const [tournamentTitle, setTournamentTitle] = useState('Tournament');
    const [playerCount, setPlayerCount] = useState(0);
    const [activeView, setActiveView] = useState('diagram');

    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(
                    `http://localhost:8080/api/tournaments/${tournamentId}`
                );
                const tournamentData = response.data;
                setTournamentTitle(tournamentData.name || 'Tournament');
                setPlayerCount(tournamentData.participants?.length || 0);
            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };
        fetchTournamentDetails();
    }, [tournamentId]);

    useEffect(() => {
        const fetchRounds = async () => {
            try {
                const response = await axios.get(
                    `http://localhost:8080/api/tournaments/${tournamentId}/allRounds`
                );
                setAvailableRounds(response.data);
            } catch (error) {
                console.error('Error fetching rounds:', error);
            }
        };
        fetchRounds();
    }, [tournamentId]);

    useEffect(() => {
        const fetchMatches = async () => {
            try {
                if (selectedRound) {
                    const response = await axios.get(
                        `http://localhost:8080/api/rounds/${selectedRound}/matches`
                    );
                    const matchesData = response.data;

                    const enhancedMatches = await Promise.all(
                        matchesData.map(async (match) => {
                            const [player1Response, player2Response] = await Promise.all([
                                axios.get(`http://localhost:9090/user/getUser/${match.uid1}`),
                                axios.get(`http://localhost:9090/user/getUser/${match.uid2}`),
                            ]);

                            const player1Data = player1Response.data;
                            const player2Data = player2Response.data;

                            return {
                                ...match,
                                player1: player1Data.name || 'Player 1',
                                rating1: player1Data.elo || 'N/A',
                                nationality1: player1Data.nationality || 'N/A',
                                player2: player2Data.name || 'Player 2',
                                rating2: player2Data.elo || 'N/A',
                                nationality2: player2Data.nationality || 'N/A',
                            };
                        })
                    );

                    setMatches(enhancedMatches);
                } else {
                    setMatches([]); // Clear matches if no round is selected
                }
            } catch (error) {
                console.error('Error fetching matches:', error);
                setMatches([]);
            }
        };
        fetchMatches();
    }, [selectedRound]);

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
                                        key={round.trid}
                                        onClick={() => handleRoundSelection(round.trid)}
                                    >
                                        Round {round.roundNumber}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {activeView === 'list' ? (
                    availableRounds.map((round) => (
                        <div key={round.trid}>
                            <h2 className="round-title">Round {round.roundNumber}</h2>
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
                                    </tr>
                                </thead>
                                <tbody>
                                    {matches.filter((match) => match.rid === round.trid).length > 0 ? (
                                        matches
                                            .filter((match) => match.rid === round.trid)
                                            .map((match, index) => (
                                                <tr key={index}>
                                                    <td>{index + 1}</td>
                                                    <td>{new Date(match.matchDate).toLocaleDateString()}</td>
                                                    <td>{match.location || 'N/A'}</td>
                                                    <td>{match.player1}</td>
                                                    <td>{match.rating1}</td>
                                                    <td>{match.nationality1}</td>
                                                    <td>{match.user1Score}</td>
                                                    <td className="vs-text">VS</td>
                                                    <td>{match.user2Score}</td>
                                                    <td>{match.player2}</td>
                                                    <td>{match.rating2}</td>
                                                    <td>{match.nationality2}</td>
                                                </tr>
                                            ))
                                    ) : (
                                        <tr>
                                            <td colSpan="12">No matches for this round.</td>
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
