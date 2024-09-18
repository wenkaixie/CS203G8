import React, { useState, useEffect } from 'react';
import './Login.css';
import './Popup.css';
import Container from 'react-bootstrap/Container';
import Icon from '../../assets/images/icon.jpg';
import { Img } from 'react-image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import { FaGoogle, FaFacebook } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import FBInstanceAuth from "../../firebase/firebase_auth";  // Firebase auth class instance
import { signInWithEmailAndPassword, onAuthStateChanged } from 'firebase/auth';
import { collection, query, where, getDocs } from 'firebase/firestore';
import { FirestoreDB } from '../../firebase/firebase_config';

const Login = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);  // State for handling errors
    const [showPassword, setShowPassword] = useState(false);  // Show/hide password
    const [loading, setLoading] = useState(true); // Loading state for auth check

    const auth = FBInstanceAuth.getAuth();  // Get Firebase auth instance
    const navigate = useNavigate();  // React router's navigation hook

    useEffect(() => {
        // Listen for authentication state changes
        const unsubscribe = onAuthStateChanged(auth, (user) => {
            if (user) {
                const role = getUserRole(user.email);
                // Redirect user to the appropriate home page based on role
                if (role == 'User') {
                    navigate('/user/home');
                } else if (role == 'Admin') {
                    navigate('/admin/home');
                }
            } else {
                // User is not authenticated, show login form
                setLoading(false);
            }
        });

        // Clean up the listener on component unmount
        return () => unsubscribe();
    }, [auth, navigate]);

    // Handler for finding user role in firebase
    const getUserRole = async (email) => {
        console.log('Checking user role');
    
        try {
          const roles = ['User', 'Admin', 'Staff'];
    
          for (const role of roles) {
            const roleQuery = query(collection(FirestoreDB, role), where('Email', '==', email));
            const roleSnapshot = await getDocs(roleQuery);
    
            if (!roleSnapshot.empty) {
              console.log(`User is a ${role}`);
              const roleDoc = roleSnapshot.docs[0];
    
              // Store the uid and role in local storage
              localStorage.setItem('userDocID', roleDoc.id);
              localStorage.setItem('userRole', role);
              return role;
            }
          }
    
          console.log('User not found in any role');
          return null;
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
        }
    };

    // Handle Google login
    const handleGoogleLogin = async () => {
        try {
            const { user, errorCode } = await FBInstanceAuth.googleLogin(auth);

            if (user) {
                console.log("Google login successful");
                // onAuthStateChanged will handle the redirection to '/home'
            } else {
                setError(`Google login failed: ${errorCode}`);
            }
        } catch (error) {
            console.error("Google login error:", error.message);
            setError(`Google login failed: ${error.message}`);
        }
    };

    // Handle Facebook login
    const handleFacebookLogin = async () => {
        try {
            const { user, errorCode } = await FBInstanceAuth.facebookLogin(auth);

            if (user) {
                console.log("Facebook login successful");
                // onAuthStateChanged will handle the redirection to '/home'
            } else {
                setError(`Facebook login failed: ${errorCode}`);
            }
        } catch (error) {
            console.error("Facebook login error:", error.message);
            setError(`Facebook login failed: ${error.message}`);
        }
    };

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
                    <h1>Sign In</h1>
                    <Container fluid className='login-details'>
                        <Form style={{ fontSize: "20px" }} onSubmit={handleLogin}>
                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>Email address</Form.Label>
                                <Form.Control
                                    type="email"
                                    placeholder="Enter email"
                                    onChange={handleEmailChange}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicPassword">
                                <Form.Label>Password</Form.Label>
                                <Form.Control
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Enter password"
                                    onChange={handlePasswordChange}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicCheckbox">
                                <Form.Check
                                    type="checkbox"
                                    label="Show password"
                                    onChange={toggleShowPassword}
                                />
                            </Form.Group>

                            {error && <p className="error-message">{error}</p>}  {/* Display errors */}

                            <Button variant="primary" className='login-button' type="submit" style={{ backgroundColor: "#8F3013", border: "0" }}>
                                Sign In
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
                                        Sign up
                                    </span>
                                </span>
                            </div>
                        </Form>
                    </Container>
                </Container>
            </Container>
            {error && (
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
