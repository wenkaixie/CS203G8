import React, { useState } from 'react';
import './Login.css';
import Container from 'react-bootstrap/Container';
import Icon from '../../assets/images/icon.jpg';
import { Img } from 'react-image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import { FaGoogle, FaFacebook } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import FBInstanceAuth from "../../firebase/firebase_auth";  // Firebase auth class instance
import { signInWithEmailAndPassword, getIdToken } from 'firebase/auth';

const Login = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);  // State for handling errors
    const [showPassword, setShowPassword] = useState(false);  // Show/hide password

    const auth = FBInstanceAuth.getAuth();  // Get Firebase auth instance
    const navigate = useNavigate();  // React router's navigation hook

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

    // Handle login with email and password
    const handleLogin = async (event) => {
        event.preventDefault();
        setError(null);  // Clear any previous error

        try {
            const userCredential = await signInWithEmailAndPassword(auth, email, password);
            const user = userCredential.user;

            if (user) {
                console.log("Login successful");

                // Retrieve the user ID token and store it
                const token = await getIdToken(user);
                localStorage.setItem('token', token);
                navigate('/home');  // Redirect to the home page upon successful login
            } else {
                setError("Invalid credentials");
            }
        } catch (error) {
            console.error("Login error:", error.message);
            setError(`Login failed: ${error.message}`);
        }
    };

    // Handle Google login
    const handleGoogleLogin = async () => {
        try {
            const userCredential = await FBInstanceAuth.googleLogin(auth);
            const user = userCredential.user;

            if (user) {
                console.log("Google login successful");

                const token = await getIdToken(user);
                localStorage.setItem('token', token);
                navigate('/home');  // Redirect to the home page upon successful Google login
            } else {
                setError("Invalid credentials");
            }
        } catch (error) {
            console.error("Google login error:", error.message);
            setError(`Google login failed: ${error.message}`);
        }
    };

    // Handle Facebook login
    const handleFacebookLogin = async () => {
        try {
            const userCredential = await FBInstanceAuth.facebookLogin(auth);
            const user = userCredential.user;

            if (user) {
                console.log("Facebook login successful");

                const token = await getIdToken(user);
                localStorage.setItem('token', token);
                navigate('/home');  // Redirect to the home page upon successful Facebook login
            } else {
                setError("Invalid credentials");
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

                            <Button 
                                variant="primary" 
                                className="login-button facebookButton"  
                                type="button" 
                                style={{ backgroundColor: "#3b5998", color: "white", borderColor: "#3b5998" }}  
                                onClick={handleFacebookLogin}>
                                <FaFacebook />
                                Login with Facebook
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
        </Container>
    );
};

export default Login;
