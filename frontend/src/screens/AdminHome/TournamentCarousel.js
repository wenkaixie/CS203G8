import React, { useState, useEffect } from 'react';
import './TournamentCarousel.css';
import Carousel from 'react-bootstrap/Carousel';
import Container from 'react-bootstrap/Container';
import CountdownTimer from './CountdownTimer';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { getAuth } from "firebase/auth";

const TournamentCarousel = () => {
  const [ongoingTournaments, setOngoingTournaments] = useState([]);
  const [upcomingTournaments, setUpcomingTournaments] = useState([]);

  const navigate = useNavigate(); // Use the navigate hook

  // Function to handle clicking on a tournament
  const handleTournamentClick = (tournamentId) => {
    navigate(`/user/tournament/${tournamentId}/overview`);
  };

  const auth = getAuth();

  useEffect(() => {
    const fetchOngoingTournaments = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/tournaments/ongoing/${auth.currentUser.uid}`);
        setOngoingTournaments(response.data);
      } catch (error) {
        console.error('Error fetching ongoing tournaments:', error);
      }
    };

    fetchOngoingTournaments();
  }, []);

  useEffect(() => {
    const fetchUpcomingTournaments = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/tournaments/upcoming/${auth.currentUser.uid}`);
        setUpcomingTournaments(response.data);
      } catch (error) {
        console.error('Error fetching upcoming tournaments:', error);
      }
    };

    fetchUpcomingTournaments();
  }, []);

  if ((!ongoingTournaments && !upcomingTournaments) || (ongoingTournaments.length === 0 && upcomingTournaments.length === 0)) {
    return(
      <Container fluid>
        <Carousel data-bs-theme="dark">
            <Carousel.Item>
              <Carousel.Caption>
                <p> </p>
                <h2>You have no Upcoming Tournaments</h2>
                <h3>Create a Tournament to get started!</h3>
              </Carousel.Caption>
            </Carousel.Item>
        </Carousel>
      </Container>
    );
}

  return (
    <Container fluid>
      <Carousel data-bs-theme="dark">
        {ongoingTournaments.map((tournament, index) => (
          <Carousel.Item key={index} onClick={() => handleTournamentClick(tournament.tid)} style={{ cursor: 'pointer' }} >
            <Carousel.Caption>
              <p>My Ongoing Tournaments</p>
              <h2>{tournament.name}</h2>
              <h3>Round 1</h3>
              <CountdownTimer targetDate={tournament.startDatetime} />
            </Carousel.Caption>
          </Carousel.Item>
        ))}
        {upcomingTournaments.map((tournament, index) => (
          <Carousel.Item key={index} onClick={() => handleTournamentClick(tournament.tid)} style={{ cursor: 'pointer' }} >
            <Carousel.Caption>
              <p>My Upcoming Tournaments</p>
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

export default TournamentCarousel;