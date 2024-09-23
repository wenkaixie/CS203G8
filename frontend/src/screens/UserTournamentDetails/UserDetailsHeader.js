import React, { useState } from 'react';
import './UserDetailsHeader.css';
import Navbar from '../../components/navbar/Navbar';
import RegistrationForm from './RegistrationForm'; 

const UserDetailsHeader = ({ tournamentTitle, playerCount }) => {
    const [isRegistered, setIsRegistered] = useState(false); // Local state to track registration status
    const [showRegistrationForm, setShowRegistrationForm] = useState(false); // State to manage registration form visibility

    const handleRegisterClick = () => {
        if (!isRegistered) {
            setShowRegistrationForm(true); 
        }
    };

    const closeForm = () => {
        setShowRegistrationForm(false); 
    };

    const handleFormSubmit = (data) => {
        console.log('Form Submitted:', data); // Handle form submission data
        setIsRegistered(true); // Mark as registered after form submission
        setShowRegistrationForm(false); // Close the registration form
    };

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
                        onClick={handleRegisterClick}
                        disabled={isRegistered} // Disable the button if already registered
                    >
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

            {/* Registration Form Modal */}
            {showRegistrationForm && (
                <RegistrationForm 
                    closeForm={closeForm} 
                    onSubmit={handleFormSubmit} 
                />
            )}
        </div>
    );
};

export default UserDetailsHeader;
