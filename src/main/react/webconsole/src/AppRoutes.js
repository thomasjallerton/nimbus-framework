import React from "react";
import {BrowserRouter as Router, Route} from "react-router-dom";
import FileBucketsPage from "./filebucket/FileBucketsPage"
import {FileBucketPage} from "./filebucket/FileBucketPage";
import {NavBar} from "./navbar/NavBar"
import {HomePage} from "./homepage/HomePage"
import DocumentStoresPage from "./document/DocumentStoresPage"
import {DocumentStorePage} from "./document/DocumentStorePage"
import KeyValueStoresPage from "./keyvalue/KeyValueStoresPage"
import {KeyValueStorePage} from "./keyvalue/KeyValueStorePage";
import {QueuesPage} from "./queue/QueuesPage"
import {NotificationsPage} from "./notification/NotificationsPage";
import {FunctionsPage} from "./function/FunctionsPage";

function AppRoutes() {
    return (
        <Router>
            <div>
                <NavBar/>

                <Route exact path="/" component={HomePage}/>

                <Route exact path="/Functions" component={FunctionsPage} />

                <Route exact path="/FileBuckets" component={FileBucketsPage} />
                <Route exact path="/FileBuckets/:bucketName" component={FileBucketPage}/>

                <Route exact path="/DocumentStores" component={DocumentStoresPage} />
                <Route exact path="/DocumentStores/:tableName" component={DocumentStorePage} />

                <Route exact path="/KeyValueStores" component={KeyValueStoresPage} />
                <Route exact path="/KeyValueStores/:tableName" component={KeyValueStorePage} />

                <Route exact path="/Queues" component={QueuesPage} />

                <Route exact path="/Notifications" component={NotificationsPage} />
            </div>
        </Router>
    );
}

export default AppRoutes;