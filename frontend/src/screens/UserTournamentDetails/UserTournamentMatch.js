import React, { useState, useEffect } from 'react';
import './UserTournamentMatch.css';
import Header from './UserDetailsHeader';
import { useParams } from 'react-router-dom';
import UserTournamentMatchDiagram from './UserTournamentMatchDiagram';

const UserTournamentMatch = () => {
    const { tournamentId } = useParams(); // Get the tournamentId from the URL
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredMatches, setFilteredMatches] = useState([]);
    const [sortBy, setSortBy] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [selectedRound, setSelectedRound] = useState(''); // Initially, empty to display "All rounds"
    const [availableRounds, setAvailableRounds] = useState([]);
    const [tournamentTitle, setTournamentTitle] = useState('Tournament');
    const [playerCount, setPlayerCount] = useState(0);
    const [activeView, setActiveView] = useState('diagram'); // State to control active view

    // Fetch tournament details including the number of players and title
    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}`);
                if (!response.ok) throw new Error('Failed to fetch tournament details');

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
                setAvailableRounds(data);
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
                const roundId = selectedRound || '';
                const response = await fetch(`http://localhost:8080/api/rounds/${roundId}/matches`);
                const matchesData = await response.json();

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

    const handleListButtonClick = () => setActiveView('list');
    const handleDiagramButtonClick = () => setActiveView('diagram');
    const handleSearch = (e) => setSearchTerm(e.target.value);
    const toggleDropdown = () => setIsDropdownVisible(!isDropdownVisible);

    const handleRoundSelection = (round) => {
        setSelectedRound(round);
        setIsDropdownVisible(false);
    };

    const handleSortChange = (criteria) => {
        setSortBy(criteria);
        setIsDropdownVisible(false);
    };

    // Filter and sort matches based on search term and sorting criteria
    useEffect(() => {
        let updatedList = matches;

        if (searchTerm) {
            updatedList = updatedList.filter(
                (match) =>
                    match.player1.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.player2.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.date.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.location.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        if (sortBy) {
            updatedList = updatedList.sort((a, b) => 
                sortBy === 'newest' ? new Date(b.date) - new Date(a.date) : new Date(a.date) - new Date(b.date)
            );
        }

        setFilteredMatches(updatedList);
    }, [searchTerm, matches, sortBy]);

    return (
        <div>
            <Header tournamentTitle={tournamentTitle} playerCount={playerCount} />

            <div className="user-tournament-match">
                <div className="controls-container">
                    <div className="user-diagram-header-buttons">
                        <button
                            onClick={handleDiagramButtonClick}
                            className={`user-diagram-header-button ${activeView === 'diagram' ? 'active' : ''}`}
                        >
                            Diagram
                        </button>
                        <button
                            onClick={handleListButtonClick}
                            className={`user-diagram-header-button ${activeView === 'list' ? 'active' : ''}`}
                        >
                            List
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

                {activeView === 'list' ? (
                    <div className="match-list">
                        {filteredMatches.map((match, index) => (
                            <div key={index}>
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
                                            <td>{index + 1}</td>
                                            <td>{match.matchDate}</td>
                                            <td>{match.location}</td>
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
                                    </tbody>
                                </table>
                            </div>
                        ))}
                    </div>
                ) : (
                    <UserTournamentMatchDiagram />
                )}
            </div>
        </div>
    );
};

export default UserTournamentMatch;
