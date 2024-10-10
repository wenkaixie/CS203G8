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
    const [selectedRound, setSelectedRound] = useState('All rounds');
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
                setPlayerCount(tournamentData.users ? tournamentData.users.length : 0); // Handle null or empty users array
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

    // Fetch matches based on the selected round
    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const roundId = selectedRound === 'All rounds' ? '' : selectedRound;
                const response = await fetch(`http://localhost:8080/api/rounds/${roundId}/matches`);
                const data = await response.json();
                setMatches(data);
                setFilteredMatches(data);
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
        setSelectedRound(round);
    };

    const handleImageViewClick = () => {
        window.location.href = `/tournament/${tournamentId}/matchtree`;
    };

    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible);
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
        let updatedList = matches; // Directly use matches

        // Filter by search term
        if (searchTerm) {
            updatedList = updatedList.filter(
                (match) =>
                    match.player1.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.player2.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.date.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.location.toLowerCase().includes(searchTerm.toLowerCase()) // Filter by location
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
                tournamentTitle={tournamentTitle} // Set the tournament name
                playerCount={playerCount} // Set the actual number of players from API
            />

            <div className="user-tournament-match">
                {/* Search and Sort Controls */}
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

                    {/* Order By Dropdown */}
                    <div className="dropdown">
                        <button className="order-button" onClick={toggleDropdown}>
                            All rounds
                        </button>
                        {isDropdownVisible && (
                            <div className="dropdown-content">
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

                {/* Matches List */}
                <div className="match-list">
                    {matches.map((roundData, roundIndex) => (
                        <div key={roundIndex}>
                            <div className="round-title">Round {roundData.round}</div>
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
                                    {roundData.matches.map((match, matchIndex) => (
                                        <tr key={matchIndex}>
                                            <td>{match.no}</td>
                                            <td>{match.date}</td>
                                            <td>{match.location}</td>
                                            <td>{match.player1}</td>
                                            <td>{match.rating1}</td>
                                            <td>{match.nationality1}</td>
                                            <td className="result-column">
                                                <span className={`result-gap ${getResult(match.score1, match.score2).toLowerCase()}`}>
                                                    {getResult(match.score1, match.score2)}
                                                </span>

                                                <span className="score">{match.score1}</span>
                                            </td>
                                            <td className="vs-text">VS</td>
                                            <td className="result-column">
                                                <span className="score">{match.score2}</span>
                                                <span className={`result-gap ${getResult(match.score2, match.score1).toLowerCase()}`}>
                                                    {getResult(match.score2, match.score1)}
                                                </span>

                                            </td>
                                            <td>{match.player2}</td>
                                            <td>{match.rating2}</td>
                                            <td>{match.nationality2}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    ))}
                </div>
            </div>
        </div >
    );
};

export default UserTournamentMatch;
