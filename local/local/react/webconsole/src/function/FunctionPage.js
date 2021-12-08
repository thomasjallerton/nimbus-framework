import './FunctionPage.css'
import React from 'react';
import axios from "axios";

export class FunctionPage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            body: <div/>
        }
    }

    static renderHttpFunction(functionInformation) {
        return <div>
            <h3>HTTP Function</h3>
            <div className="flex">
                <div> <b>Trigger:</b> {functionInformation.httpMethod.toUpperCase()} {functionInformation.httpPath}</div>
            </div>
        </div>
    }

    static renderBasicFunction(functionInformation) {
        return <div>
            <h3>Basic Function</h3>
            <div className="flex">
                <div> <b>Cron Rule:</b> {functionInformation.cronRule}</div>
            </div>
        </div>
    }

    static renderDocumentStoreFunction(functionInformation) {
        return <div>
            <h3>Document Store Function</h3>
            <div className="flex">
                <div><b>Table Name:</b> {functionInformation.tableName}</div>
                <div className="margin-left"><b>Event Type:</b> {functionInformation.eventType} </div>
            </div>
        </div>
    }

    static renderKeyValueStoreFunction(functionInformation) {
        return <div>
            <h3>Key-Value Store Function</h3>
            <div className="flex">
                <div><b>Table Name:</b> {functionInformation.tableName}</div>
                <div className="margin-left"><b>Event Type:</b> {functionInformation.eventType} </div>
            </div>
        </div>
    }

    static renderFileStorageFunction(functionInformation) {
        return <div>
            <h3>File Storage Function</h3>
            <div className="flex">
                <div><b>Bucket Name:</b> {functionInformation.bucketName}</div>
                <div className="margin-left"><b>Event Type:</b> {functionInformation.eventType} </div>
            </div>
        </div>
    }

    static renderNotificationFunction(functionInformation) {
        return <div>
            <h3>Notification Function</h3>
            <div className="flex">
                <div><b>Topic:</b> {functionInformation.notificationTopic} </div>
            </div>
        </div>
    }

    static renderQueueFunction(functionInformation) {
        return <div>
            <h3>Queue Function</h3>
            <div className="flex">
                <div><b>Queue Name:</b> {functionInformation.queueName} </div>
                <div className="margin-left"><b>Batch Size:</b> {functionInformation.batchSize} </div>
            </div>
        </div>
    }

    static renderWebSocketFunction(functionInformation) {
        return <div>
            <h3>WebSocket Function</h3>
            <div className="flex">
                <div><b>Topic:</b> {functionInformation.topic} </div>
            </div>
        </div>
    }

    componentDidMount() {
        console.log(this.props.match);
        axios.get('http://localhost:8080/NimbusWebConsole/FunctionAPI?operation=functionInformation'
            + '&className=' + this.props.match.params.className
            + '&methodName=' + this.props.match.params.methodName)
            .then(response => {
                    let body = <div/>;
                    switch (response.data.type) {
                        case 'HTTP':
                            body = FunctionPage.renderHttpFunction(response.data);
                            break;
                        case 'WEBSOCKET':
                            body = FunctionPage.renderWebSocketFunction(response.data);
                            break;
                        case 'QUEUE':
                            body = FunctionPage.renderQueueFunction(response.data);
                            break;
                        case 'NOTIFICATION':
                            body = FunctionPage.renderNotificationFunction(response.data);
                            break;
                        case 'DOCUMENT_STORE':
                            body = FunctionPage.renderDocumentStoreFunction(response.data);
                            break;
                        case 'KEY_VALUE_STORE':
                            body = FunctionPage.renderKeyValueStoreFunction(response.data);
                            break;
                        case 'FILE_STORAGE':
                            body = FunctionPage.renderFileStorageFunction(response.data);
                            break;
                        case 'BASIC':
                            body = FunctionPage.renderBasicFunction(response.data);
                            break;
                    }
                    this.setState({
                        body: body
                    })
                }
            )
            .catch(error => {
                console.log(error);
            });
    }

    render() {
        let str = this.props.match.params.className;
        let n = str.lastIndexOf('.');
        let className = str.substring(n + 1);
        return <div className="content">
            <h2>{className}::{this.props.match.params.methodName}</h2>
            {this.state.body}
        </div>
    }
}