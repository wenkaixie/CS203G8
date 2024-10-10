import React, { useState } from 'react';
import './FilterOverlay.css'; // Create a separate CSS file for the filter overlay

const FilterOverlay = ({ isOpen, onClose }) => {
    if (!isOpen) return null;

    return (
        <div className="filter-overlay">
            <div className="filter-content">
                <button className="close-button" onClick={onClose}>X</button>
                <h2>Filters</h2>
                {/* Order By Section */}
                <div className="filter-section">
                    <label>Order By</label>
                    <select>
                        <option value="name">Name</option>
                        <option value="age">Age</option>
                        <option value="worldRank">World Rank</option>
                    </select>
                </div>

                {/* Nationality Section */}
                <div className="filter-section nationality-section">
                    <label>Nationality</label>
                    <div className="nationality-buttons">
                        <button>Germany</button>
                        <button>China</button>
                        <button>United Kingdom</button>
                        <button>USA</button>
                        <button>India</button>
                        <button className="add-nationality-button">+</button>
                    </div>
                </div>

                {/* World Rank Section */}
                <div className="filter-section">
                    <label>World rank</label>
                    <div className="range-input">
                        <input type="number" min="1" max="500" placeholder="Min" />
                        <input type="number" min="1" max="500" placeholder="Max" />
                    </div>
                </div>

                {/* Games Played Section */}
                <div className="filter-section">
                    <label>Games played this season</label>
                    <div className="range-input">
                        <input type="number" min="0" max="35" placeholder="Min" />
                        <input type="number" min="0" max="35" placeholder="Max" />
                    </div>
                </div>

                <button className="apply-button">Apply</button>
            </div>
        </div>
    );
};

export default FilterOverlay;
