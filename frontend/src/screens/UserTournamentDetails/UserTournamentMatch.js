import React, { useState, useEffect } from 'react';
import './UserTournamentMatch.css';
import Header from './UserDetailsHeader';
import UserTournamentMatchDiagram from './UserTournamentMatchDiagram';

const UserTournamentMatch = () => {
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredMatches, setFilteredMatches] = useState([]);
    const [sortBy, setSortBy] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [playerCount, setPlayerCount] = useState(0);
    const [activeView, setActiveView] = useState('diagram'); // State to control active view
    const [selectedRound, setSelectedRound] = us
    // Dummy data with player scores and results (Win/Lost/Draw)
    const dummyData = [
        {
            round: 1,
            matches: [
                {
                    no: 1, date: 'Jun 21, 2024 08:40am', location: 'City Hall, Table 5',
                    player1: 'Hikaru Nakamura', rating1: 2000, nationality1: 'Japan', score1: 1,
                    player2: 'Vincent Keymer', rating2: 2100, nationality2: 'Germany', score2: 0
                },
                {
                    no: 2, date: 'Jun 21, 2024 09:45am', location: 'City Hall, Table 5',
                    player1: 'Hikaru Nakamura', rating1: 2000, nationality1: 'Japan', score1: 0.5,
                    player2: 'Vincent Keymer', rating2: 2100, nationality2: 'Germany', score2: 0.5
                }
            ]
        },
        {
            round: 2,
            matches: [
                {
                    no: 1, date: 'Jun 21, 2024 08:40am', location: 'City Hall, Table 5',
                    player1: 'Hikaru Nakamura', rating1: 2000, nationality1: 'Japan', score1: 1,
                    player2: 'Vincent Keymer', rating2: 2100, nationality2: 'Germany', score2: 0
                },
                {
                    no: 2, date: 'Jun 21, 2024 09:45am', location: 'City Hall, Table 5',
                    player1: 'Hikaru Nakamura', rating1: 2000, nationality1: 'Japan', score1: 0.5,
                    player2: 'Vincent Keymer', rating2: 2100, nationality2: 'Germany', score2: 0.5
                }
            ]
        }
    ];

    useEffect(() => {
        setMatches(dummyData);
        const allMatches = dummyData.flatMap(roundData => roundData.matches);
        setFilteredMatches(allMatches);
    }, [dummyData]);

    // Switch view between list and diagram
    const handleListButtonClick = () => {
        setActiveView('list');
    };

    const handleDiagramButtonClick = () => {
        setActiveView('diagram');
    };
    // Handle search input
    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    // Handle round selection from the dropdown (without changing display yet)
    const handleRoundSelection = (round) => {
        setSelectedRound(round);
        // Filtering logic will be added here later if needed
    };

    const handleImageViewClick = () => {
        const tournamentId = 1; // Replace with the actual tournament ID
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
        let updatedList = matches.flatMap(roundData => roundData.matches); // Flatten all rounds into a single array

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
                tournamentTitle="Tournament 1"
                playerCount={playerCount} // Replace with actual count from database
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
                    <div className="buttons-container">
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
                                Order By
                            </button>
                            {isDropdownVisible && (
                                <div className="dropdown-content">
                                    <div className="dropdown-item" onClick={() => handleSortChange('newest')}>
                                        Newest
                                    </div>
                                    <div className="dropdown-item" onClick={() => handleSortChange('oldest')}>
                                        Oldest
                                    </div>
                                </div>
                            )}
                        </div>

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
                                <div className="dropdown-item" onClick={() => handleRoundSelection('round1')}>
                                    Round 1
                                </div>
                                <div className="dropdown-item" onClick={() => handleRoundSelection('round2')}>
                                    Round 2
                                </div>
                            </div>
                        )}
                    </div>

                </div>
                {/* Conditionally Render List or Diagram */}
                {activeView === 'list' && (
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
                                            <th>Nationality</th>
                                            <th></th>
                                            <th>Nationality</th>
                                            <th>Player 2</th>
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
                          
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {roundData.matches.map((match, matchIndex) => (
                                            <tr key={matchIndex}>
                                                <td>{match.no}</td>
                                                <td>{match.date}</td>
                                                <td>{match.location}</td> 
                                                <td>{match.player1}</td>
                                                <td>{match.nationality1}</td>
                                                <td>VS</td>
                                                <td>{match.nationality2}</td>
                                                <td>{match.player2}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        ))}
                    </div>
                )}

                {activeView === 'diagram' && (
                    <UserTournamentMatchDiagram />
                )}
            </div>
        </div >
    );
};

export default UserTournamentMatch;