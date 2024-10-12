import React, { useState, useEffect } from 'react';
import './UserTournamentMatch.css';
import Header from './UserDetailsHeader';
import { useParams } from 'react-router-dom';

const UserTournamentMatch = () => {
    const { id: tournamentId } = useParams(); // Get the tournamentId from the URL
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredMatches, setFilteredMatches] = useState([]);
    const [sortBy, setSortBy] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [selectedRound, setSelectedRound] = useState(''); // Initially, empty to display "All rounds"
    const [availableRounds, setAvailableRounds] = useState([]);
    const [tournamentTitle, setTournamentTitle] = useState('Tournament');
    const [playerCount, setPlayerCount] = useState(0);

    // Fetch tournament details including the number of players and title
    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}`);
                if (!response.ok) {
                    throw new Error('Failed to fetch tournament details');
                }
                const tournamentData = await response.json();
                setTournamentTitle(tournamentData.name || 'Tournament');
                setPlayerCount(tournamentData.users ? tournamentData.users.length : 0);
            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };

        fetchTournamentDetails();
    }, [tournamentId]);

    // Fetch rounds for the tournament
    useEffect(() => {
        const fetchRounds = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}/rounds`);
                const data = await response.json();
                setAvailableRounds(data); // Set the available rounds for the dropdown
            } catch (error) {
                console.error('Error fetching rounds:', error);
            }
        };

        fetchRounds();
    }, [tournamentId]);

    // Fetch matches for the selected round
    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const roundId = selectedRound === '' ? '' : selectedRound;
                const response = await fetch(`http://localhost:8080/api/rounds/${roundId}/matches`);
                const matchesData = await response.json();

                // Fetch player details for each match
                const enhancedMatches = await Promise.all(
                    matchesData.map(async (match) => {
                        const player1Response = await fetch(`http://localhost:9090/api/Users/${match.uid1}`);
                        const player2Response = await fetch(`http://localhost:9090/api/Users/${match.uid2}`);

                        const player1Data = await player1Response.json();
                        const player2Data = await player2Response.json();

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
                setFilteredMatches(enhancedMatches);
            } catch (error) {
                console.error('Error fetching matches:', error);
            }
        };

        fetchMatches();
    }, [selectedRound]);

    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    // Handle round selection from the dropdown
    const handleRoundSelection = (round) => {
        setSelectedRound(round); // Set selected round
        setIsDropdownVisible(false); // Hide dropdown after selection
    };

    const handleImageViewClick = () => {
        window.location.href = `/tournament/${tournamentId}/overview`; // Navigate to overview page
    };

    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible); // Toggle dropdown visibility
    };

    const handleSortChange = (criteria) => {
        setSortBy(criteria);
        setIsDropdownVisible(false); // Hide dropdown after selection
    };

    const getResult = (score1, score2) => {
        if (score1 > score2) return 'Won';
        if (score1 < score2) return 'Lost';
        return 'Draw';
    };

    // Filter and sort matches based on search term and sorting criteria
    useEffect(() => {
        let updatedList = matches;

        // Filter by search term
        if (searchTerm) {
            updatedList = updatedList.filter(
                (match) =>
                    match.player1.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.player2.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.date.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.location.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // Sort the list
        if (sortBy) {
            updatedList = updatedList.sort((a, b) => {
                if (sortBy === 'newest') {
                    return new Date(b.date) - new Date(a.date); // Sort by newest date first
                } else if (sortBy === 'oldest') {
                    return new Date(a.date) - new Date(b.date); // Sort by oldest date first
                }
                return 0;
            });
        }

        setFilteredMatches(updatedList);
    }, [searchTerm, matches, sortBy]);

    return (
        <div>
            <Header
                tournamentTitle={tournamentTitle}
                playerCount={playerCount}
            />

            <div className="user-tournament-match">
                <div className="controls-container">
                    <div className="view-buttons">
                        <button className="list-view-button">
                            <img src={require('../../assets/images/List-view.png')} alt="List View" />
                        </button>
                        <button className="image-view-button" onClick={handleImageViewClick}>
                            <img src={require('../../assets/images/Image-view.png')} alt="Image View" />
                        </button>
                    </div>

                    <div className="search-bar">
                        <input
                            type="text"
                            placeholder="Search for a round/participant/date"
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
                        <img src={require('../../assets/images/Adjust.png')} alt="Filter Icon" className="filter-icon" />
                        <span>Filter</span>
                    </button>

                    <div className="dropdown">
                        {/* Set the button to show "All rounds" by default */}
                        <button className="order-button" onClick={toggleDropdown}>
                            {selectedRound === '' ? 'All rounds' : `Round ${selectedRound}`}
                        </button>
                        {isDropdownVisible && (
                            <div className="dropdown-content">
                                {/* Show available rounds */}
                                <div
                                    className="dropdown-item"
                                    onClick={() => handleRoundSelection('')}
                                >
                                    All rounds
                                </div>
                                {availableRounds.map((round, index) => (
                                    <div
                                        className="dropdown-item"
                                        key={index}
                                        onClick={() => handleRoundSelection(round.trid)}
                                    >
                                        Round {round.roundNumber}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                <div className="match-list">
                    {filteredMatches.map((match, matchIndex) => (
                        <div key={matchIndex}>
                            <div className="round-title">Round {match.roundNumber}</div>
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
                                    <tr>
                                        <td>{matchIndex + 1}</td>
                                        <td>{match.matchDate}</td>
                                        <td>{match.location}</td>
                                        <td>{match.player1}</td>
                                        <td>{match.rating1}</td>
                                        <td>{match.nationality1}</td>
                                        <td className="result-column">
                                            <span className={`result-gap ${getResult(match.user1Score, match.user2Score).toLowerCase()}`}>
                                                {getResult(match.user1Score, match.user2Score)}
                                            </span>
                                            <span className="score">{match.user1Score}</span>
                                        </td>
                                        <td className="vs-text">VS</td>
                                        <td className="result-column">
                                            <span className="score">{match.user2Score}</span>
                                            <span className={`result-gap ${getResult(match.user2Score, match.user1Score).toLowerCase()}`}>
                                                {getResult(match.user2Score, match.user1Score)}
                                            </span>
                                        </td>
                                        <td>{match.player2}</td>
                                        <td>{match.rating2}</td>
                                        <td>{match.nationality2}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default UserTournamentMatch;
