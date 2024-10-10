import React, { useState } from 'react';
import './RegistrationForm.css';
import LockIcon from '../../assets/images/Password.png';

const RegistrationForm = ({ tournamentID, closeForm, onSubmit }) => {
    const [fullName, setFullName] = useState('John Tan Choon Hui'); // Pre-filled dummy data
    const [age, setAge] = useState(23); // Pre-filled dummy data
    const [location, setLocation] = useState('Singapore'); // Pre-filled dummy data
    const [email, setEmail] = useState('JohnTanCH@gmail.com'); // Pre-filled dummy data

    // Function to register user for the tournament
    const registerUser = async (tournamentID, userDetails) => {
        try {
            const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentID}/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userDetails),
            });

            if (!response.ok) {
                throw new Error('Failed to register user');
            }

            const data = await response.json();
            console.log('User registered successfully:', data);
            alert('Registration successful!');
        } catch (error) {
            console.error('Error registering user:', error);
            alert('Failed to register. Please try again.');
        }
    };

    // Handle form submission
    const handleSubmit = () => {
        const userDetails = { fullName, age, location, email };
        registerUser(tournamentID, userDetails);
        onSubmit(userDetails);
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
                </div>
                <div className="registration-footer">
                    <button 
                        className="registration-button" 
                        onClick={handleSubmit}
                    >
                        Register
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RegistrationForm;
