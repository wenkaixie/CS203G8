import React from 'react';
import './MatchCard.css';

const MatchCard = () => {
    return (
        <div className="match-card">
            <h2 className="match-title">Summer cup (Round 1)</h2>
            <div className="match-details">
                <div className="player">
                <div className="player-icon"></div>
                <p className="player-name">John</p>
                <p className="player-country">USA</p>
                </div>
                <div className="match-score">
                <span className="score">5</span>
                <span className="dash">-</span>
                <span className="score">2</span>
                </div>
                <div className="player">
                <div className="player-icon"></div>
                <p className="player-name">Mak</p>
                <p className="player-country">SIN</p>
                </div>
            </div>
            <div className="match-time">7.30pm</div>
        </div>
    );
};

export default MatchCard;