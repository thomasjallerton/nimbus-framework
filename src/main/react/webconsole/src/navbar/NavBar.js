import React from "react";
import "./NavBar.css";
import {NavLink, Link} from "react-router-dom";

export function NavBar() {
    return (
        <nav>
            <ul>
                <li>
                    <NavLink exact activeClassName="active" className="navlink" to="/">Home</NavLink>
                </li>
                <li className="dropdown">
                    <Link className="navlink" to="/">Resources</Link>
                    <div className="dropdown-content">
                        <Link className="navlink" to="/Functions">Functions</Link>
                        <Link className="navlink" to="/FileBuckets">File Buckets</Link>
                        <Link className="navlink" to="/DocumentStores">Document Stores</Link>
                        <Link className="navlink" to="/KeyValueStores">Key-Value Stores</Link>
                        <Link className="navlink" to="/Queues">Queues</Link>
                        <Link className="navlink" to="/Notifications">Notification Topics</Link>
                    </div>
                </li>
            </ul>
        </nav>
    )
}