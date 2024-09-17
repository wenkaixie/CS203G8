// import React from 'react';
// import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
// import Home from './screens/Home/Home';
// import Login from './screens/Login/Login';
// import CreateProfile from './screens/CreateProfile/CreateProfile';
// import Signup from './screens/Signup/Signup';

// function App() {
//   return (
//     <Router>
//       <Routes>
//         <Route path="/login" element={<Login />} />
//         <Route path="/signup" element={<Signup />} />
//         <Route path="/home" element={<Home />} />
//         <Route path="/create_profile" element={<CreateProfile />} />
//       </Routes>
//     </Router>
//   );
// }

// export default App;

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
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/create_profile" element={<CreateProfile />} />
        {/* User Protected Routes */}
        <Route
          path="/user"
          element={
            <ProtectedRoute allowedRoles={['user']}>
              <Routes>
                {/* <Route path="/home" element={<UserHome />} /> */}
              </Routes>
            </ProtectedRoute>
          }
        />

        {/* Admin Protected Routes */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={['admin']}>
              <Routes>
                {/* <Route path="/home" element={<AdminHome />} /> */}
              </Routes>
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
