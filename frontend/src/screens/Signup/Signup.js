import React, { useState } from 'react';
import './Signup.css';
import Container from 'react-bootstrap/Container';
import Icon from '../../assets/images/icon.jpg';
import { Img } from 'react-image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import { FaGoogle } from 'react-icons/fa';
import { GoogleLogin } from '@react-oauth/google';


const Signup = () => {
    const [showPassword, setShowPassword] = useState(false);

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    return (
        <Container fluid className="signup">
            <Container fluid className="signup-slide-left">
                <Container fluid className='signup-content'>
                    <h1>Sign Up</h1>
                    <Container fluid className='signup-details'>
                        <Form style={{ fontSize:"20px" }}>
                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>Email address</Form.Label>
                                <Form.Control type="email" placeholder="Enter email" />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicPassword">
                                <Form.Label>Password</Form.Label>
                                <Form.Control 
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Enter password" 
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicPasswordRetype">
                                <Form.Label>Re-Enter Password</Form.Label>
                                <Form.Control 
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Re-enter password" 
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicCheckbox">
                                <Form.Check 
                                    type="checkbox" 
                                    label="Show password" 
                                    onChange={togglePasswordVisibility}
                                />
                            </Form.Group>
                            <Button variant="primary" className='login-button' type="submit" style={{ backgroundColor:"#8F3013", border:"0" }}>
                                Sign Up
                            </Button>
                            <div className="divider-container">
                                <hr className="divider-line" />
                                <span className="divider-text">or</span>
                                <hr className="divider-line" />
                            </div>
                            <Button variant="primary" className="login-button googleButton" type="submit" style={{ backgroundColor:"#FFFFFF", color:"black", borderColor:"black" }}>
                                <FaGoogle />
                                Sign Up with Google
                            </Button>
                        </Form>
                    </Container>
                </Container>
            </Container>
            <Container fluid className="signup-slide-right">
                <Img
                    src={Icon}
                    height={'418px'}
                    width={'321px'}
                />
            </Container>
        </Container>
    );
}

export default Signup;