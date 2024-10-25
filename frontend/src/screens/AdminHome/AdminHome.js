import React from 'react';
import './AdminHome.css';
import MatchCard from './CreateTournamentCard';
import MyTournamentsTable from './MyTournamentsTable';
import TournamentCarousel from './TournamentCarousel';
import Divider from '@mui/material/Divider';
import Tasks from './Tasks';
import AdminNavbar from '../../components/adminNavbar/AdminNavbar';

const Dashboard = () => {

  return (
    <div className='dashboard'>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner' style={{maxHeight:'fit-content'}}>
          <MatchCard />
        </div>
        <div className='dashboard-col-inner' style={{maxHeight:'fit-content'}}>
          <Tasks />
        </div>
      </div>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner'>
          <MyTournamentsTable />
        </div>
      </div>
    </div>
  );
};

const AdminHome = () => {

  return (
    <div>
      <div className='background'>
        <div className='background-content'>
          <AdminNavbar />
          <TournamentCarousel />
          <Dashboard />
        </div>
      </div>
    </div>
  );
};

export default AdminHome;