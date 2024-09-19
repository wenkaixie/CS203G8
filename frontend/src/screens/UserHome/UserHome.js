import React from 'react';
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


const Dashboard = () => {
  const navigate = useNavigate();

  const handleViewGamesHistory = (event) => {
    navigate ('/user/home'); // replace with round details
  }

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
            <h8>View games history</h8>
          </div>
        </div>
      </div>
      <div className='dashboard-col'>
        <div className='dashboard-col-inner'>
          <TournamentsTable />
        </div>
      </div>
    </div>
  );
};

const TournamentJumbotron = () => {
  return (
    <Container fluid>
      <Carousel data-bs-theme="dark">
        <Carousel.Item>
          <Carousel.Caption>
            <p>Upcoming Tournaments</p>
            <h2>Tournament 1</h2>
            <h3>Round 1</h3>
            <CountdownTimer targetDate={'2024-09-22T10:00:00'}/>
          </Carousel.Caption>
        </Carousel.Item>
        <Carousel.Item>
          <Carousel.Caption>
            <p>Upcoming Tournaments</p>
            <h2>Tournament 2</h2>
            <h3>Round 1</h3>
            <CountdownTimer targetDate={'2024-09-24T10:00:00'}/>
          </Carousel.Caption>
        </Carousel.Item>
        <Carousel.Item>
          <Carousel.Caption>
            <p>Upcoming Tournaments</p>
            <h2>Tournament 3</h2>
            <h3>Round 1</h3>
            <CountdownTimer targetDate={'2024-09-27T10:00:00'}/>
          </Carousel.Caption>
        </Carousel.Item>
      </Carousel>
    </Container>
  );
};

const Home = () => {
  return (
    <div>
        <div className='background'>
          <div className='background-content'>
            <Navbar />
            <TournamentJumbotron />
            <Dashboard />
          </div>
        </div>
    </div>
  );
}

export default Home;