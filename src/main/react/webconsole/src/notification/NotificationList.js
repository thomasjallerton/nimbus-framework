import React from 'react';

import './NotificationList.css'

export class NotificationList extends React.Component {

    constructor(props) {
        super(props);
        console.log("Called!");
        let notificationTopics = props.notificationTopics.map(topicNotification => {
            return <div className="row" key={topicNotification.topicName}>
                <div className="small-column cell">
                    <input type="radio"
                           id={topicNotification.topicName}
                           name="selectedTopic"
                           onChange={props.handleSelectedTopicChange}
                    />
                </div>
                <div className="column cell">{topicNotification.topicName}</div>
                <div className="column cell">{topicNotification.subscribers}</div>
                <div className="column cell">{topicNotification.totalNotifications}</div>
            </div>
        });
        this.state = {
            notificationTopics: notificationTopics
        }
    }

    render() {
        return (
            <div>
                <div className="row" key="titles">
                    <div className="small-column table-header"/>
                    <div className="column table-header">Notification Topic</div>
                    <div className="column table-header">Number of Subscribers</div>
                    <div className="column table-header">Number of Notifications</div>
                </div>
                {this.state.notificationTopics}
            </div>
        )
    }
}