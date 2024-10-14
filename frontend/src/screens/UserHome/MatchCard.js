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
            console.log(response.data);
        } catch (error) {
            console.error('Error fetching match:', error);
        }
    };

    useEffect(() => {
        fetchMatch();
    }, []);

    const handleViewGamesHistory = () => {
        navigate('/user/home');
    };

    const formatDateTime = (dateString) => {
        const date = new Date(dateString);
    
        // Format the date
        const dateOptions = { month: 'short', day: 'numeric' };
        const formattedDate = date.toLocaleDateString('en-US', dateOptions);
    
        // Format the time
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
                <h4 className="match-title">Summer cup (Round 1)</h4>
                <h6>{formatDateTime(match.matchDate)}</h6>
                <br></br>
                <div className="match-details">
                    <div className="player">
                    <div className="player-icon"></div>
                    <p className="player-name">John</p>
                    <p className="player-country">USA</p>
                    </div>
                    <div className="match-score">
                    <span className="score">1</span>
                    <span className="dash">-</span>
                    <span className="score">0</span>
                    </div>
                    <div className="player">
                    <div className="player-icon"></div>
                    <p className="player-name">Mak</p>
                    <p className="player-country">SIN</p>
                    </div>
                </div>
            </div>
            <br></br>
            <div onClick={ handleViewGamesHistory } className='games-history'>
                <h6>View Tournament Details</h6>
            </div>
        </div>
    );
};

export default MatchCard;