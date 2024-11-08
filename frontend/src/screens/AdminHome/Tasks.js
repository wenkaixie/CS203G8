import React, { useState, useEffect } from "react";
import "./Tasks.css";
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import axios from "axios";
import { getAuth } from "firebase/auth";
import { useNavigate } from 'react-router-dom';

const Tasks = () => {
  const auth = getAuth();
  const [tasks, setTasks] = useState([]);
  const [currentPage, setCurrentPage] = useState(1); // Track the current page
  const tasksPerPage = 3;
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

  // Calculate current tasks for the current page
  const indexOfLastTask = currentPage * tasksPerPage;
  const indexOfFirstTask = indexOfLastTask - tasksPerPage;
  const currentTasks = tasks.slice(indexOfFirstTask, indexOfLastTask);

  const totalPages = Math.ceil(tasks.length / tasksPerPage);

  // Handle page navigation
  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
  };

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
        {currentTasks.map((task, index) => (
          <div
            key={index}
            className="task-item"
            onClick={() => navigate(`/admin/tournament/${task.tid}/overview`)}
          >
            <div className="task-details">
              <h4>{task.name}</h4>
              <p>{task.description}</p>
              <span>Status: {task.status}</span>
            </div>
            <ArrowForwardIosIcon />
          </div>
        ))}
      </div>

      <br></br>
      {/* Pagination */}
      <div className="pagination">
        <span>
          <ArrowBackIosNewIcon 
            onClick={() => currentPage > 1 && handlePageChange(currentPage - 1)}
            sx={{ cursor: "pointer", fontSize: "20px" }}
          />
        </span>
        {Array.from({ length: totalPages }, (_, i) => (
          <span
            key={i + 1}
            className={`page-number ${currentPage === i + 1 ? 'active' : ''}`}
            onClick={() => handlePageChange(i + 1)}
          >
            {i + 1}
          </span>
        ))}
        <span>
          <ArrowForwardIosIcon
            onClick={() => currentPage < totalPages && handlePageChange(currentPage + 1)}
            sx={{ cursor: "pointer", fontSize: "20px" }}
          />
        </span>
      </div>
    </div>
  );
}

export default Tasks;