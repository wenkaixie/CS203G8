import React, { useState } from 'react';
import './RegistrationForm.css';
import LockIcon from '../../assets/images/Password.png';

const RegistrationForm = ({ closeForm, onSubmit }) => {
    const [fullName, setFullName] = useState('John Tan Choon Hui'); // Pre-filled dummy data
    const [age, setAge] = useState(23); // Pre-filled dummy data
    const [location, setLocation] = useState('Singapore'); // Pre-filled dummy data
    const [email, setEmail] = useState('JohnTanCH@gmail.com'); // Pre-filled dummy data

    // Handle form submission
    const handleSubmit = () => {
        // Perform form validation here
        onSubmit({ fullName, age, location, email });
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
