import React from 'react';
import './UserHome.css';
import Navbar from '../../components/navbar/Navbar';
import MyTournamentsTable from './MyTournamentsTable';
import AllTournamentsTable from './AllTournamentsTable';
import TournamentCarousel from './TournamentCarousel';
import Divider from '@mui/material/Divider';
import UserDetails from './UserDetails';

const Dashboard = () => {

  return (
    <div className='dashboard'>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner' style={{maxHeight:'fit-content'}}>
          <UserDetails />
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