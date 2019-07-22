import React from 'react';
import './NotificationsPage.css';
import axios from "axios";
import {NotificationList} from './NotificationList'
import Notify from "./Notify";
import ViewSubscribers from "./ViewSubscribers";

export class NotificationsPage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            notificationTopics: [],
            selectedTopic: "",
            showNotify: false,
            showSubscribers: false,
            version: 0
        };
        this.handleSelectedTopicChange.bind(this);
    }

    getQueues() {
        axios.get('http://localhost:8080/NimbusWebConsole/NotificationAPI?operation=listNotificationTopics')
            .then(response => {
                    this.setState({
                        notificationTopics: response.data,
                        version: this.state.version+1
                    })
                }
            )
            .catch(error => {
                console.log(error);
            });
    }

    componentDidMount() {
        this.getQueues()
    }

    handleSelectedTopicChange = event => {
        this.setState({
            selectedTopic: event.target.id
        })
    };

    notifyUpdate = () => {
        this.getQueues()
    };


    render() {
        return (
            <div className="content" key="QueuesPage">
                <h2 key="title">Notification Topics</h2>
                <div className="row" key="Actions">
                    <button className="myButton" onClick={this.showNotifyModal}>Notify</button>
                    <button className="myButton" onClick={this.showSubscriberModal}>View Subscribers</button>
                </div>
                <br/>
                <NotificationList
                    notificationTopics={this.state.notificationTopics}
                    handleSelectedTopicChange={this.handleSelectedTopicChange}
                    key={this.state.version}
                />
                <Notify
                    show={this.state.showNotify}
                    notifyUpdate={this.notifyUpdate}
                    hideModal={this.hideNotifyModal}
                    key={this.state.selectedTopic}
                    topicName={this.state.selectedTopic}
                />
                <ViewSubscribers
                    show={this.state.showSubscribers}
                    notifyUpdate={this.notifyUpdate}
                    hideModal={this.hideSubscriberModal}
                    key={this.state.selectedTopic}
                    topic={
                        this.state.notificationTopics.find(topic => {
                            return topic.topicName === this.state.selectedTopic
                        })
                    }/>
            </div>
        );
    }

    showNotifyModal = () => {
        this.setState({
            showNotify: true
        })
    };

    hideNotifyModal = () => {
        this.setState({
            showNotify: false
        })
    };

    showSubscriberModal = () => {
        this.setState({
            showSubscribers: true
        })
    };

    hideSubscriberModal = () => {
        this.setState({
            showSubscribers: false
        })
    };
}

