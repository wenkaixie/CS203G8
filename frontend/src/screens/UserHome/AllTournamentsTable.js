import React, { useState, useEffect } from 'react';
import './AllTournamentsTable.css';
import { useNavigate } from 'react-router-dom';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import axios from 'axios';
import { getAuth } from "firebase/auth";

const AllTournamentsTable = () => {
    const [eligibleButton, setEligibleButton] = useState(true);
    const [allButton, setAllButton] = useState(false);
    const [sortedTournaments, setSortedTournaments] = useState(null); // Initially null
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [sortBy, setSortBy] = useState('');
    const [currentPage, setCurrentPage] = useState(1); // Track the current page
    const tournamentsPerPage = 5; // Define how many tournaments to show per page
    const [tournaments, setTournaments] = useState([]);

    const navigate = useNavigate();
    const auth = getAuth();

    // Fetch eligible upcoming tournaments
    const fetchEligibleUpcomingTournaments = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/tournaments/eligible/${auth.currentUser.uid}`);
            console.log(response.data);
            setTournaments(response.data);
        } catch (error) {
            console.error('Error fetching eligible tournaments:', error);
        }
    };

    // Fetch all upcoming tournaments
    const fetchAllUpcomingTournaments = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/tournaments/all`);
            console.log(response.data);
            setTournaments(response.data);
        } catch (error) {
            console.error('Error fetching all tournaments:', error);
        }
    };

    useEffect(() => {
        fetchEligibleUpcomingTournaments();
    }, []);

    const handleEligibleAllButtonChange = (type) => {
        setSortBy('');
        setSortedTournaments(null);
        setCurrentPage(1);
        if (type === 'eligible') {
            setEligibleButton(true);
            setAllButton(false);
            fetchEligibleUpcomingTournaments();
        } else if (type === 'all') {
            setEligibleButton(false);
            setAllButton(true);
            fetchAllUpcomingTournaments(); 
        }
    };

    const isPlayerRegistered = (tournament) => {
        // Check if current user is in the users list
        if (tournament.users != null && tournament.users.includes(auth.currentUser.uid)) {
            return 'Registered';
        }
    
        return tournament.status;
    };

    const handleRowClick = (tournamentId) => {
        navigate(`/user/tournament/${tournamentId}/overview`);
    };

    const handleViewAllTournaments = () => {
        navigate(`/user/tournaments`);
    }

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const options = { month: 'short', day: 'numeric' };
        return date.toLocaleDateString('en-US', options);
    };

    // Sorting the tournaments
    const handleSortChange = (criteria) => {
        let sortedList = [...tournaments]; // Use the original tournaments array
        if (criteria === 'Name') {
            sortedList.sort((a, b) => a.name.localeCompare(b.name));
        } else if (criteria === 'Date') {
            sortedList.sort((a, b) => new Date(a.startDatetime) - new Date(b.startDatetime));
        } else if (criteria === 'Slots') {
            sortedList.sort((a, b) => a.capacity - b.capacity);
        } else if (criteria === 'Prize') {
            sortedList.sort((a, b) => a.prize - b.prize);
        }
        setSortedTournaments(sortedList); // Set the sorted tournaments
        setSortBy(criteria);
        setIsDropdownVisible(false);
        setCurrentPage(1); // Reset to page 1 after sorting
    };

    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible);
    };

    const tournamentsToDisplay = sortedTournaments || tournaments; // Use sortedTournaments if available, otherwise use original tournaments

    // Handle case when tournaments array is empty or null
    if (!tournamentsToDisplay || tournamentsToDisplay.length === 0) {
        return (
            <div className="tournament-container">
                <h2 className="tournament-title">All Tournaments</h2>
                <div className="filter-tabs">
                    <div className='eligible-all-buttons'>
                        <button onClick={() => handleEligibleAllButtonChange('eligible')} className={`eligible-all-button tab ${eligibleButton ? 'active' : ''}`}>
                            Eligible
                        </button>
                        <button onClick={() => handleEligibleAllButtonChange('all')} className={`eligible-all-button tab ${allButton ? 'active' : ''}`}>
                            All
                        </button>
                    </div>
                </div>
                <div className="no-tournaments">No Tournaments Available</div>
            </div>
        );
    }

    // Pagination logic
    const indexOfLastTournament = currentPage * tournamentsPerPage;
    const indexOfFirstTournament = indexOfLastTournament - tournamentsPerPage;
    const currentTournaments = tournamentsToDisplay.slice(indexOfFirstTournament, indexOfLastTournament);

    const totalPages = Math.ceil(tournamentsToDisplay.length / tournamentsPerPage);

    // Handle page navigation
    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    return (
        <div className="tournament-container">
            <h2 className="tournament-title">All Tournaments</h2>
            <div className="filter-tabs">
                <div className='eligible-all-buttons'>
                    <button onClick={() => handleEligibleAllButtonChange('eligible')} className={`eligible-all-button tab ${eligibleButton ? 'active' : ''}`}>
                        Eligible
                    </button>
                    <button onClick={() => handleEligibleAllButtonChange('all')} className={`eligible-all-button tab ${allButton ? 'active' : ''}`}>
                        All
                    </button>
                </div>

                <div className="buttons-container">
                    <div className="dropdown">
                        <button className="order-button" onClick={toggleDropdown}>
                            Order By {sortBy && `(${sortBy})`}
                        </button>
                        {isDropdownVisible && (
                            <div className="dropdown-content">
                                <div className="dropdown-item" onClick={() => handleSortChange('Name')}>
                                    Name
                                </div>
                                <div className="dropdown-item" onClick={() => handleSortChange('Date')}>
                                    Date
                                </div>
                                <div className="dropdown-item" onClick={() => handleSortChange('Slots')}>
                                    Slots
                                </div>
                                <div className="dropdown-item" onClick={() => handleSortChange('Prize')}>
                                    Prize
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            <table className="tournament-table">
                <thead>
                    <tr>
                        <th>No</th>
                        <th>Name</th>
                        <th>Date</th>
                        <th>Location</th>
                        <th>Slots</th>
                        <th>Status</th>
                        <th>Prize</th>
                    </tr>
                </thead>
                <tbody>
                    {currentTournaments.map((tournament, index) => {
                        let registrationStatus = isPlayerRegistered(tournament); // 'Registered', 'Open', or 'Closed'

                        return (
                            <tr key={tournament.tid} onClick={() => handleRowClick(tournament.tid)} className="clickable-row">
                                <td>{indexOfFirstTournament + index + 1}</td> {/* Display correct numbering */}
                                <td>{tournament.name}</td>
                                <td>{formatDate(tournament.startDatetime)} - {formatDate(tournament.endDatetime)}</td>
                                <td>{tournament.location}</td>
                                <td>{tournament.capacity}</td>
                                <td className={`status-${registrationStatus}`}>{registrationStatus}</td>
                                <td>${tournament.prize}</td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>

            {/* Pagination */}
            <div className="pagination">
                <span>
                    <ArrowBackIosNewIcon 
                        onClick={() => currentPage > 1 && handlePageChange(currentPage - 1)}
                        sx={{cursor:"pointer", fontSize:"20px"}}
                    />
                </span>
                {Array.from({ length: totalPages }, (_, i) => (
                    <span
                        key={i + 1}
                        className={`page-number ${currentPage === i + 1 ? 'active' : ''}`}
                        onClick={() => handlePageChange(i + 1)}
                    >
                        {i + 1}
                    </span>
                ))}
                <span>
                    <ArrowForwardIosIcon
                        onClick={() => currentPage < totalPages && handlePageChange(currentPage + 1)}
                        sx={{cursor:"pointer", fontSize:"20px"}}
                    />
                </span>
            </div>

            <div className="show-more-text" onClick={handleViewAllTournaments}>
                View All Tournaments
            </div>
        </div>
    );
};

export default AllTournamentsTable;