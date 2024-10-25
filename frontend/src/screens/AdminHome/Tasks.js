import React, { useState, useEffect } from 'react';
import './AdminHome.css';
import axios from 'axios';
import { getAuth } from "firebase/auth";
import TrophyIcon from '../../assets/images/trophy.png';
import StarIcon from '../../assets/images/star.png';
import { Img } from 'react-image';

const Tasks = () => {
    const auth = getAuth();
    const [tasks, setTasks] = useState(null);

    useEffect(() => {
        const fetchTasks = async () => {
            try {
                const response = await axios.get(`http://localhost:7070/admin/getAdminTaskList/${auth.currentUser.uid}`);
                setTasks(response.data);
            } catch (error) {
                console.error('Error fetching user details:', error);
            }
        };

        fetchTasks();
    }, []);

    if (!tasks) {
        return <div>Loading Tasks...</div>; 
    }

    return(
        <div>
          <div className='welcome-back'>
            <h2>Welcome Back, {tasks.username}!</h2>
          </div>
          <div className='rating-and-rank'>
            <div className='rating-and-rank-section'>
              <h4>Elo Rating</h4>
              <div fluid className='rating-and-rank-section-details'>
                <Img 
                  src={StarIcon}
                  height={'30%'}
                  width={'30%'}
                />
                <h2 style={{margin:'0px'}}>{tasks.elo}</h2>
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
                <h2 style={{margin:'0px'}}>#{}</h2>
              </div>
            </div>
          </div>
        </div>
    );
}

export default Tasks;