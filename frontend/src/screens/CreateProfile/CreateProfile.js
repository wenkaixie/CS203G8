import React  from "react";
import './CreateProfile.css';
import logoImage from '../../assets/images/logo.png';
import profileImage from '../../assets/images/chess-profile-pic.jpg';
import { Img } from "react-image";
import Image from 'react-bootstrap/Image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';

const CreateProfile = () => {
    return(
        <div>
            <div className="icon-bar">
                <Img 
                    src={logoImage}
                    width={212}
                    height={72}
                />
            </div>
            <div className="create-profile-wrapper">
                <div className="create-profile-container">
                    <div className="create-profile-heading">
                        <h1>Create Profile</h1>
                    </div>
                    <div className="create-profile-icon-wrapper">
                        <div className="create-profile-icon">
                            <Image 
                                src={profileImage} 
                                className="create-profile-picture"
                                roundedCircle 
                            />
                        </div>
                    </div>
                    <div className="create-profile-details-wrapper">
                        <Form style={{ fontSize:"20px" }}>
                            <Form.Group className="mb-3" controlId="formBasicFirstName">
                                <Form.Label>First Name</Form.Label>
                                <Form.Control type="string" placeholder="Enter first name" />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicLastName">
                                <Form.Label>Last Name</Form.Label>
                                <Form.Control type="string" placeholder="Enter last name" />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicCountry">
                                <Form.Label>Country</Form.Label>
                                <Form.Control type="string" placeholder="Enter country" />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicAge">
                                <Form.Label>Age</Form.Label>
                                <Form.Control type="number" placeholder="Enter age" />
                            </Form.Group>
                            <div className="create-profile-button-wrapper">
                                <Button variant="primary" className='continue-button' type="submit">
                                    Continue
                                </Button>
                            </div>
                        </Form>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CreateProfile;