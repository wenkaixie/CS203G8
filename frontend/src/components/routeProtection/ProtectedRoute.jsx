import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, onAuthStateChanged } from 'firebase/auth';
import { query, getDocs, collection, where } from 'firebase/firestore'; // Firestore for fetching user roles
import { FirestoreDB } from '../../firebase/firebase_config';

const ProtectedRoute = ({ children, allowedRoles }) => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [accessDenied, setAccessDenied] = useState(false);

    useEffect(() => {
        const auth = getAuth();

        const checkAuth = () => {
            setLoading(true);

            const unsubscribe = onAuthStateChanged(auth, async (user) => {
                if (user) {
                    try {
                        let userRole = null;

                        // Check if the user exists in the 'User' collection
                        const userQuery = query(collection(FirestoreDB, 'User'), where('UID', '==', user.uid));
                        const userSnapshot = await getDocs(userQuery);
            
                        if (!userSnapshot.empty) {
                            userRole = 'User';
                        } else {
                            // If not found in 'User', check the 'Admin' collection
                            const userQuery = query(collection(FirestoreDB, 'Admin'), where('UID', '==', user.uid));
                            const userSnapshot = await getDocs(userQuery);
                
                            if (!userSnapshot.empty) {
                                userRole = 'Admin';
                            }
                        }

                        if (!userRole || !allowedRoles.includes(userRole)) {
                            // User is authenticated but doesn't have the required role
                            setAccessDenied(true);
                        } else {
                            // User is authenticated and has the required role
                            setAccessDenied(false);
                        }
                    } catch (error) {
                        console.error("Error fetching user role: ", error);
                        setAccessDenied(true); // Deny access if any error occurs
                    }
                } else {
                    // User is not authenticated, redirect to login
                    navigate('/');
                    setAccessDenied(true);
                }

                setLoading(false);
            });

            return () => unsubscribe();
        };

        checkAuth();
    }, [navigate, allowedRoles]);

    const handleClosePopup = () => {
        setAccessDenied(false);
        setLoading(true);
        navigate(-1); // Go back to the previous route
    };

    if (loading) {
        return; // return a blank page during loading process
    }

    return (
        <>
            {accessDenied ? (
                <div className="popup">
                    <div className="popup-content">
                        <h2>Access Denied</h2>
                        <p>You do not have permission to view this page.</p>
                        <button onClick={handleClosePopup}>Close</button>
                    </div>
                </div>
            ) : (
                children // Render children only if access is not denied
            )}
        </>
    );
};

export default ProtectedRoute;
