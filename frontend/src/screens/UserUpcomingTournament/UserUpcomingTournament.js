import React, { useState, useEffect } from "react";
import './UserUpcomingTournament.css';
import filterIcon from '../../assets/images/Adjust.png';
import searchIcon from '../../assets/images/Search.png';
import Navbar from '../../components/navbar/Navbar';

import { collection, getDocs } from 'firebase/firestore';
import { FirestoreDB } from '../../firebase/firebase_config';  // Import the Firestore config

const UserUpcomingTournament = () => {
    const [activeTab, setActiveTab] = useState('upcoming');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);

    const [tournaments, setTournaments] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const [filteredTournaments, setFilteredTournaments] = useState([]);

    // Fetch tournaments from Firestore
    useEffect(() => {
        const fetchTournaments = async () => {
            try {
                const tournamentSnapshot = await getDocs(collection(FirestoreDB, "tournaments"));
                const tournamentList = tournamentSnapshot.docs.map(doc => ({
                    ...doc.data(),
                    id: doc.id
                }));
                setTournaments(tournamentList);
                setFilteredTournaments(tournamentList); // Initially set the filtered list
            } catch (error) {
                console.error("Error fetching tournaments: ", error);
            }
        };

        fetchTournaments();
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

    // Handle filter option
    const handleFilterChange = (e) => {
        setFilterStatus(e.target.value);
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
                if (sortBy === 'name') {
                    return a.name.localeCompare(b.name);
                } else if (sortBy === 'date') {
                    return new Date(a.startDatetime.toDate()) - new Date(b.startDatetime.toDate());
                } else if (sortBy === 'slots') {
                    return b.capacity - a.capacity;
                } else if (sortBy === 'prize') {
                    return parseFloat(b.prize.replace('$', '')) - parseFloat(a.prize.replace('$', ''));
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
                                    <div className="dropdown-item" onClick={() => handleSortChange('date')}>
                                        Date
                                    </div>
                                    <div className="dropdown-item" onClick={() => handleSortChange('slots')}>
                                        Slots
                                    </div>
                                    <div className="dropdown-item" onClick={() => handleSortChange('prize')}>
                                        Prize
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Tournament List */}
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
                                    <td>{new Date(tournament.startDatetime.toDate()).toLocaleString()}</td>
                                    <td>{tournament.location}</td>
                                    <td>{tournament.capacity}</td>
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
