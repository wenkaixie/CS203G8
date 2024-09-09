import React from 'react';
import './Home.css';
import Navbar from '../../components/navbar/Navbar';
import Carousel from 'react-bootstrap/Carousel';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import TrophyIcon from '../../assets/images/trophy.png';
import StarIcon from '../../assets/images/star.png';
import { Img } from 'react-image';

const Dashboard = () => {
  return (
    <Container fluid className="dashboard">
      <Row className='dashboard-row'>
        <Col xs={4} className='dashboard-col'>
          <Row className='dashboard-inner-row'>
            <Container fluid className='dashboard-container'>
              <h2 style={{fontWeight: '600px'}}>Welcome Back, John!</h2>
              <Container fluid className='dashboard-container-coloured'>
                <Container fluid className='dashboard-container-coloured-inner'>
                  <h4>Rating</h4>
                  <Container fluid className='dashboard-container-coloured-inner-second'>
                    <Img 
                      src={StarIcon}
                      height={'50px'}
                      width={'50px'}
                    />
                    <h2 style={{margin:'0px'}}>1000</h2>
                  </Container>
                </Container>
                <div className="divider" />
                <Container fluid className='dashboard-container-coloured-inner'>
                  <h4>World Rank</h4>
                  <Container fluid className='dashboard-container-coloured-inner-second'>
                    <Img 
                      src={TrophyIcon}
                      height={'50px'}
                      width={'50px'}
                    />
                    <h2 style={{margin:'0px'}}>#35</h2>
                  </Container>
                </Container>
              </Container>
            </Container>
          </Row>
          <Row className='dashboard-inner-row'>
            <Container fluid className='dashboard-container'>
              <h3>Recent Games</h3>
              <h5>14 May</h5>
              <Container fluid style={{alignItems:'baseline', padding:'5%', display:'inline'}} className='dashboard-container-coloured'>
                <h4>Summer Cup (Round 1)</h4>
                <Container fluid className='dashboard-container-coloured-inner'>
                  5 - 2
                </Container>
              </Container>
            </Container>
          </Row>
        </Col>
        <Col xs={8}>
          <Container fluid className='dashboard-container'>
            hello
          </Container>
        </Col>
      </Row>
    </Container>
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
          </Carousel.Caption>
        </Carousel.Item>
        <Carousel.Item>
          <Carousel.Caption>
            <p>Upcoming Tournaments</p>
            <h2>Tournament 2</h2>
            <h3>Round 1</h3>
          </Carousel.Caption>
        </Carousel.Item>
        <Carousel.Item>
          <Carousel.Caption>
            <p>Upcoming Tournaments</p>
            <h2>Tournament 3</h2>
            <h3>Round 1</h3>
          </Carousel.Caption>
        </Carousel.Item>
      </Carousel>
    </Container>
  );
};

function Home() {
  return (
    <div>
        <Navbar />
        <TournamentJumbotron />
        <Dashboard />
    </div>
  );
}

export default Home;