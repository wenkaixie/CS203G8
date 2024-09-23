import React, { useState } from 'react';
import './RegistrationForm.css';
import DateButton from './DateButton'; 
import LockIcon from '../../assets/images/Password.png';

const RegistrationForm = ({ closeForm, onSubmit }) => {
    const [fullName, setFullName] = useState('John Tan Choon Hui'); // Pre-filled dummy data
    const [age, setAge] = useState(23); // Pre-filled dummy data
    const [location, setLocation] = useState('Singapore'); // Pre-filled dummy data
    const [email, setEmail] = useState('JohnTanCH@gmail.com'); // Pre-filled dummy data
    const [selectedDates, setSelectedDates] = useState([]); // No dates selected initially

    const dates = [
        'Jun 17, 2024',
        'Jun 18, 2024',
        'Jun 19, 2024'
    ]; // Dummy data, will be replaced by database values

    // Toggle selected date
    const toggleDate = (date) => {
        if (selectedDates.includes(date)) {
            setSelectedDates(selectedDates.filter(d => d !== date));
        } else {
            setSelectedDates([...selectedDates, date]);
        }
    };

    // Handle form submission
    const handleSubmit = () => {
        // Perform form validation here
        onSubmit({ fullName, age, location, email, selectedDates });
        closeForm();
    };

    return (
        <div className="registration-overlay" onClick={closeForm}>
            {/* Click on overlay to close */}
            <div className="registration-modal" onClick={(e) => e.stopPropagation()}>
                {/* Prevent closing when clicking inside modal */}
                <div className="registration-header">
                    <h2 className="registration-title">Registration</h2>
                    <button className="close-button" onClick={closeForm}>×</button>
                    <div className="header-underline"></div>
                </div>
                <div className="registration-body">
                    <div className="form-group">
                        <div className="form-group">
                            <label className="verify-label">Verify your personal details</label>
                        </div>

                        <div className="input-group">
                            <label>Full name</label>
                            <input 
                                type="text" 
                                value={fullName} 
                                onChange={(e) => setFullName(e.target.value)} 
                                placeholder="Enter your full name"
                            />
                        </div>
                        <div className="input-row">
                            <div className="input-group">
                                <label>Age</label>
                                <input 
                                    type="number" 
                                    value={age} 
                                    onChange={(e) => setAge(e.target.value)} 
                                    placeholder="Enter your age"
                                />
                            </div>
                            <div className="input-group">
                                <label>Location</label>
                                <input 
                                    type="text" 
                                    value={location} 
                                    onChange={(e) => setLocation(e.target.value)} 
                                    placeholder="Enter your location"
                                />
                            </div>
                        </div>
                        <div className="input-group input-with-icon">
                            <label>Email</label>
                            <input 
                                type="email" 
                                value={email} 
                                readOnly // Make the input uneditable
                            />
                            <img src={LockIcon} alt="Lock Icon" className="lock-icon" />
                        </div>
                    </div>

                    <div className="form-group">
                        <label>Choose your preferred game dates (at least 1)</label>
                        <div className="date-buttons">
                            {dates.map((date) => (
                                <DateButton 
                                    key={date}
                                    date={date}
                                    isSelected={selectedDates.includes(date)}
                                    onClick={() => toggleDate(date)}
                                />
                            ))}
                        </div>
                    </div>
                </div>
                <div className="registration-footer">
                    <button 
                        className="registration-button" 
                        onClick={handleSubmit}
                        disabled={selectedDates.length === 0} // Disable if no date is selected
                    >
                        Register
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RegistrationForm;
