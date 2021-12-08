import React from 'react';
import './QueuesPage.css';
import axios from "axios";
import {QueuesList} from './QueuesList'
import AddItem from "./AddItem";

export class QueuesPage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            queues: [],
            selectedQueue: "",
            show: false,
            version: 0
        };
        this.handleSelectedQueueChange.bind(this);
    }

    getQueues() {
        axios.get('http://localhost:8080/NimbusWebConsole/QueueAPI?operation=listQueues')
            .then(response => {
                    this.setState({
                        queues: response.data,
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

    handleSelectedQueueChange = event => {
        this.setState({
            selectedQueue: event.target.id
        })
    };

    notifyUpdate = () => {
        this.getQueues()
    };


    render() {
        return (
            <div className="content" key="QueuesPage">
                <h2 key="title">Queues</h2>
                <div className="row" key="Actions">
                    <button className="myButton" onClick={this.showModal}>Add Item</button>
                </div>
                <br/>
                <QueuesList
                    queues={this.state.queues}
                    handleSelectedQueueChange={this.handleSelectedQueueChange}
                    key={this.state.version}
                />
                <AddItem
                    show={this.state.show}
                    notifyUpdate={this.notifyUpdate}
                    hideModal={this.hideModal}
                    key={this.state.selectedQueue}
                    queueName={this.state.selectedQueue}
                />
            </div>
        );
    }

    showModal = () => {
        if (this.state.selectedQueue !== "") {
            this.setState({
                show: true
            })
        } else {
            alert("Please select a queue")
        }
    };

    hideModal = () => {
        this.setState({
            show: false
        })
    };
}

