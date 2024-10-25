import React, { useState, useEffect } from 'react';
import './AdminHome.css';
import axios from 'axios';
import { getAuth } from "firebase/auth";
import TrophyIcon from '../../assets/images/trophy.png';
import StarIcon from '../../assets/images/star.png';
import { Img } from 'react-image';

const UserDetails = () => {
    const auth = getAuth();
    const [userDetails, setUserDetails] = useState(null);
    const [userRank, setUserRank] = useState('');

    useEffect(() => {
        const fetchUserDetails = async () => {
            try {
                const response = await axios.get(`http://localhost:9090/user/getUser/${auth.currentUser.uid}`);
                setUserDetails(response.data);
            } catch (error) {
                console.error('Error fetching user details:', error);
            }
        };

        fetchUserDetails();
    }, []);

    useEffect(() => {
      const fetchUserRank = async () => {
          try {
              const response = await axios.get(`http://localhost:9090/user/getUserRank/${auth.currentUser.uid}`);
              setUserRank(response.data);
          } catch (error) {
              console.error('Error fetching user rank:', error);
          }
      };

      fetchUserRank();
  }, []);

    if (!userDetails) {
        return <div>Loading User Details...</div>; 
    }

    return(
        <div>
          <div className='welcome-back'>
            <h2>Welcome Back, {userDetails.username}!</h2>
          </div>
          <div className='rating-and-rank'>
            <div className='rating-and-rank-section'>
              <h4>Elo Rating</h4>
              <div fluid className='rating-and-rank-section-details'>
                <Img 
                  src={StarIcon}
                  height={'30%'}
                  width={'30%'}
                />
                <h2 style={{margin:'0px'}}>{userDetails.elo}</h2>
              </div>
            </div>
            <div className='divider'></div>
            <div className='rating-and-rank-section'>
              <h4>Rank</h4>
              <div fluid className='rating-and-rank-section-details'>
                <Img 
                  src={TrophyIcon}
                  height={'30%'}
                  width={'30%'}
                />
                <h2 style={{margin:'0px'}}>#{userRank}</h2>
              </div>
            </div>
          </div>
        </div>
    );
}

export default UserDetails;