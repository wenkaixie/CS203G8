import React, { useState, useEffect } from 'react';
import { getAuth } from 'firebase/auth'; 
import './RegistrationForm.css';
import LockIcon from '../../assets/images/Password.png';


const RegistrationForm = ({ tournamentID, closeForm, onSubmit }) => {
    const [fullName, setFullName] = useState(''); 
    const [age, setAge] = useState(''); 
    const [location, setLocation] = useState(''); 
    const [email, setEmail] = useState(''); 
    const [uid, setUid] = useState(null); // To store the user's UID

    // Fetch the UID and user details when the component mounts
    useEffect(() => {
        const auth = getAuth();
        const user = auth.currentUser;

        if (user) {
            setUid(user.uid); // Set the user's UID

            // Fetch user details using the uid
            const fetchUserDetails = async () => {
                try {
                    const response = await fetch(`http://localhost:8080/user/getUser/${user.uid}`);
                    if (!response.ok) {
                        throw new Error('Failed to fetch user details');
                    }

                    const userData = await response.json();
                    // Set the form fields with the retrieved user data
                    setFullName(userData.fullName || '');
                    setAge(userData.age || '');
                    setLocation(userData.location || '');
                    setEmail(userData.email || '');
                } catch (error) {
                    console.error('Error fetching user details:', error);
                }
            };

            fetchUserDetails();
        } else {
            console.error('No user is signed in.');
        }
    }, []);

    // Function to register user for the tournament
    const registerUser = async (tournamentID, userDetails) => {
        try {
            const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentID}/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userDetails), // Send user details including UID
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
        if (!uid) {
            alert('User not authenticated');
            return;
        }

        // Include UID in userDetails
        const userDetails = { fullName, age, location, email, uid };
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
                        <label className="verify-label">Verify your personal details</label>

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
