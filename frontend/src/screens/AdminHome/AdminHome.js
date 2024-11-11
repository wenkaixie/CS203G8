import React, { useState, useEffect } from 'react';
import './AdminHome.css';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import CreateTournamentForm from '../AdminTournamentDetails/CreateTournamentForm'
import MyTournamentsTable from './MyTournamentsTable';
import TournamentCarousel from './TournamentCarousel';
import Tasks from './Tasks';
import AdminNavbar from '../../components/adminNavbar/AdminNavbar';
import axios from 'axios';
import { getAuth } from "firebase/auth";

const Dashboard = ({upcomingTournaments, ongoingTournaments}) => {
  const [showCreateForm, setShowCreateForm] = useState(false);

  const closeForm = () => {
      setShowCreateForm(false);
  };

  return (
    <div className='dashboard'>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner' style={{maxHeight:'fit-content'}}>
          <div className="create-tournament">
            <h2>Create a new tournament</h2>
            <AddCircleIcon
              sx={{ fontSize: '2.5rem', cursor: 'pointer', color: '#8F3013' }}
              onClick={() => setShowCreateForm(true)}
            />
            {showCreateForm && 
              <CreateTournamentForm 
                onClose={closeForm}
                onSuccess={() => {
                    window.location.reload();
                    closeForm();
              }}/>
            }
          </div>
        </div>
        <div className='dashboard-col-inner' style={{maxHeight:'fit-content'}}>
          <Tasks />
        </div>
      </div>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner'>
          <MyTournamentsTable upcomingTournaments={upcomingTournaments} ongoingTournaments={ongoingTournaments} />
        </div>
      </div>
    </div>
  );
};

const AdminHome = () => {
  const [ongoingTournaments, setOngoingTournaments] = useState([]);
  const [upcomingTournaments, setUpcomingTournaments] = useState([]);

  const auth = getAuth();

  const fetchTournaments = async () => {
    try {
      const response = await axios.get(`${process.env.REACT_APP_API_URL}:7070/admin/getAdminTournaments/${auth.currentUser.uid}`);
      const tournaments = response.data;
      console.log(response.data);
      
      // Get the current time to compare with tournament dates
      const currentTime = new Date();

      // Split tournaments into ongoing and upcoming based on start and end datetimes
      const ongoing = tournaments.filter(
        (tournament) => new Date(tournament.startDatetime) <= currentTime && new Date(tournament.endDatetime) > currentTime
      );

      const upcoming = tournaments.filter(
        (tournament) => new Date(tournament.startDatetime) > currentTime
      );

      console.log(upcoming);

      setOngoingTournaments(ongoing);
      setUpcomingTournaments(upcoming);
    } catch (error) {
      console.error('Error fetching tournaments:', error);
    }
  };

  useEffect(() => {
    fetchTournaments();
  }, []);

  return (
    <div>
      <div className='background'>
        <div className='background-content'>
          <AdminNavbar />
          <TournamentCarousel upcomingTournaments={upcomingTournaments} ongoingTournaments={ongoingTournaments} />
          <Dashboard upcomingTournaments={upcomingTournaments} ongoingTournaments={ongoingTournaments} />
        </div>
      </div>
    </div>
  );
};

export default AdminHome;