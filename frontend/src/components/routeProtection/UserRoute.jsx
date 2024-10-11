import React from 'react';
import ProtectedRoute from './ProtectedRoute'; // Adjust the path as necessary

const UserRoute = ({ children }) => {
    return <ProtectedRoute allowedRoles={['User']}>{children}</ProtectedRoute>;
};

export default UserRoute;
