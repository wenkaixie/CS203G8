import React, { useState } from 'react';
import './TournamentsTable.css';
import { useNavigate } from 'react-router-dom';
import filterIcon from '../../assets/images/Adjust.png';

const TournamentsTable = ({ tournaments }) => {
    const [eligibleButton, setEligibleButton] = useState(true);
    const [allButton, setAllButton] = useState(false);
    const [sortedTournaments, setSortedTournaments] = useState(null); // Initially null
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [sortBy, setSortBy] = useState('');

    const navigate = useNavigate();

    const handleEligibleAllButtonChange = () => {
        setEligibleButton(!eligibleButton);
        setAllButton(!allButton);
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
            sortedList.sort((a, b) => a.prizePool - b.prizePool);
        }
        console.log(sortedList);
        setSortedTournaments(sortedList); // Set the sorted tournaments
        setSortBy(criteria);
        setIsDropdownVisible(false);
    };

    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible);
    };

    const handleFilterClick = () => {
        alert('Filter button clicked! This can be implemented based on specific filtering logic.');
    };

    const tournamentsToDisplay = sortedTournaments || tournaments; // Use sortedTournaments if available, otherwise use original tournaments

    return (
        <div className="tournament-container">
            <h2 className="tournament-title">Tournaments</h2>
            <div className="filter-tabs">
                <div className='eligible-all-buttons'>
                    <button onClick={handleEligibleAllButtonChange} className={`eligible-all-button tab ${eligibleButton ? 'active' : ''}`}>
                        Eligible
                    </button>
                    <button onClick={handleEligibleAllButtonChange} className={`eligible-all-button tab ${allButton ? 'active' : ''}`}>
                        All
                    </button>
                </div>

                {/* Buttons Container */}
                <div className="buttons-container">
                    {/* Filter Button */}
                    <button className="filter-button" onClick={handleFilterClick}>
                        <img src={filterIcon} alt="Filter Icon" className="filter-icon" />
                        Filter
                    </button>

                    {/* Order By Dropdown Button */}
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
                    {tournamentsToDisplay.map((tournament, index) => (
                        <tr key={tournament.tid} onClick={() => handleRowClick(tournament.tid)} className='clickable-row'>
                            <td>{index + 1}</td>
                            <td>{tournament.name}</td>
                            <td>{formatDate(tournament.startDatetime)} - {formatDate(tournament.endDatetime)}</td>
                            <td>{tournament.location}</td>
                            <td>{tournament.capacity}</td>
                            <td>{tournament.status || 'empty'}</td>
                            <td>{tournament.prizePool}</td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <div className="pagination">
                <span>&lt;</span>
                <span className="page-number">1</span>
                <span className="page-number">2</span>
                <span className="page-number">3</span>
                <span>…</span>
                <span className="page-number">8</span>
                <span>&gt;</span>
            </div>
            <div className="show-more" onClick={handleViewAllTournaments}>View All Tournaments</div>
        </div>
    );
};

export default TournamentsTable;