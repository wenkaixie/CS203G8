import React, { useState, useEffect } from 'react';
import './MyTournamentsTable.css';
import { useNavigate } from 'react-router-dom';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import { getAuth } from "firebase/auth";

const MyTournamentsTable = ({upcomingTournaments, ongoingTournaments}) => {
    const [ongoingButton, setOngoingButton] = useState(true);
    const [upcomingButton, setUpcomingButton] = useState(false);
    const [sortedTournaments, setSortedTournaments] = useState(null); // Initially null
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [sortBy, setSortBy] = useState('');
    const [currentPage, setCurrentPage] = useState(1); // Track the current page
    const tournamentsPerPage = 5; // Define how many tournaments to show per page
    const [tournaments, setTournaments] = useState([]);

    const navigate = useNavigate();
    const auth = getAuth();

    // Set tournaments to ongoingTournaments by default when it becomes available
    useEffect(() => {
        if (ongoingTournaments && ongoingTournaments.length > 0) {
            setTournaments(ongoingTournaments);
        }
    }, [ongoingTournaments]);

    const fetchOngoingTournaments = () => {
        setTournaments(ongoingTournaments);
    }

    const fetchUpcomingTournaments = () => {
        setTournaments(upcomingTournaments);
    }

    const handleOngoingUpcomingButtonChange = (type) => {
        setSortBy('');
        setSortedTournaments(null);
        setCurrentPage(1);
        if (type === 'ongoing') {
            setOngoingButton(true);
            setUpcomingButton(false);
            fetchOngoingTournaments();
        } else if (type === 'upcoming') {
            setOngoingButton(false);
            setUpcomingButton(true);
            fetchUpcomingTournaments();
        }
    };

    const handleRowClick = (tournamentId) => {
        navigate(`/admin/tournament/${tournamentId}/overview`);
    };

    const handleViewMyTournaments = () => {
        navigate(`/admin/tournaments`);
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
                <h2 className="tournament-title">My Tournaments</h2>
                <div className="filter-tabs">
                    <div className='ongoing-upcoming-buttons'>
                        <button onClick={() => handleOngoingUpcomingButtonChange('ongoing')} className={`ongoing-upcoming-button tab ${ongoingButton ? 'active' : ''}`}>
                            Ongoing
                        </button>
                        <button onClick={() => handleOngoingUpcomingButtonChange('upcoming')} className={`ongoing-upcoming-button tab ${upcomingButton ? 'active' : ''}`}>
                            Upcoming
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
            <h2 className="tournament-title">My Tournaments</h2>
            <div className="filter-tabs">
                <div className='ongoing-upcoming-buttons'>
                    <button onClick={() => handleOngoingUpcomingButtonChange('ongoing')} className={`ongoing-upcoming-button tab ${ongoingButton ? 'active' : ''}`}>
                        Ongoing
                    </button>
                    <button onClick={() => handleOngoingUpcomingButtonChange('upcoming')} className={`ongoing-upcoming-button tab ${upcomingButton ? 'active' : ''}`}>
                        Upcoming
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

                    return (
                        <tr key={tournament.tid} onClick={() => handleRowClick(tournament.tid)} className="clickable-row">
                            <td>{indexOfFirstTournament + index + 1}</td> {/* Display correct numbering */}
                            <td>{tournament.name}</td>
                            <td>{formatDate(tournament.startDatetime)} - {formatDate(tournament.endDatetime)}</td>
                            <td>{tournament.location}</td>
                            <td>{tournament.capacity}</td>
                            <td className={`status-${tournament.status.toLowerCase()}`}>{tournament.status}</td>
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

            <div className="show-more-text" onClick={handleViewMyTournaments}>
                View My Tournaments
            </div>
        </div>
    );
};

export default MyTournamentsTable;