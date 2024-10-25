import React, { useState, useEffect } from "react";
import "./Tasks.css";
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import axios from "axios";
import { getAuth } from "firebase/auth";
import { useNavigate } from 'react-router-dom';

const Tasks = () => {
  const auth = getAuth();
  const [tasks, setTasks] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const response = await axios.get(
          `http://localhost:7070/admin/getAdminTaskList/${auth.currentUser.uid}`
        );
        setTasks(response.data);
      } catch (error) {
        console.error("Error fetching tasks:", error);
      }
    };

    fetchTasks();
  }, []);

  if (!tasks.length) {
    return (
      <div>
        <h2>Tasks</h2>
        <p>You have no tasks</p>
      </div>
    );
  }

  return (
    <div>
      <div className="tasks-header">
        <h2>Tasks</h2>
      </div>
      <div className="task-list">
        {tasks.map((task, index) => (
          <div key={index} className="task-item">
            <div className="task-details">
              <h4>{task.name}</h4>
              <p>{task.description}</p>
              <span>{task.status}</span>
            </div>
            <ArrowForwardIosIcon 
              sx={{ fontSize: '1.5rem', cursor: 'pointer' }} 
              onClick={() => navigate(`/admin/tournament/${task.tid}`)} 
            />
          </div>
        ))}
      </div>
    </div>
  );
}

export default Tasks;