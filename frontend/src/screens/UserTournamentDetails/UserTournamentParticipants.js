import React, { useState, useEffect } from 'react';
import './UserTournamentParticipants.css';
import Header from './UserDetailsHeader';

const UserTournamentParticipants = () => {
    const [participants, setParticipants] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filteredParticipants, setFilteredParticipants] = useState([]);
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [playerCount, setPlayerCount] = useState(0); // Add player count state

    // Fetch tournament details and participants from API
    useEffect(() => {
        const fetchParticipants = async () => {
            // Replace with your API call to fetch participants and player count
            const data = [
                { id: 1, name: 'Hikaru Nakamura', nationality: 'Japan', age: 30, worldRank: 3252, rating: 2860, gamesPlayed: 7 },
                { id: 2, name: 'Vincent Keymer', nationality: 'Germany', age: 24, worldRank: 3254, rating: 2850, gamesPlayed: 2 },
                { id: 3, name: 'Wei Yi', nationality: 'China', age: 43, worldRank: 2789, rating: 3060, gamesPlayed: 13 },
                { id: 4, name: 'John Tan', nationality: 'Singapore', age: 36, worldRank: 3198, rating: 2760, gamesPlayed: 23 },
                { id: 5, name: 'Player 5', nationality: 'United Kingdom', age: 17, worldRank: 1897, rating: 4860, gamesPlayed: 11 },
            ];
            setParticipants(data);
            setPlayerCount(data.length); // Set player count based on participants length
        };
        fetchParticipants();
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

    // Filter and sort the participants list based on user inputs
    useEffect(() => {
        let updatedList = participants;

        // Filter by search term
        if (searchTerm) {
            updatedList = updatedList.filter((participant) =>
                participant.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // Sort the list
        if (sortBy) {
            updatedList = updatedList.sort((a, b) => {
                if (sortBy === 'name') {
                    return a.name.localeCompare(b.name);
                }
                else if (sortBy === 'age') {
                    return b.age - a.age;
                }
                else if (sortBy === 'worldRank') {
                    return a.worldRank - b.worldRank;
                } 
                return 0;
            });
        }

        setFilteredParticipants(updatedList);
    }, [searchTerm, sortBy, participants]);

    return (
        <div>
            <Header 
                tournamentTitle="Tournament 1" 
                playerCount={playerCount} // Use the centralized player count state
            />

            <div className="user-tournament">
                {/* Participants List */}
                <div className="participants-container">
                    {/* Search and Sort Controls */}
                    <div className="controls-container">
                        <div className="search-bar">
                            <input
                                type="text"
                                placeholder="Search for a participant"
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
                                        <div className="dropdown-item" onClick={() => handleSortChange('name')}>
                                            Name
                                        </div>
                                        <div className="dropdown-item" onClick={() => handleSortChange('age')}>
                                            Age
                                        </div>
                                        <div className="dropdown-item" onClick={() => handleSortChange('worldRank')}>
                                            World Rank
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* Participants Table */}
                    <div className="participants-list">
                        <table className="participants-table">
                            <thead>
                                <tr>
                                    <th>No</th>
                                    <th>Name</th>
                                    <th>Nationality</th>
                                    <th>Age</th>
                                    <th>World rank</th>
                                    <th>Rating</th>
                                    <th>Games played this season</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredParticipants.map((participant, index) => (
                                    <tr key={participant.id}>
                                        <td>{index + 1}</td>
                                        <td>{participant.name}</td>
                                        <td>{participant.nationality}</td>
                                        <td>{participant.age}</td>
                                        <td>{participant.worldRank}</td>
                                        <td>{participant.rating}</td>
                                        <td>{participant.gamesPlayed}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserTournamentParticipants;
