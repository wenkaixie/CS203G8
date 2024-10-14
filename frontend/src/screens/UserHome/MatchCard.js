import React, { useState, useEffect } from 'react';
import './MatchCard.css';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { getAuth } from "firebase/auth";

const MatchCard = () => {
    const navigate = useNavigate();
    const auth = getAuth();
    const [match, setMatch] = useState(null);

    const fetchMatch = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/rounds/latest/${auth.currentUser.uid}`);
            setMatch(response.data);
        } catch (error) {
            console.error('Error fetching match:', error);
        }
    };

    useEffect(() => {
        fetchMatch();
    }, []);

    const handleViewTournament = (tournamentId) => {
        navigate(`/user/tournament/${tournamentId}/games`);
    };

    const formatDateTime = (dateString) => {
        const date = new Date(dateString);
    
        const dateOptions = { month: 'short', day: 'numeric' };
        const formattedDate = date.toLocaleDateString('en-US', dateOptions);
    
        const timeOptions = { hour: 'numeric', minute: 'numeric', hour12: true };
        const formattedTime = date.toLocaleTimeString('en-US', timeOptions).toLowerCase();
    
        return `${formattedDate}, ${formattedTime}`;
    };

    if (!match) {
        return (
            <div>
                <h2>Recent Match</h2>
                <h5>You have no recent match</h5>
            </div>
        );
    }

    return (
        <div>
            <h3>Recent Match</h3>
            <div className="match-card">
                <h2 className="match-title">{match.tournamentName} (Round {match.roundName})</h2>
                <h5>{formatDateTime(match.matchDate)}</h5>
                <br />
                <div className="match-details">
                    <div className="player">
                        <div className="player-icon"></div>
                        <div className='games-name'>
                            {match.user1isWhite ? (
                                <div className="black-square"></div>
                            ) : (
                                <div className="white-square"></div>
                            )}
                            <p className="player-name">{match.uid1Name}</p>
                        </div>
                        <p className="player-country">{match.uid1Nationality}</p>
                    </div>
                    <div className="match-score">
                        <span className="score">{match.user1Score}</span>
                        <span className="dash">-</span>
                        <span className="score">{match.user2Score}</span>
                    </div>
                    <div className="player">
                        <div className="player-icon"></div>
                        <div className='games-name'>
                            {match.user1isWhite ? (
                                <div className="white-square"></div>
                            ) : (
                                <div className="black-square"></div>
                            )}
                            <p className="player-name">{match.uid2Name}</p>
                        </div>
                        <p className="player-country">{match.uid2Nationality}</p>
                    </div>
                </div>
            </div>
            <br />
            <div onClick={() => handleViewTournament(match.tournamentId)} className='games-history'>
                <h6>View Tournament Details</h6>
            </div>
        </div>
    );
};

export default MatchCard;