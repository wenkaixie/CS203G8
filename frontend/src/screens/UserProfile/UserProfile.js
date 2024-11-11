import { React, useState, useEffect } from 'react';
import './UserProfile.css';
import Navbar from '../../components/navbar/Navbar';
import { getAuth } from "firebase/auth";
import axios from 'axios';
import { useNavigate, useParams } from 'react-router-dom';
import Image from 'react-bootstrap/Image';
import profileImage from '../../assets/images/chess-profile-pic.jpg';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import chessIcon from '../../assets/images/ChessIcon.png';
import { Divider } from '@mui/material';
import countryList from 'react-select-country-list';
import MyTournamentsTable from './MyTournamentsTable';

const UserProfile = () => {
    const { userID } = useParams();
    const [userProfileData, setUserProfileData] = useState();
    const [userRank, setUserRank] = useState();
    const [isLoading, setIsLoading] = useState(true);
    const [countryCode, setCountryCode] = useState('');
    
    const auth = getAuth();
    const navigate = useNavigate();

    const fetchProfile = async () => {
        try {
            const response = await axios.get(`${process.env.REACT_APP_API_URL}:9090/user/getUser/${userID}`);
            const data = response.data;
            setUserProfileData(data);

            // Check if nationality is defined before attempting to get the country code
            if (data.nationality) {
                const countries = countryList().getData();
                const country = countries.find(
                    (c) => c.label.toLowerCase() === data.nationality.toLowerCase()
                );
                if (country) setCountryCode(country.value.toLowerCase());
            }

            setIsLoading(false);
            console.log(data);
            
        } catch (error) {
            console.error('Error fetching profile:', error);
            setIsLoading(false);
        }
    };

    const fetchUserRank = async () => {
        try {
            const response = await axios.get(`${process.env.REACT_APP_API_URL}:9090/user/getUserRank/${userID}`);
            setUserRank(response.data);
        } catch (error) {
            console.error('Error fetching user rank:', error);
        }
    };

    useEffect(() => {
        if (userID) {
            fetchProfile();
            fetchUserRank();
        }
    }, [userID]);

    if (isLoading) {
        return <div>Loading...</div>;
    }
    
    if (!userProfileData) {
        return <div>Error: User data could not be loaded.</div>;
    }

    return (
        <div>
            <Navbar />
            <div className='profile'>
                <div className="profile-title" onClick={() => navigate(-1)}>
                    <ArrowBackIosNewIcon sx={{ fontSize: '2rem', cursor: 'pointer' }}/>
                    <h1 style={{ marginBottom:"0px", marginTop:"10px"}}>View Profile</h1>
                </div>
                <div className='profile-container'>
                    <div className='profile-header'>
                        <div className="profile-icon-wrapper">
                            <div className="profile-icon">
                                <Image 
                                    src={profileImage} 
                                    className="profile-picture"
                                    roundedCircle 
                                />
                            </div>
                        </div>
                        <div className='profile-header-details'>
                            <div className='profile-header-text'>
                                <h1>
                                    {countryCode && (
                                        <img
                                            src={`https://flagcdn.com/w20/${countryCode}.png`}
                                            alt={`${userProfileData.nationality} flag`}
                                            style={{ margin: 10, width: 30, height: 22.5 }}
                                        />
                                    )}
                                    {userProfileData.name} ({userProfileData.username})
                                </h1>
                                <h5>
                                    <Image 
                                        src={chessIcon}
                                        className='chess-icon'
                                    />
                                    Chess.com: {userProfileData.chessUsername ? userProfileData.chessUsername : "Unlinked"}
                                </h5>
                            </div>
                            {auth.currentUser.uid === userID && (
                                <button 
                                    className='edit-profile-button' 
                                    onClick={() => navigate('/user/update-profile')}
                                >
                                    Edit Profile
                                </button>
                            )}
                        </div>
                    </div>
                    <div className='profile-body'>
                        <div className='profile-body-tournaments'>
                            {/* <h2>Completed Tournaments</h2> */}
                            <div>
                                <MyTournamentsTable />
                            </div>
                        </div>
                        <Divider orientation="vertical" flexItem style={{ margin: '0 40px', backgroundColor: '#ccc', borderRadius: "1px"}} />
                        <div className='profile-body-stats'>
                            <h2>User Statistics</h2>
                            <br></br>
                            <div className='profile-body-stats-text'>
                                <p>Elo:</p>
                                <p>{userProfileData.elo}</p>
                            </div>
                            <div className='profile-body-stats-text'>
                                <p>Site Rank: </p>
                                <p>#{userRank}</p>
                            </div>
                            <div className='profile-body-stats-text'>
                                <p>Tournaments Joined: </p>
                                <p>{userProfileData.registrationHistory.length}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserProfile;