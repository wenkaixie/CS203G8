import React, { useState }  from 'react';
import './TournamentsTable.css';
import { useNavigate } from 'react-router-dom';
import TimerIcon from '@mui/icons-material/Timer';
import TuneIcon from '@mui/icons-material/Tune';
import BookmarkBorderIcon from '@mui/icons-material/BookmarkBorder';

const TournamentsTable = ({ tournaments }) => {
    const [eligibleButton, setEligibleButton] = useState(true);
    const [allButton, setAllButton] = useState(false);

    const navigate = useNavigate();

    const handleEligibleAllButtonChange = (event) => {
        setEligibleButton(!eligibleButton);
        setAllButton(!allButton);
    }

    const handleRowClick = (tournamentId) => {
        // Navigate to the specific tournament overview page using the tournament ID
        navigate(`/user/tournament/${tournamentId}/overview`);
    }

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const options = { month: 'short', day: 'numeric' };
        return date.toLocaleDateString('en-US', options);
    };

    return (
        <div className="tournament-container">
            <h2 className="tournament-title">Tournaments</h2>
            <div className="filter-tabs">
                <div className='eligible-all-buttons'>
                    <button onClick={ handleEligibleAllButtonChange } className={`eligible-all-button tab ${eligibleButton === true ? 'active' : ''}`}>Eligible</button>
                    <button onClick={ handleEligibleAllButtonChange } className={`eligible-all-button tab ${allButton === true ? 'active' : ''}`}>All</button>
                </div>
                <div className="filter-buttons">
                    <button className="filter-icon">
                        <TuneIcon />
                    </button>
                    <button className="filter-icon">
                        <TimerIcon />
                    </button>
                    <button className="order-by">Order By</button>
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
                        <th>Save</th>
                    </tr>
                </thead>
                <tbody>
                    {tournaments.map((tournament, index) => (
                        <tr key={tournament.no} 
                            onClick={() => handleRowClick(tournament.tid)}
                            className='clickable-row'
                        >
                            <td>{index + 1}</td>
                            <td>{tournament.name}</td>
                            <td>{formatDate(tournament.startDatetime)} - {formatDate(tournament.endDatetime)}</td>
                            <td>{tournament.location}</td>
                            <td>{tournament.capacity}</td>
                            <td>empty</td>
                            <td>
                                <BookmarkBorderIcon />
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            <div>
                <div className="pagination">
                    <span>&lt;</span>
                    <span className="page-number">1</span>
                    <span className="page-number">2</span>
                    <span className="page-number">3</span>
                    <span>…</span>
                    <span className="page-number">8</span>
                    <span>&gt;</span>
                </div>
                <div className="show-more">Show more</div>
            </div>
        </div>
    );
};

export default TournamentsTable;