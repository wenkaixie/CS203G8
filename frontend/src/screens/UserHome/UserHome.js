import React, { useState, useEffect } from 'react';
import './UserHome.css';
import Navbar from '../../components/navbar/Navbar';
import TrophyIcon from '../../assets/images/trophy.png';
import StarIcon from '../../assets/images/star.png';
import { Img } from 'react-image';
import MatchCard from './MatchCard';
import MyTournamentsTable from './MyTournamentsTable';
import UpcomingTournamentsTable from './UpcomingTournamentsTable';
import { useNavigate } from 'react-router-dom';
import TournamentCarousel from './TournamentCarousel';
import Divider from '@mui/material/Divider';
import axios from 'axios';
import { getAuth } from "firebase/auth";

const Dashboard = () => {
  const navigate = useNavigate();
  const auth = getAuth();
  const [userDetails, setUserDetails] = useState(null);

  const handleViewGamesHistory = () => {
    navigate('/user/home');
  };

  const fetchUserDetails = async () => {
    try {
        const response = await axios.get(`http://localhost:8080/user/${auth.currentUser.uid}`); //todo fetch from userdetails
        setUserDetails(response.data);
    } catch (error) {
        console.error('Error fetching user details:', error);
    }
  };

  useEffect(() => {
    fetchUserDetails();
  }, []);

  return (
    <div className='dashboard'>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner'>
          <div className='welcome-back'>
            <h2>Welcome Back, John!</h2>
          </div>
          <div className='rating-and-rank'>
            <div className='rating-and-rank-section'>
              <h4>Rating</h4>
              <div fluid className='rating-and-rank-section-details'>
                <Img 
                  src={StarIcon}
                  height={'30%'}
                  width={'30%'}
                />
                <h2 style={{margin:'0px'}}>1000</h2>
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
                <h2 style={{margin:'0px'}}>#45</h2>
              </div>
            </div>
          </div>
        </div>
        <div className='dashboard-col-inner'>
          <div>
            <h2>Recent games</h2>
            <h5>14 May</h5>
          </div>
          <MatchCard />
          <div onClick={ handleViewGamesHistory } className='games-history'>
            <h6>View Tournament</h6>
          </div>
        </div>
      </div>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner'>
          <MyTournamentsTable />
          <Divider sx={{ my: 0.5 }} />
          <UpcomingTournamentsTable />
        </div>
      </div>
    </div>
  );
};

const Home = () => {

  return (
    <div>
      <div className='background'>
        <div className='background-content'>
          <Navbar />
          <TournamentCarousel />
          <Dashboard />
        </div>
      </div>
    </div>
  );
};

export default Home;