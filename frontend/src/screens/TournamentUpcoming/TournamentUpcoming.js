import React from "react";
import './TournamentUpcoming.css';
import filterIcon from '../../assets/images/Adjust.png';
import searchIcon from '../../assets/images/Search.png';
import Navbar from '../../components/navbar/Navbar';

import { useState, useEffect } from 'react';

// Example API URL where tournaments are fetched
export const API_URL = 'http://your-api-url/tournaments';

const TournamentUpcoming = () => {
    const [activeTab, setActiveTab] = useState('upcoming');
    const [tournaments, setTournaments] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const [filteredTournaments, setFilteredTournaments] = useState([]);

    // Fetch tournaments from API
    useEffect(() => {
        fetch(API_URL)
            .then(response => response.json())
            .then(data => setTournaments(data))
            .catch(err => console.error(err));
    }, []);

    // Filter tournaments based on active tab
    useEffect(() => {
        let filteredList = tournaments;
        if (activeTab !== '') {
            filteredList = tournaments.filter(tournament => tournament.status === activeTab);
        }
        setFilteredTournaments(filteredList);
    }, [activeTab, tournaments]);

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

    // Filter and sort the tournament list based on user inputs
    useEffect(() => {
        let updatedList = tournaments;

        // Filter by status
        if (filterStatus) {
            updatedList = updatedList.filter(tournament => tournament.status === filterStatus);
        }

        // Filter by search term (tournament name)
        if (searchTerm) {
            updatedList = updatedList.filter(tournament =>
                tournament.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // Sort the list
        if (sortBy) {
            updatedList = updatedList.sort((a, b) => {
                if (sortBy === 'date') {
                    return new Date(a.date) - new Date(b.date);
                } else if (sortBy === 'prize') {
                    return b.prize - a.prize;
                }
                return 0;
            });
        }

        setFilteredTournaments(updatedList);
    }, [searchTerm, sortBy, filterStatus, tournaments]);

    // Handle save option
    const handleSave = (tournamentId) => {
        // Logic to save a tournament (could be a POST request to an API)
        console.log(`Tournament ${tournamentId} saved!`);
    };

    return (
        <div>
            {/* Navbar at the top */}
            <Navbar />

            {/* Tournament Content */}
            <div className="tournament-upcoming">

                {/* Container for Header and Subtask Bar */}
                <div className="header-subtask-container">
                    {/* Page Header */}
                    <h1>Tournament</h1>

                    {/* Subtask Bar */}
                    <div className="subtask-bar">
                        <button
                            className={`subtask-button ${activeTab === 'upcoming' ? 'active' : ''}`}
                            onClick={() => setActiveTab('upcoming')}
                        >
                            Upcoming
                        </button>
                        <button
                            className={`subtask-button ${activeTab === 'ongoing' ? 'active' : ''}`}
                            onClick={() => setActiveTab('ongoing')}
                        >
                            Ongoing
                        </button>
                        <button
                            className={`subtask-button ${activeTab === 'past' ? 'active' : ''}`}
                            onClick={() => setActiveTab('past')}
                        >
                            Past
                        </button>
                    </div>
                </div>


                {/* Search and Filter Controls */}
                <div className="controls-container">
                    {/* Search Bar */}
                    <div className="search-bar">
                        <input
                            type="text"
                            placeholder="Search for a tournament"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                        <img src={searchIcon} alt="Search Icon" className="search-icon" />
                    </div>

                    {/* Buttons Container */}
                    <div className="buttons-container">
                        {/* Filter Button */}
                        <button className="filter-button">
                            <img src={filterIcon} alt="Filter Icon" className="filter-icon" />
                            Filter
                        </button>

                        {/* Order By Button */}
                        <button className="order-button">
                            Order By
                        </button>
                    </div>
                </div>

                <div className="tournament-list-container">
                <table className="tournament-table">
                    <thead>
                        <tr>
                            <th>No</th>
                            <th>Name</th>
                            <th>Date</th>
                            <th>Location</th>
                            <th>Slots</th>
                            <th>Status</th>
                            <th>Save</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredTournaments.map((tournament, index) => (
                            <tr key={tournament.id}>
                                <td>{index + 1}</td>
                                <td>{tournament.name}</td>
                                <td>{tournament.date}</td>
                                <td>{tournament.location}</td>
                                <td>{tournament.slots}</td>
                                <td className={`status-${tournament.status.replace(/\s+/g, '-').toLowerCase()}`}>
                                    {tournament.status}
                                </td>
                                <td className="save-icon">
                                    <button onClick={() => handleSave(tournament.id)}>Save</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            </div>
        </div>
    );
};

export default TournamentUpcoming;
