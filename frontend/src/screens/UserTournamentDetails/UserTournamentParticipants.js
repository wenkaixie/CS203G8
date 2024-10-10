import React, { useState, useEffect } from 'react';
import './UserTournamentParticipants.css';
import Header from './UserDetailsHeader';
import FilterOverlay from './FilterOverlay'; // Import the new FilterOverlay component

const UserTournamentParticipants = () => {
    const [participants, setParticipants] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filteredParticipants, setFilteredParticipants] = useState([]);
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [playerCount, setPlayerCount] = useState(0);
    const [isFilterOpen, setIsFilterOpen] = useState(false); // State for controlling filter modal

    useEffect(() => {
        const fetchParticipants = async () => {
            const data = [
                { id: 1, name: 'Hikaru Nakamura', nationality: 'Japan', age: 30, worldRank: 3252, rating: 2860, gamesPlayed: 7 },
                { id: 2, name: 'Vincent Keymer', nationality: 'Germany', age: 24, worldRank: 3254, rating: 2850, gamesPlayed: 2 },
                { id: 3, name: 'Wei Yi', nationality: 'China', age: 43, worldRank: 2789, rating: 3060, gamesPlayed: 13 },
                { id: 4, name: 'John Tan', nationality: 'Singapore', age: 36, worldRank: 3198, rating: 2760, gamesPlayed: 23 },
                { id: 5, name: 'Player 5', nationality: 'United Kingdom', age: 17, worldRank: 1897, rating: 4860, gamesPlayed: 11 },
            ];
            setParticipants(data);
            setPlayerCount(data.length);
        };
        fetchParticipants();
    }, []);

    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible);
    };

    const handleSortChange = (criteria) => {
        setSortBy(criteria);
        setIsDropdownVisible(false);
    };

    useEffect(() => {
        let updatedList = participants;

        if (searchTerm) {
            updatedList = updatedList.filter((participant) =>
                participant.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

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
            <Header activetab="participants" tournamentTitle="Tournament 1" playerCount={playerCount} />

            <div className="user-tournament-participants">
                <div className="controls-container">
                    <div className="search-bar">
                        <input
                            type="text"
                            placeholder="Search for a participant"
                            value={searchTerm}
                            onChange={handleSearch}
                        />
                        <img src={require('../../assets/images/Search.png')} alt="Search Icon" className="search-icon" />
                    </div>
                    <div className="buttons-container">
                        <button className="filter-button" onClick={() => setIsFilterOpen(true)}>
                            <img src={require('../../assets/images/Adjust.png')} alt="Filter Icon" className="filter-icon" />
                            <span>Filter</span>
                        </button>
                        <div className="dropdown">
                            <button className="order-button" onClick={toggleDropdown}>Order By</button>
                            {isDropdownVisible && (
                                <div className="dropdown-content">
                                    <div className="dropdown-item" onClick={() => handleSortChange('name')}>Name</div>
                                    <div className="dropdown-item" onClick={() => handleSortChange('age')}>Age</div>
                                    <div className="dropdown-item" onClick={() => handleSortChange('worldRank')}>World Rank</div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

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

            {/* Filter Overlay */}
            <FilterOverlay isOpen={isFilterOpen} onClose={() => setIsFilterOpen(false)} />
        </div>
    );
};

export default UserTournamentParticipants;
