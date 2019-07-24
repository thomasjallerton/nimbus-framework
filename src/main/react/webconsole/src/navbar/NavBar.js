import React from "react";
import "./NavBar.css";
import {NavLink, Link} from "react-router-dom";

export function NavBar() {
    return (
        <nav>
            <ul>
                <li>
                    <NavLink exact activeClassName="active" className="navlink" to="/NimbusWebConsole">Home</NavLink>
                </li>
                <li className="dropdown">
                    <Link className="navlink" to="/">Resources</Link>
                    <div className="dropdown-content">
                        <Link className="navlink" to="/NimbusWebConsole/Functions">Functions</Link>
                        <Link className="navlink" to="/NimbusWebConsole/FileBuckets">File Buckets</Link>
                        <Link className="navlink" to="/NimbusWebConsole/DocumentStores">Document Stores</Link>
                        <Link className="navlink" to="/NimbusWebConsole/KeyValueStores">Key-Value Stores</Link>
                        <Link className="navlink" to="/NimbusWebConsole/Queues">Queues</Link>
                        <Link className="navlink" to="/NimbusWebConsole/Notifications">Notification Topics</Link>
                    </div>
                </li>
            </ul>
        </nav>
    )
}