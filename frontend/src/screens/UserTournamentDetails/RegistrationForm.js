import React, { useState, useEffect } from 'react';
import { getAuth } from 'firebase/auth';
import axios from 'axios';
import './RegistrationForm.css';
import LockIcon from '../../assets/images/Password.png';

const RegistrationForm = ({ tournamentID, closeForm, onSubmit }) => {
    const [fullName, setFullName] = useState('');
    const [age, setAge] = useState('');
    const [nationality, setNationality] = useState('');
    const [email, setEmail] = useState('');
    const [uid, setUid] = useState(null);
    const [authId, setAuthId] = useState(null);

    useEffect(() => {
        const auth = getAuth();
        const user = auth.currentUser;

        if (user) {
            setUid(user.uid);
            setAuthId(user.uid);

            const fetchUserDetails = async () => {
                try {
                    const response = await axios.get(`http://localhost:9090/user/getUser/${user.uid}`);
                    const userData = response.data;

                    setFullName(userData.name || '');
                    setAge(userData.age || '');
                    setNationality(userData.nationality || '');
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

    const handleSubmit = async () => {
        const userDetails = { authId, uid, name: fullName, dateOfBirth: age, nationality, email };

        try {
            const response = await axios.post(
                `http://localhost:9090/user/registerTournament/${tournamentID}`,
                userDetails
            );

            alert(response.data);
            onSubmit(userDetails);
            closeForm();
        } catch (error) {
            console.error('Error registering user:', error);
            alert('Failed to register. Please try again.');
        }
    };

    return (
        <div className="registration-overlay" onClick={closeForm}>
            <div className="registration-modal" onClick={(e) => e.stopPropagation()}>
                <h2>Registration</h2>
                <div>
                    <label>Full Name</label>
                    <input
                        type="text"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                    />
                </div>
                <div>
                    <label>Age</label>
                    <input
                        type="number"
                        value={age}
                        onChange={(e) => setAge(e.target.value)}
                    />
                </div>
                <div>
                    <label>Nationality</label>
                    <input
                        type="text"
                        value={nationality}
                        onChange={(e) => setNationality(e.target.value)}
                    />
                </div>
                <div>
                    <label>Email</label>
                    <input type="email" value={email} readOnly />
                    <img src={LockIcon} alt="Lock Icon" />
                </div>
                <button onClick={handleSubmit}>Register</button>
            </div>
        </div>
    );
};

export default RegistrationForm;
