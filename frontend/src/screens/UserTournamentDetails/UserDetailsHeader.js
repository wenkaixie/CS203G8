import React, { useState, useEffect } from 'react';
import { getAuth } from 'firebase/auth'; // Import Firebase Auth
import { NavLink, useParams, useLocation } from 'react-router-dom'; 
import './UserDetailsHeader.css';
import Navbar from '../../components/navbar/Navbar';
import RegistrationForm from './RegistrationForm';

const UserDetailsHeader = ({ userElo }) => {
    const { tournamentId } = useParams(); 
    const location = useLocation(); 
    const activeTab = location.pathname.split('/').pop(); 

    const [isRegistered, setIsRegistered] = useState(false); 
    const [showRegistrationForm, setShowRegistrationForm] = useState(false); 
    const [tournamentData, setTournamentData] = useState({});
    const [numberOfPlayers, setNumberOfPlayers] = useState(0);
    const [isEligible, setIsEligible] = useState(false); 
    const [registrationError, setRegistrationError] = useState(''); 
    const [userUid, setUserUid] = useState(null); 

    // Fetch tournament details and current user UID
    useEffect(() => {
        const auth = getAuth();
        const user = auth.currentUser;

        if (user) {
            setUserUid(user.uid); 

            // Fetch tournament details from the API
            const fetchTournamentData = async () => {
                try {
                    const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}`);
                    if (!response.ok) {
                        throw new Error('Failed to fetch tournament data');
                    }
                    const data = await response.json();
                    setTournamentData(data);
                    setNumberOfPlayers(data.users ? data.users.length : 0); 

                    // Check if user's ELO meets the requirement
                    if (userElo >= data.eloRequirement) {
                        setIsEligible(true);
                    }
                } catch (error) {
                    console.error("Failed to fetch tournament data:", error);
                }
            };
            fetchTournamentData();
        } else {
            console.error('No user is signed in');
        }
    }, [tournamentId, userElo]);

    const availableSlots = tournamentData.capacity ? tournamentData.capacity - numberOfPlayers : 0;

    const handleRegisterClick = () => {
        if (isEligible && !isRegistered) {
            setShowRegistrationForm(true);
        }
    };

    const closeForm = () => {
        setShowRegistrationForm(false);
        setRegistrationError(''); // Clear any previous errors when the form is closed
    };

    const handleFormSubmit = async (userDetails) => {
        try {
            // Make API call to register the user using UID instead of email
            const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    uid: userUid,  // Use the UID here
                    fullName: userDetails.fullName,
                    age: userDetails.age,
                    location: userDetails.location
                })
            });

            if (!response.ok) {
                throw new Error('Failed to register. Please try again.');
            }

            // If the registration is successful
            setIsRegistered(true);
            setNumberOfPlayers(numberOfPlayers + 1); // Increase the player count by 1
            setShowRegistrationForm(false);
        } catch (error) {
            // If registration fails, show the error message and do not register
            setRegistrationError(error.message);
            setIsRegistered(false); // Ensure the user is not marked as registered
        }
    };

    return (
        <div className="header-wrapper">
            <Navbar />

            <div className="header-container">
                <div className="header-left">
                    <button className="back-button" onClick={() => window.history.back()}>{'<'}</button>
                    <h1 className="tournament-title">{tournamentData.name || "Tournament"}</h1>
                </div>
                <div className="registration-container">
                    <button
                        className={isRegistered ? 'registered-button' : 'register-button'}
                        onClick={handleRegisterClick}
                        disabled={!isEligible || isRegistered} // Disable if not eligible or already registered
                    >
                        {isRegistered ? 'Registered' : isEligible ? 'Register' : 'Not Eligible'}
                    </button>
                    <div className="players-count">
                        <span>Players:</span>
                        <span>{numberOfPlayers}</span>
                    </div>
                </div>
            </div>

            {registrationError && <p className="error-message">{registrationError}</p>}

            <div className="tournament-info">
                <div className="info-box">
                    <span className="icon">🕒</span>
                    <div>
                        <span className="info-text">
                            {tournamentData.startDatetime ? new Date(tournamentData.startDatetime).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' }) : 'N/A'} -  
                            {' '}
                            {tournamentData.endDatetime ? new Date(tournamentData.endDatetime).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' }) : 'N/A'}
                        </span>
                    </div>
                </div>
                <div className="info-box">
                    <span className="icon">💰</span>
                    <div>
                        <span className="info-text">${tournamentData.prize || 'N/A'}</span>
                        <span className="sub-text">Total prize pool</span>
                    </div>
                </div>
                <div className="info-box">
                    <span className="icon">✔️</span>
                    <div>
                        <span className="info-text">{availableSlots >= 0 ? availableSlots : 'N/A'}</span>
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
                    tournamentID={tournamentId} // Pass the tournament ID to the registration form
                />
            )}
        </div>
    );
};

export default UserDetailsHeader;
