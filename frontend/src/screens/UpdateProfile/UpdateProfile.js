import React, { useState, useEffect } from "react";
import './UpdateProfile.css';
import logoImage from '../../assets/images/logo.png';
import profileImage from '../../assets/images/chess-profile-pic.jpg';
import { Img } from "react-image";
import Image from 'react-bootstrap/Image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import axios from 'axios';
import {getAuth} from 'firebase/auth';

const UpdateProfile = () => {
    // Define state for form inputs
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [country, setCountry] = useState('');
    const [age, setAge] = useState('');

    // Define state for placeholders (fetched data)
    const [profileData, setProfileData] = useState({
        firstName: '',
        lastName: '',
        country: '',
        age: ''
    });

    // Fetch user profile data when the component mounts
    useEffect(() => {
        const fetchProfileData = async () => {
            try {
                // Initialize Firebase auth
                const auth = getAuth();
                const user = auth.currentUser;

                if (user) {
                    const uid = user.uid; // Fetch user ID from Firebase

                    // Make API call to your backend with the user ID
                    const response = await axios.get(`http://localhost:9090/user/getUser/${uid}`);

                    if (response.status === 200) {
                        const data = response.data;
                        setProfileData({
                            firstName: data.name || '', // Fallback to empty string if undefined
                            country: data.country || '',
                            age: data.age || ''
                        });
                    }
                } else {
                    console.log('No authenticated user found.');
                }
            } catch (error) {
                console.error('Error fetching profile data:', error);
            }
        };

        fetchProfileData();
    }, []);

    // Handler for form submission
    const handleSubmit = async (event) => {
        event.preventDefault();

        // Data to be sent to the server
        const updatedProfileData = {
            firstName,
            lastName,
            country,
            age
        };

        try {
            // Replace with your API endpoint to update profile data
            const response = await axios.put('https://your-api-url.com/api/profile/update', updatedProfileData); // Change to your API endpoint

            // Handle successful response (e.g., display success message)
            if (response.status === 200) {
                alert('Profile updated successfully!');
            }
        } catch (error) {
            // Handle error response
            console.error('Error updating profile:', error);
            alert('Failed to update profile.');
        }
    };

    return (
        <div>
            <div className="icon-bar">
                <Img 
                    src={logoImage}
                    width={212}
                    height={72}
                />
            </div>
            <div className="update-profile-wrapper">
                <div className="update-profile-container">
                    <div className="update-profile-heading">
                        <h1>Update Profile</h1>
                    </div>
                    <div className="update-profile-icon-wrapper">
                        <div className="update-profile-icon">
                            <Image 
                                src={profileImage} 
                                className="update-profile-picture"
                                roundedCircle 
                            />
                        </div>
                    </div>
                    <div className="update-profile-details-wrapper">
                        <Form style={{ fontSize: "20px" }} onSubmit={handleSubmit}>
                            <Form.Group className="mb-3" controlId="formBasicFirstName">
                                <Form.Label>First Name</Form.Label>
                                <Form.Control 
                                    type="text" 
                                    placeholder={profileData.firstName || 'Enter first name'} // Use placeholder from fetched data
                                    value={firstName}
                                    onChange={(e) => setFirstName(e.target.value)}  // Update firstName state
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicLastName">
                                <Form.Label>Last Name</Form.Label>
                                <Form.Control 
                                    type="text" 
                                    placeholder={profileData.lastName || 'Enter last name'} // Use placeholder from fetched data
                                    value={lastName}
                                    onChange={(e) => setLastName(e.target.value)}  // Update lastName state
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicCountry">
                                <Form.Label>Country</Form.Label>
                                <Form.Control 
                                    type="text" 
                                    placeholder={profileData.country || 'Enter country'} // Use placeholder from fetched data
                                    value={country}
                                    onChange={(e) => setCountry(e.target.value)}  // Update country state
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicAge">
                                <Form.Label>Age</Form.Label>
                                <Form.Control 
                                    type="number" 
                                    placeholder={profileData.age || 'Enter age'} // Use placeholder from fetched data
                                    value={age}
                                    onChange={(e) => setAge(e.target.value)}  // Update age state
                                />
                            </Form.Group>
                            <div className="update-profile-button-wrapper">
                                <Button variant="primary" className='continue-button' type="submit">
                                    Save
                                </Button>
                            </div>
                        </Form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UpdateProfile;