import React from 'react';
import './UserDetailsHeader.css';
import Navbar from '../../components/navbar/Navbar';

const UserDetailsHeader = ({ tournamentTitle, isRegistered, handleRegister, playerCount }) => {
    return (
        <div className="header-wrapper">
            {/* Navbar at the top */}
            <Navbar />

            {/* Tournament Header */}
            <div className="header-container">
                <div className="header-left">
                    <button className="back-button">{'<'}</button>
                    <h1 className="tournament-title">{tournamentTitle}</h1>
                </div>
                <div className="registration-container">
                    {/* Register button */}
                    <button 
                        className={isRegistered ? 'registered-button' : 'register-button'} 
                        onClick={handleRegister}>
                        {isRegistered ? 'Registered' : 'Register'}
                    </button>
                    {/* Players count below the button */}
                    <div className="players-count">
                        <span>Players:</span>
                        <span>{playerCount}</span>
                    </div>
                </div>
            </div>

            {/* Tournament Info Boxes */}
            <div className="tournament-info">
                <div className="info-box">
                    <span className="icon">🕒</span>
                    <div>
                        <span className="info-text">Jul 21 - Jul 28</span>
                    </div>
                </div>
                <div className="info-box">
                    <span className="icon">💰</span>
                    <div>
                        <span className="info-text">$50,000</span>
                        <span className="sub-text">Total prize pool</span>
                    </div>
                </div>
                <div className="info-box">
                    <span className="icon">✔️</span>
                    <div>
                        <span className="info-text">50</span>
                        <span className="sub-text">Available slots</span>
                    </div>
                </div>
            </div>

            {/* Subtabs Banner */}
            <div className="banner">
                <ul className="subtabs">
                    <li className="subtab-item">Overview</li>
                    <li className="subtab-item">Participants</li>
                    <li className="subtab-item">Games</li>
                    <li className="subtab-item">Results</li>
                    <li className="subtab-item">Discussion</li>
                </ul>
            </div>
        </div>
    );
};

export default UserDetailsHeader;
