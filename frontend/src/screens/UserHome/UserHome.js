import React, { useState, useEffect } from 'react';
import './UserHome.css';
import Navbar from '../../components/navbar/Navbar';
import Carousel from 'react-bootstrap/Carousel';
import Container from 'react-bootstrap/Container';
import TrophyIcon from '../../assets/images/trophy.png';
import StarIcon from '../../assets/images/star.png';
import { Img } from 'react-image';
import MatchCard from './MatchCard';
import TournamentsTable from './TournamentsTable';
import { useNavigate } from 'react-router-dom';
import CountdownTimer from './CountdownTimer';
import axios from 'axios';

const Dashboard = ({ tournaments }) => {
  const navigate = useNavigate();

  const handleViewGamesHistory = () => {
    navigate('/user/home');
  };

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
            <h6>View games history</h6>
          </div>
        </div>
      </div>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner'>
          <TournamentsTable tournaments={tournaments} />
        </div>
      </div>
    </div>
  );
};

const TournamentJumbotron = ({ tournaments }) => {
  const navigate = useNavigate(); // Use the navigate hook

  // Function to handle clicking on a tournament
  const handleTournamentClick = (tournamentId) => {
    navigate(`/user/tournament/${tournamentId}/overview`);
  };

  return (
    <Container fluid>
      <Carousel data-bs-theme="dark">
        {tournaments.map((tournament, index) => (
          <Carousel.Item key={index} onClick={() => handleTournamentClick(tournament.tid)} style={{ cursor: 'pointer' }} >
            <Carousel.Caption>
              <p>Upcoming Tournaments</p>
              <h2>{tournament.name}</h2>
              <h3>Round 1</h3>
              <CountdownTimer targetDate={tournament.startDatetime} />
            </Carousel.Caption>
          </Carousel.Item>
        ))}
      </Carousel>
    </Container>
  );
};

const Home = () => {
  const [tournaments, setTournaments] = useState([]);

  useEffect(() => {
    const fetchTournaments = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/tournaments');
        setTournaments(response.data);
      } catch (error) {
        console.error('Error fetching tournaments:', error);
      }
    };

    fetchTournaments();
  }, []);

  return (
    <div>
      <div className='background'>
        <div className='background-content'>
          <Navbar />
          <TournamentJumbotron tournaments={tournaments} />
          <Dashboard tournaments={tournaments} />
        </div>
      </div>
    </div>
  );
};

export default Home;