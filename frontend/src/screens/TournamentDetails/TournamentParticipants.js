import React, { useState, useEffect } from 'react';
import './UserTournamentParticipants.css';
import SearchIconPath from '../../assets/images/Search.png';
import FilterIconPath from '../../assets/images/Adjust.png';
import Navbar from '../../components/navbar/Navbar';

const UserTournamentParticipants = () => {
    const [participants, setParticipants] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const [filteredParticipants, setFilteredParticipants] = useState([]);
    const [isRegistered, setIsRegistered] = useState(false);

    // Fetch tournament details and participants from API
    useEffect(() => {
        // Fetch data from your API
        const fetchParticipants = async () => {
            // Replace with your API call
            const data = [
                { id: 1, name: 'Hikaru Nakamura', nationality: 'Japan', age: 30, worldRank: 3252, rating: 2860, gamesPlayed: 7 },
                { id: 2, name: 'Vincent Keymer', nationality: 'Germany', age: 24, worldRank: 3254, rating: 2850, gamesPlayed: 2 },
                { id: 3, name: 'Wei Yi', nationality: 'China', age: 43, worldRank: 2789, rating: 3060, gamesPlayed: 13 },
                { id: 4, name: 'John Tan', nationality: 'Singapore', age: 36, worldRank: 3198, rating: 2760, gamesPlayed: 23 },
                { id: 5, name: 'Player 5', nationality: 'United Kingdom', age: 17, worldRank: 1897, rating: 4860, gamesPlayed: 11 },
            ];
            setParticipants(data);
        };
        fetchParticipants();
    }, []);

    // Handle search input
    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    // Handle sorting option
    const handleSortChange = (e) => {
        setSortBy(e.target.value);
    };

    // Handle filter option
    const handleFilterChange = (e) => {
        setFilterStatus(e.target.value);
    };

    // Handle register button click
    const handleRegister = () => {
        // Logic to handle registration, e.g., API call
        setIsRegistered(true);
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
                if (sortBy === 'worldRank') {
                    return a.worldRank - b.worldRank;
                } else if (sortBy === 'rating') {
                    return b.rating - a.rating;
                }
                return 0;
            });
        }

        setFilteredParticipants(updatedList);
    }, [searchTerm, sortBy, participants]);

    return (
        <div>
            {/* Navbar at the top */}
            <Navbar />

            <div className="tournament-detail-container">
                {/* Tournament Header Container */}
                <div className="tournament-header-container">
                    <div className="header-left">
                        {/* Back Button and Tournament Header */}
                        <button className="back-button">{'<'}</button>
                        <h1 className="tournament-title">Tournament 1</h1>
                    </div>

                    {/* Registration and Player Count */}
                    <div className="header-right">
                        <div className="registration">
                            <button className={isRegistered ? 'registered-button' : 'register-button'} onClick={handleRegister}>
                                {isRegistered ? 'Registered' : 'Register'}
                            </button>
                            <div className="players-count">
                                <span className="players-label">Players:</span>
                                <span className="players-number">{filteredParticipants.length}</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Tournament Info Boxes */}
                <div className="tournament-info">
                    <div className="info-box">
                        <div className="info-text-container">
                            <span className="icon">🕒</span>
                            <span className="info-value">Jul 21 - Jul 28</span>
                        </div>
                    </div>
                    <div className="info-box">
                        <span className="icon">💰</span>
                        <div className="info-text-container">
                            <span className="info-value">$50,000</span>
                            <span className="info-subtext">Total prize pool</span>
                        </div>
                    </div>
                    <div className="info-box">
                        <span className="icon">✔️</span>
                        <div className="info-text-container">
                            <span className="info-value">50</span>
                            <span className="info-subtext">Available slots</span>
                        </div>
                    </div>
                </div>


                {/* Subtask Bar */}
                <div className="subtask-bar">
                    <button className="subtask-button active">Overview</button>
                    <button className="subtask-button">Participants</button>
                    <button className="subtask-button">Games</button>
                    <button className="subtask-button">Results</button>
                    <button className="subtask-button">Discussion</button>
                </div>
            </div>

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
                        <img src={SearchIconPath} alt="Search Icon" className="search-icon" />
                    </div>
                    <div className="buttons-container">
                        {/* Filter Button */}
                        <button className="filter-button">
                            <img src={FilterIconPath} alt="Filter Icon" className="filter-icon" />
                            <span>Filter</span>
                        </button>

                        {/* Order By Button */}
                        <button className="order-button">
                            Order By
                            <span className="arrow">▼</span>
                        </button>
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
    );
};

export default UserTournamentParticipants;
