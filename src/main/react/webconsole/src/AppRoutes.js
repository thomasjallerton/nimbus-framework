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

                <Route exact path="/NimbusWebConsole/" component={HomePage}/>
                <Route exact path="/NimbusWebConsole/index.html" component={HomePage}/>

                <Route exact path="/NimbusWebConsole/Functions" component={FunctionsPage} />

                <Route exact path="/NimbusWebConsole/FileBuckets" component={FileBucketsPage} />
                <Route exact path="/NimbusWebConsole/FileBuckets/:bucketName" component={FileBucketPage}/>

                <Route exact path="/NimbusWebConsole/DocumentStores" component={DocumentStoresPage} />
                <Route exact path="/NimbusWebConsole/DocumentStores/:tableName" component={DocumentStorePage} />

                <Route exact path="/NimbusWebConsole/KeyValueStores" component={KeyValueStoresPage} />
                <Route exact path="/NimbusWebConsole/KeyValueStores/:tableName" component={KeyValueStorePage} />

                <Route exact path="/NimbusWebConsole/Queues" component={QueuesPage} />

                <Route exact path="/NimbusWebConsole/Notifications" component={NotificationsPage} />
            </div>
        </Router>
    );
}

export default AppRoutes;