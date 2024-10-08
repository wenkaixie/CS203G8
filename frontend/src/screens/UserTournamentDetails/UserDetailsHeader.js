import React, { useState, useEffect } from 'react';
import { NavLink, useParams, useLocation } from 'react-router-dom'; 
import './UserDetailsHeader.css';
import Navbar from '../../components/navbar/Navbar';
import RegistrationForm from './RegistrationForm';

const UserDetailsHeader = ({ tournamentTitle = "Tournament 1" }) => {
    const { tournamentId } = useParams(); // Dynamically get tournamentId from URL parameters
    const location = useLocation(); // Get the current location (URL)

    const [isRegistered, setIsRegistered] = useState(false); 
    const [showRegistrationForm, setShowRegistrationForm] = useState(false); 
    const [totalParticipants, setTotalParticipants] = useState(30); 
    const [numberOfPlayers, setNumberOfPlayers] = useState(5); 

    // Define activeTab based on the last part of the current URL
    const activeTab = location.pathname.split('/').pop(); // Get the last part of the URL (e.g., 'overview', 'participants')

    useEffect(() => {
        // Simulated API response for total participants and registered players
        const fetchTournamentData = async () => {
            try {
                const totalSlots = 30; // Replace with real API call
                const registeredPlayers = 5; // Replace with real API call
                setTotalParticipants(totalSlots);
                setNumberOfPlayers(registeredPlayers);
            } catch (error) {
                console.error("Failed to fetch tournament data:", error);
            }
        };
        fetchTournamentData();
    }, []);

    const availableSlots = totalParticipants - numberOfPlayers;

    const handleRegisterClick = () => {
        if (!isRegistered) {
            setShowRegistrationForm(true);
        }
    };

    const closeForm = () => {
        setShowRegistrationForm(false);
    };

    const handleFormSubmit = (data) => {
        setIsRegistered(true);
        setNumberOfPlayers(numberOfPlayers + 1);
        setShowRegistrationForm(false);
    };

    return (
        <div className="header-wrapper">
            <Navbar />

            <div className="header-container">
                <div className="header-left">
                    <button className="back-button" onClick={() => window.history.back()}>{'<'}</button>
                    <h1 className="tournament-title">{tournamentTitle}</h1>
                </div>
                <div className="registration-container">
                    <button
                        className={isRegistered ? 'registered-button' : 'register-button'}
                        onClick={handleRegisterClick}
                        disabled={isRegistered}
                    >
                        {isRegistered ? 'Registered' : 'Register'}
                    </button>
                    <div className="players-count">
                        <span>Players:</span>
                        <span>{numberOfPlayers}</span>
                    </div>
                </div>
            </div>

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
                        <span className="info-text">{availableSlots}</span>
                        <span className="sub-text">Available slots</span>
                    </div>
                </div>
            </div>

            {/* Updated Banner */}
            <div className="banner">
                <ul className="subtabs">
                    <li className={`subtab-item ${activeTab === 'overview' ? 'active' : ''}`}>
                        <NavLink to={`/tournament/${tournamentId}/overview`}>
                            Overview
                        </NavLink>
                    </li>
                    <li className={`subtab-item ${activeTab === 'participants' ? 'active' : ''}`}>
                        <NavLink to={`/tournament/${tournamentId}/participants`}>
                            Participants
                        </NavLink>
                    </li>
                    <li className={`subtab-item ${activeTab === 'games' ? 'active' : ''}`}>
                        <NavLink to={`/tournament/${tournamentId}/games`}>
                            Games
                        </NavLink>
                    </li>
                    <li className={`subtab-item ${activeTab === 'discussion' ? 'active' : ''}`}>
                        <NavLink to={`/tournament/${tournamentId}/discussion`}>
                            Discussion
                        </NavLink>
                    </li>
                </ul>
            </div>

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
