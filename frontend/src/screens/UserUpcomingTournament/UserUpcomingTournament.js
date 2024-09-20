import React from "react";
import './UserUpcomingTournament.css';
import filterIcon from '../../assets/images/Adjust.png';
import searchIcon from '../../assets/images/Search.png';
import Navbar from '../../components/navbar/Navbar';

import { useState, useEffect } from 'react';

// Example API URL where tournaments are fetched
export const API_URL = 'http://your-api-url/tournaments';

const UserUpcomingTournament = () => {
    const [activeTab, setActiveTab] = useState('upcoming');

    
    const [tournaments, setTournaments] = useState([
        {
            id: 1,
            name: 'Youth Chess Championships 2024',
            date: 'Jul 27 - Jul 29, 2024',
            location: 'Singapore',
            slots: 30,
            prize: '$50,000',
            status: 'Open registration',
        },
        {
            id: 2,
            name: 'Sants Open 2024',
            date: 'Aug 23 - Sep 02, 2024',
            location: 'Barcelona, Spain',
            slots: 100,
            prize: '$100,000',
            status: 'Published',
        },
        {
            id: 3,
            name: '45th FIDE Chess Olympiad 2024',
            date: 'Sep 10 - Sep 24, 2024',
            location: 'Budapest, Hungary',
            slots: 250,
            prize: '$20,000',
            status: 'Registered',
        },
        {
            id: 4,
            name: 'Grand Chess Tour 2024',
            date: 'Sep 16 - Sep 28, 2024',
            location: 'London, UK',
            slots: 75,
            prize: '$50,000',
            status: 'Registration closed',
        },
        
    ]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const [filteredTournaments, setFilteredTournaments] = useState([]);

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
                            onChange={handleSearch} 
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
                            <th>Prize</th>
                            <th>Status</th>
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
                                <td>{tournament.prize}</td>
                                <td className={`status-${tournament.status.replace(/\s+/g, '-').toLowerCase()}`}>
                                    {tournament.status}
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

export default UserUpcomingTournament;
