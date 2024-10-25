import React from 'react';
import './AdminHome.css';
import Navbar from '../../components/navbar/Navbar';
import MatchCard from './MatchCard';
import MyTournamentsTable from './MyTournamentsTable';
import AllTournamentsTable from './AllTournamentsTable';
import TournamentCarousel from './TournamentCarousel';
import Divider from '@mui/material/Divider';
import UserDetails from './AdminDetails';

const Dashboard = () => {

  return (
    <div className='dashboard'>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner' style={{maxHeight:'fit-content'}}>
          <UserDetails />
        </div>
        <div className='dashboard-col-inner' style={{maxHeight:'fit-content'}}>
          <MatchCard />
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