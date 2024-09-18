import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/routeProtection/ProtectedRoute';

// Import your pages/components
import UserHome from './screens/UserHome/UserHome';
import Login from './screens/Login/Login';
import CreateProfile from './screens/CreateProfile/CreateProfile';
import Signup from './screens/Signup/Signup';

function App() {
  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/user/home" element={<UserHome />} />
        <Route path="/" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/createprofile" element={<CreateProfile />} />
        <Route path="/user/home" element={<UserHome />} />
        
        {/* User Protected Routes */}
        <Route
          path="/user/*"  // Wildcard allows for nested routes
          element={
            <ProtectedRoute allowedRoles={['user']}>
            </ProtectedRoute>
          }
        >
          {/* Nested user-specific routes */}
          {/* <Route path="home" element={<UserHome />} /> */}
        </Route>

        {/* Admin Protected Routes */}
        <Route
          path="/admin/*"
          element={
            <ProtectedRoute allowedRoles={['admin']}>
            </ProtectedRoute>
          }
        >
          {/* Nested admin-specific routes here */}
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
