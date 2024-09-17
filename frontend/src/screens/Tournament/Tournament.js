import React from "react";
import Tabs from "@mui/material/Tabs";
import "./style.css";

export const TournamentUpcoming = () => {
  return (
    <div className="tournament-upcoming">
      <div className="div">
        <Tabs>
          <div className="text-wrapper">Home</div>
          <div className="text-wrapper-2">Performance</div>
          <div className="text-wrapper-3">Tournament</div>
          <div className="text-wrapper-4">Calendar</div>
        </Tabs>
        <img className="male-user" alt="Male user" src="male-user.png" />
        <div className="text-wrapper-5">Tournament</div>
        <div className="filters">
          <div className="order-by-filter">
            <div className="overlap-group">
              <div className="text-wrapper-6">Order By</div>
            </div>
            <div className="overlap">
              <img className="arrow" alt="Arrow" />
            </div>
          </div>
          <div className="adjust">
            <img className="img" alt="Adjust" src="adjust.png" />
          </div>
        </div>
        <div className="search">
          <div className="overlap-2">
            <img className="search-2" alt="Search" src="search.png" />
            <input className="search-for-a" placeholder="Search for a tournament" type="text" />
          </div>
        </div>
        <div className="navbar">
          <div className="text-wrapper-7">Status</div>
          <div className="text-wrapper-8">Slots</div>
          <div className="text-wrapper-9">Location</div>
          <div className="text-wrapper-10">Date</div>
          <div className="text-wrapper-11">Name</div>
          <div className="text-wrapper-12">Save</div>
          <div className="text-wrapper-13">No</div>
          <div className="text-wrapper-14">Prize</div>
        </div>
        <div className="records">
          <div className="navbar-2">
            <div className="text-wrapper-15">Registered</div>
            <div className="text-wrapper-16">30</div>
            <div className="text-wrapper-17">Singapore</div>
            <img className="bookmark" alt="Bookmark" src="bookmark.png" />
            <p className="p">Jul 27, 2024 - 29 Jul, 2024</p>
            <div className="text-wrapper-18">Youth Chess Championships 2024</div>
            <div className="text-wrapper-19">1</div>
            <div className="text-wrapper-20">$50,000</div>
          </div>
          <div className="navbar-3">
            <div className="text-wrapper-15">Open registration</div>
            <div className="text-wrapper-16">30</div>
            <div className="text-wrapper-17">Singapore</div>
            <img className="bookmark" alt="Bookmark" src="bookmark.png" />
            <div className="text-wrapper-18">Youth Chess Championships 2024</div>
            <div className="text-wrapper-19">5</div>
            <div className="text-wrapper-20">$50,000</div>
            <p className="p">Jul 27, 2024 - 29 Jul, 2024</p>
          </div>
          <div className="navbar-4">
            <div className="text-wrapper-15">Published</div>
            <div className="text-wrapper-16">30</div>
            <div className="text-wrapper-17">Singapore</div>
            <img className="bookmark" alt="Bookmark" src="bookmark.png" />
            <div className="text-wrapper-18">Youth Chess Championships 2024</div>
            <div className="text-wrapper-19">4</div>
            <div className="text-wrapper-20">$50,000</div>
            <p className="p">Jul 27, 2024 - 29 Jul, 2024</p>
          </div>
          <div className="navbar-5">
            <div className="text-wrapper-15">Published</div>
            <div className="text-wrapper-16">30</div>
            <div className="text-wrapper-17">Singapore</div>
            <img className="bookmark" alt="Bookmark" src="bookmark.png" />
            <div className="text-wrapper-18">Youth Chess Championships 2024</div>
            <div className="text-wrapper-19">3</div>
            <div className="text-wrapper-20">$50,000</div>
            <p className="p">Jul 27, 2024 - 29 Jul, 2024</p>
          </div>
          <div className="navbar-6">
            <div className="text-wrapper-15">Open registration</div>
            <div className="text-wrapper-16">30</div>
            <div className="text-wrapper-17">Singapore</div>
            <img className="bookmark" alt="Bookmark" src="bookmark.png" />
            <div className="text-wrapper-18">Youth Chess Championships 2024</div>
            <div className="text-wrapper-19">2</div>
            <div className="text-wrapper-20">$50,000</div>
            <p className="p">Jul 27, 2024 - 29 Jul, 2024</p>
          </div>
        </div>
        <div className="text-wrapper-21">Show more</div>
        <div className="pages">
          <img className="proceed" alt="Proceed" src="image.svg" />
          <img className="proceed-2" alt="Proceed" src="proceed.svg" />
          <p className="element">
            <span className="span">1</span>
            <span className="text-wrapper-22">&nbsp;&nbsp;&nbsp;&nbsp; 2&nbsp;&nbsp;&nbsp;&nbsp; 3</span>
          </p>
        </div>
        <div className="options">
          <div className="overlap-3">
            <div className="text-wrapper-23">Ongoing</div>
            <div className="div-wrapper">
              <div className="text-wrapper-24">Upcoming</div>
            </div>
            <div className="text-wrapper-25">Past</div>
          </div>
        </div>
        <div className="logo">
          <div className="overlap-4">
            <img className="logo-2" alt="Logo" src="logo.png" />
            <img className="untitled-design" alt="Untitled design" src="untitled-design-2.png" />
          </div>
        </div>
      </div>
    </div>
  );
};