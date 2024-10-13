import React from 'react';
import './UserHome.css';
import Navbar from '../../components/navbar/Navbar';
import MatchCard from './MatchCard';
import MyTournamentsTable from './MyTournamentsTable';
import AllTournamentsTable from './AllTournamentsTable';
import { useNavigate } from 'react-router-dom';
import TournamentCarousel from './TournamentCarousel';
import Divider from '@mui/material/Divider';
import UserDetails from './UserDetails';

const Dashboard = () => {
  const navigate = useNavigate();

  const handleViewGamesHistory = () => {
    navigate('/user/home');
  };


  return (
    <div className='dashboard'>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner'>
          <UserDetails />
        </div>
        <div className='dashboard-col-inner'>
          <div>
            <h2>Recent games</h2>
            <h5>14 May</h5>
          </div>
          <MatchCard />
          <div onClick={ handleViewGamesHistory } className='games-history'>
            <h6>View Tournament Details</h6>
          </div>
        </div>
      </div>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner'>
          <MyTournamentsTable />
          <Divider sx={{ my: 0.5 }} />
          <AllTournamentsTable />
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