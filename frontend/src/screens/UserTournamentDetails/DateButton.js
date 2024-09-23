import React from 'react';
import './DateButton.css';

const DateButton = ({ date, isSelected, onClick }) => {
    return (
        <button 
            className={`date-button ${isSelected ? 'selected' : ''}`} 
            onClick={onClick}
        >
            {date}
        </button>
    );
};

export default DateButton;
