import React, { useState, useEffect } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom'; 
import { getAuth } from 'firebase/auth';
import './UserDetailsHeader.css';
import Navbar from '../../components/navbar/Navbar';
import RegistrationForm from './RegistrationForm';

const UserDetailsHeader = ({ userElo }) => {
    const { tournamentId } = useParams(); // Dynamically get tournamentId from URL parameters
    const location = useLocation(); // Track the current route
    const navigate = useNavigate(); // Hook to handle navigation

    const [activeTab, setActiveTab] = useState('overview'); // Track active tab
    const [isRegistered, setIsRegistered] = useState(false); 
    const [showRegistrationForm, setShowRegistrationForm] = useState(false); 
    const [tournamentData, setTournamentData] = useState({});
    const [numberOfPlayers, setNumberOfPlayers] = useState(0);
    const [isEligible, setIsEligible] = useState(false); 
    const [registrationError, setRegistrationError] = useState(''); 
    const [userUid, setUserUid] = useState(null); 

    // Update activeTab based on the current URL path
    useEffect(() => {
        const path = location.pathname.split('/').pop(); // Get the current tab from the URL
        setActiveTab(path); // Set it as the active tab
    }, [location.pathname]);

    // Fetch tournament details and current user UID
    useEffect(() => {
        const auth = getAuth();
        const user = auth.currentUser;

        if (user) {
            setUserUid(user.uid); 

            const fetchTournamentData = async () => {
                try {
                    const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}`);
                    if (!response.ok) throw new Error('Failed to fetch tournament data');

                    const data = await response.json();
                    setTournamentData(data);
                    setNumberOfPlayers(data.users ? data.users.length : 0); 

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

    const availableSlots = tournamentData.capacity 
        ? tournamentData.capacity - numberOfPlayers 
        : 0;

    const handleRegisterClick = () => {
        if (isEligible && !isRegistered) {
            setShowRegistrationForm(true);
        }
    };

    const closeForm = () => {
        setShowRegistrationForm(false);
        setRegistrationError('');
    };

    const handleFormSubmit = async (userDetails) => {
        try {
            const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}/users`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    uid: userUid,
                    fullName: userDetails.fullName,
                    age: userDetails.age,
                    location: userDetails.location,
                }),
            });

            if (!response.ok) throw new Error('Failed to register. Please try again.');

            setIsRegistered(true);
            setNumberOfPlayers((prev) => prev + 1);
            setShowRegistrationForm(false);
        } catch (error) {
            setRegistrationError(error.message);
            setIsRegistered(false);
        }
    };

    const handleTabNavigation = (tab) => {
        navigate(`/tournament/${tournamentId}/${tab}`);
    };

    return (
        <div className="header-wrapper">
            <Navbar />

            <div className="header-container">
                <div className="header-left">
                    <button className="back-button" onClick={() => navigate('/user/home')}>
                        {'<'}
                    </button>
                    <h1 className="tournament-title">
                        {tournamentData.name || 'Tournament'}
                    </h1>
                </div>
                <div className="registration-container">
                    <button
                        className={isRegistered ? 'registered-button' : 'register-button'}
                        onClick={handleRegisterClick}
                        disabled={!isEligible || isRegistered}
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
                            {tournamentData.startDatetime 
                                ? new Date(tournamentData.startDatetime).toLocaleDateString('en-US', {
                                      year: 'numeric', month: 'short', day: 'numeric',
                                  }) 
                                : 'N/A'}
                            {' - '}
                            {tournamentData.endDatetime 
                                ? new Date(tournamentData.endDatetime).toLocaleDateString('en-US', {
                                      year: 'numeric', month: 'short', day: 'numeric',
                                  }) 
                                : 'N/A'}
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
                        <span className="info-text">
                            {availableSlots >= 0 ? availableSlots : 'N/A'}
                        </span>
                        <span className="sub-text">Available slots</span>
                    </div>
                </div>
            </div>

            <div className="banner">
                <ul className="subtabs">
                    {['overview', 'participants', 'games', 'discussion'].map((tab) => (
                        <li
                            key={tab}
                            className={`subtab-item ${activeTab === tab ? 'active' : ''}`}
                            onClick={() => handleTabNavigation(tab)}
                        >
                            {tab.charAt(0).toUpperCase() + tab.slice(1)}
                        </li>
                    ))}
                </ul>
            </div>

            {showRegistrationForm && (
                <RegistrationForm
                    closeForm={closeForm}
                    onSubmit={handleFormSubmit}
                    tournamentID={tournamentId}
                />
            )}
        </div>
    );
};

export default UserDetailsHeader;
