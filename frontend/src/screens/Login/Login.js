import React, { useState, useEffect } from 'react';
import './Login.css';
import './Popup.css';
import Container from 'react-bootstrap/Container';
import Icon from '../../assets/images/icon.jpg';
import { Img } from 'react-image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import { FaGoogle } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import FBInstanceAuth from "../../firebase/firebase_auth";  // Firebase auth class instance
import { getAuth, signInWithEmailAndPassword, onAuthStateChanged } from 'firebase/auth';
import { collection, query, where, getDocs } from 'firebase/firestore';
import { FirestoreDB } from '../../firebase/firebase_config';

const Login = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);  // State for handling errors
    const [showPassword, setShowPassword] = useState(false);  // Show/hide password
    const [loading, setLoading] = useState(true); // Loading state for auth check

    const auth = getAuth();  // Get Firebase auth instance
    const navigate = useNavigate();  // React router's navigation hook

    useEffect(() => {
        let initialLoad = true;
    
        // Listen for authentication state changes
        const unsubscribe = onAuthStateChanged(auth, async (user) => {
            if (user) {
                try {
                    setLoading(true);
                    //console.log("User logged in:", user.email);
                    const role = await getUserRole(user.email); // Await role checking
                    
                    // console.log("User role:", role);   

                    // Redirect user to the appropriate home page based on the collection they belong to
                    if (role === 'User') {
                        navigate('/user/home');
                    } else if (role === 'Admin') {
                        navigate('/admin/home');
                    } else if (!initialLoad) {
                        setError('User role not found.');
                    }
                } catch (error) {
                    if (!initialLoad) {
                        setError('Error retrieving user role.');
                    }
                } finally {
                    setLoading(false); // Ensure loading is set to false after processing
                }
            } else {
                setLoading(false); // No user, stop loading
            }
    
            initialLoad = false; // Set to false after the first run
        });
    
        // Clean up the listener on component unmount
        return () => unsubscribe();
    }, [auth, navigate]);    

    //Handler for finding user role in Firebase Firestore
    const getUserRole = async (email) => {
        try {
            // Check the 'User' collection
            console.log("Checking user role for email:", email);
            const userQuery = query(collection(FirestoreDB, 'Users'), where('email', '==', email));
            const userSnapshot = await getDocs(userQuery);

            if (!userSnapshot.empty) {
                return 'User'; // User found in 'User' collection
            }

            // Check the 'Admin' collection
            const adminQuery = query(collection(FirestoreDB, 'Admin'), where('email', '==', email));
            const adminSnapshot = await getDocs(adminQuery);

            if (!adminSnapshot.empty) {
                return 'Admin'; // User found in 'Admin' collection
            }

            return null; // User not found in either collection
        } catch (error) {
            console.error('Error checking user role:', error);
            return null;
        }
    };

    // Handler for email input change
    const handleEmailChange = (event) => {
        setEmail(event.target.value);
    };

    // Handler for password input change
    const handlePasswordChange = (event) => {
        setPassword(event.target.value);
    };

    // Toggle password visibility
    const toggleShowPassword = () => {
        setShowPassword(!showPassword);
    };

    const handleClosePopup = () => {
        setError(null);
    };

    // Handle login with email and password
    const handleLogin = async (event) => {
        event.preventDefault();
        setError(null);  // Clear any previous error
        try {
            await signInWithEmailAndPassword(auth, email, password);
        } catch (error) {
            console.error("Login error:", error.message);
            setError(`Login failed: ${error.message}`);
            setLoading(false);
        }
    };

    // Handle Google login
    const handleGoogleLogin = async () => {
        try {
            const userCredential = await FBInstanceAuth.googleLogin(auth);
            const user = userCredential.user;
    
            if (user) {
                console.log("Google login successful");
                // onAuthStateChanged will handle the redirection
            } 
        } catch (error) {
            console.error("Google login error:", error.message);
            setError(`Google login failed: ${error.message}`);
            setLoading(false);
        }
    };
    

    // // Handle Facebook login
    // const handleFacebookLogin = async () => {
    //     try {
    //         const { user, errorCode } = await FBInstanceAuth.facebookLogin(auth);

    //         if (user) {
    //             console.log("Facebook login successful");
    //             // onAuthStateChanged will handle the redirection to '/home'
    //         } else {
    //             setError(`Facebook login failed: ${errorCode}`);
    //         }
    //     } catch (error) {
    //         console.error("Facebook login error:", error.message);
    //         setError(`Facebook login failed: ${error.message}`);
    //         setLoading(false);
    //     } 
    // };

    // Redirect to the sign-up page when the user clicks "Sign up"
    const handleSignUpClick = () => {
        navigate('/signup');
    };

    return (
        <Container fluid className="login">
            <Container fluid className="login-slide-left">
                <Img src={Icon} height={'418px'} width={'321px'} />
            </Container>
            <Container fluid className="login-slide-right">
                <Container fluid className='login-content'>
                    <h1>Login</h1>
                    <Container fluid className='login-details'>
                        <Form style={{ fontSize: "20px" }} onSubmit={handleLogin}>
                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>Email Address</Form.Label>
                                <Form.Control
                                    type="email"
                                    placeholder="Enter Email"
                                    onChange={handleEmailChange}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicPassword">
                                <Form.Label>Password</Form.Label>
                                <Form.Control
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Enter Password"
                                    onChange={handlePasswordChange}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicCheckbox">
                                <Form.Check
                                    type="checkbox"
                                    label="Show Password"
                                    onChange={toggleShowPassword}
                                />
                            </Form.Group>

                            <Button variant="primary" className='login-button' type="submit" style={{ backgroundColor: "#8F3013", border: "0" }}>
                                Login
                            </Button>

                            <div className="divider-container">
                                <hr className="divider-line" />
                                <span className="divider-text">or</span>
                                <hr className="divider-line" />
                            </div>

                            <Button variant="primary" className="login-button googleButton" type="button" style={{ backgroundColor: "#FFFFFF", color: "black", borderColor: "black" }}
                                onClick={handleGoogleLogin}>
                                <FaGoogle />
                                Login with Google
                            </Button>
                            
                            <div className="divider-container">
                                <span className="divider-text">
                                    Don't have an account?{' '}
                                    <span
                                        className="sign-up-link"
                                        onClick={handleSignUpClick}
                                    >
                                        Sign Up
                                    </span>
                                </span>
                            </div>
                        </Form>
                    </Container>
                </Container>
            </Container>
            {!loading && error && (
                <div className="popup">
                    <div className="popup-content">
                        <h2>Error</h2>
                        <p>{error}</p>
                        <Button variant="secondary" onClick={handleClosePopup}>Close</Button>
                    </div>
                </div>
            )}
        </Container>
    );
};

export default Login;
