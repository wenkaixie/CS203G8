import React, { useState, useEffect } from "react";
import './UserUpdateProfile.css';
import logoImage from '../../assets/images/logo.png';
import profileImage from '../../assets/images/chess-profile-pic.jpg';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import { Img } from "react-image";
import Image from 'react-bootstrap/Image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import axios from 'axios';
import { getAuth } from "firebase/auth";
import { useNavigate } from "react-router-dom";

const UserUpdateProfile = () => {
    const [name, setName] = useState('');
    const [username, setUsername] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [nationality, setNationality] = useState('');
    const [dob, setDob] = useState('');
    const [chessUsername, setChessUsername] = useState('');

    const auth = getAuth();
    const navigate = useNavigate();

    useEffect(() => {
        const fetchProfileData = async () => {
            try {
                const response = await axios.get(`http://localhost:9090/user/getUser/${auth.currentUser.uid}`);
                const data = response.data;
                console.log("Data received:", data);
                setName(data.name || '');
                setUsername(data.username || '');
                setPhoneNumber(data.phoneNumber || '');
                setNationality(data.nationality || '');
                if (data.dateOfBirth) {
                    const timestamp = data.dateOfBirth;
                    const date = new Date(timestamp.seconds * 1000);
                    const formattedDate = date.toISOString().split('T')[0];
                    setDob(formattedDate);
                } else {
                    setDob('');
                }
            } catch (error) {
                console.error('Error fetching profile data:', error);
            }
        };

        fetchProfileData();
    }, []);

    const handleSubmit = async (event) => {
        event.preventDefault();

        const updatedProfileData = {
            name: name,
            username: username,
            phoneNumber: phoneNumber,
            nationality: nationality,
            dateOfBirth: dob,
            chessUsername: chessUsername
        };

        try {
            const response = await axios.put(`http://localhost:9090/user/updateUser/${auth.currentUser.uid}`, updatedProfileData);

            if (response.status === 200) {
                alert('Profile updated successfully!');
                handleHome();
            }
        } catch (error) {
            console.error('Error updating profile:', error);
            alert('Failed to update profile.');
        }
    };

    const handleReturn = () => {
        navigate(-1);
    };

    const handleHome = () => {
        navigate("/user/home");
    };


    return (
        <div>
            <div className="icon-bar">
                <Img 
                    src={logoImage}
                    width={212}
                    height={72}
                    onClick={handleHome}
                    style={{cursor: "pointer"}}
                />
            </div>
            <div className="update-profile-wrapper">
                <div className="update-profile-container">
                    <div className="update-profile-heading" onClick={handleReturn}>
                        <ArrowBackIosNewIcon sx={{ fontSize: '2rem', cursor: 'pointer' }}/>
                        <h1 style={{ marginBottom:"0px", marginTop:"10px"}}>Update Profile</h1>
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
                            <Form.Group className="mb-3" controlId="formBasicName">
                                <Form.Label>Name</Form.Label>
                                <Form.Control 
                                    type="text" 
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicUsername">
                                <Form.Label>Username</Form.Label>
                                <Form.Control 
                                    type="text" 
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicPhoneNumber">
                                <Form.Label>Phone Number</Form.Label>
                                <Form.Control 
                                    type="text" 
                                    value={phoneNumber}
                                    onChange={(e) => setPhoneNumber(e.target.value)}
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicNationality">
                                <Form.Label>Nationality</Form.Label>
                                <Form.Control 
                                    type="text" 
                                    value={nationality}
                                    onChange={(e) => setNationality(e.target.value)}
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicDob">
                                <Form.Label>Date of Birth</Form.Label>
                                <Form.Control 
                                    type="date"
                                    value={dob}
                                    onChange={(e) => setDob(e.target.value)}
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicChessUsername">
                                <Form.Label>Chess.com Username</Form.Label>
                                <Form.Control 
                                    type="text" 
                                    placeholder="Enter username" 
                                    value={chessUsername}
                                    onChange={(e) => setChessUsername(e.target.value)}
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

export default UserUpdateProfile;