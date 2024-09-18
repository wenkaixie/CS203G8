import React, { useState }  from 'react';
import './TournamentsTable.css';
import { useNavigate } from 'react-router-dom';

const TournamentsTable = () => {
    const [eligibleButton, setEligibleButton] = useState(true);
    const [allButton, setAllButton] = useState(false);

    const navigate = useNavigate();

    const handleEligibleAllButtonChange = (event) => {
        setEligibleButton(!eligibleButton);
        setAllButton(!allButton);
    }

    const handleRowClick = (event) => {
        // need to pass in tournamentID as well
        navigate('/user/home'); // replace with tournament-details page and include tournamentID
    }

    const tournaments = [
        { no: 1, name: 'Youth Chess Championships 2024', date: 'Jul 27 - Jul 29', location: 'Singapore', slots: 30, status: 'Registered' },
        { no: 2, name: 'Sants Open 2024', date: 'Aug 23 - Sep 02', location: 'Barcelona, Spain', slots: 100, status: 'Open registration' },
        { no: 3, name: '45th FIDE Chess Olympiad 2024', date: 'Sep 10 - Sep 24', location: 'Budapest, Hungary', slots: 250, status: 'Published' },
        { no: 4, name: 'Grand Chess Tour 2024', date: 'Sep 16 - Sep 28', location: 'London, UK', slots: 75, status: 'Published' },
        { no: 5, name: 'Grand Chess Tour 2024', date: 'Sep 16 - Sep 28', location: 'London, UK', slots: 75, status: 'Published' },
        { no: 6, name: 'Grand Chess Tour 2024', date: 'Sep 16 - Sep 28', location: 'London, UK', slots: 75, status: 'Published' },
        { no: 7, name: 'Grand Chess Tour 2024', date: 'Sep 16 - Sep 28', location: 'London, UK', slots: 75, status: 'Published' },
    ];

    return (
        <div className="tournament-container">
            <h2 className="tournament-title">Tournaments</h2>
            <div className="filter-tabs">
                <div className='eligible-all-buttons'>
                    <button onClick={ handleEligibleAllButtonChange } className={`tab ${eligibleButton === true ? 'active' : ''}`}>Eligible</button>
                    <button onClick={ handleEligibleAllButtonChange } className={`tab ${allButton === true ? 'active' : ''}`}>All</button>
                </div>
                <div className="filter-buttons">
                <button className="filter-icon">🔧</button>
                <button className="filter-icon">🕒</button>
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
                {tournaments.map((tournament) => (
                    <tr key={tournament.no} 
                        onClick={() => handleRowClick(tournament.no)} 
                        className='clickable-row'
                    >
                        <td>{tournament.no}</td>
                        <td>{tournament.name}</td>
                        <td>{tournament.date}</td>
                        <td>{tournament.location}</td>
                        <td>{tournament.slots}</td>
                        <td>{tournament.status}</td>
                        <td>🔖</td>
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
            <div className="show-more">Show more</div>
        </div>
    );
};

export default TournamentsTable;