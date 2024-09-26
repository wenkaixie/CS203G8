import React, { useState, useEffect } from 'react';
import './UserTournamentMatch.css';
import Header from './UserDetailsHeader';

const UserTournamentMatch = () => {
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredMatches, setFilteredMatches] = useState([]);
    const [sortBy, setSortBy] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [playerCount, setPlayerCount] = useState(0);

    //Replace with database values
    const dummyData = [
        {
            round: 1,
            matches: [
                { no: 1, date: 'Jun 21, 2024 08:40am', player1: 'Hikaru Nakamura', nationality1: 'Japan', player2: 'Vincent Keymer', nationality2: 'Germany', location: 'City Hall, Table 5' },
                { no: 2, date: 'Jun 21, 2024 09:45am', player1: 'Hikaru Nakamura', nationality1: 'Japan', player2: 'Vincent Keymer', nationality2: 'Germany', location: 'City Hall, Table 5' },
                { no: 3, date: 'Jun 21, 2024 12:30pm', player1: 'Hikaru Nakamura', nationality1: 'Japan', player2: 'Vincent Keymer', nationality2: 'Germany', location: 'City Hall, Table 5' }
            ]
        },
        {
            round: 2,
            matches: [
                { no: 1, date: 'Jun 21, 2024 08:40am', player1: 'Hikaru Nakamura', nationality1: 'Japan', player2: 'Vincent Keymer', nationality2: 'Germany', location: 'City Hall, Table 5' },
                { no: 2, date: 'Jun 21, 2024 09:45am', player1: 'Hikaru Nakamura', nationality1: 'Japan', player2: 'Vincent Keymer', nationality2: 'Germany', location: 'City Hall, Table 5' },
                { no: 3, date: 'Jun 21, 2024 12:30pm', player1: 'Hikaru Nakamura', nationality1: 'Japan', player2: 'Vincent Keymer', nationality2: 'Germany', location: 'City Hall, Table 5' }
            ]
        }
    ];

    // Fetch tournament matches from API (dummy data for now)
    useEffect(() => {
        setMatches(dummyData);
        const allMatches = dummyData.flatMap(roundData => roundData.matches); // Flatten all rounds into a single array
        setFilteredMatches(allMatches);
    }, []);

    // Handle search input
    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    // Toggle dropdown visibility
    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible);
    };

    // Handle selection from dropdown
    const handleSortChange = (criteria) => {
        setSortBy(criteria);
        setIsDropdownVisible(false); // Hide dropdown after selection
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
                    <div className="search-bar">
                        <input
                            type="text"
                            placeholder="Search for a round/participant"
                            value={searchTerm}
                            onChange={handleSearch}
                        />
                        {/* Search Icon */}
                        <img
                            src={require('../../assets/images/Search.png')}
                            alt="Search Icon"
                            className="search-icon"
                        />
                    </div>
                    <div className="buttons-container">
                        {/* Filter Button */}
                        <button className="filter-button">
                            <img
                                src={require('../../assets/images/Adjust.png')}
                                alt="Filter Icon"
                                className="filter-icon"
                            />
                            <span>Filter</span>
                        </button>

                        {/* Order By Dropdown Button */}
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
                                        <th>Nationality</th>
                                        <th></th>
                                        <th>Nationality</th>
                                        <th>Player 2</th>
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
            </div>
        </div>
    );
};

export default UserTournamentMatch;
