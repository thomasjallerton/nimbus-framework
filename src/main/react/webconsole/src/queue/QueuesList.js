import React from 'react';

import './QueuesList.css'

export class QueuesList extends React.Component {

    constructor(props) {
        super(props);
        let queues = props.queues.map((queueInformation) => {
            return <div className="row" key={queueInformation.queueName}>
                <div className="small-column cell">
                    <input type="radio"
                           id={queueInformation.queueName}
                           name="selectedQueue"
                           onChange={props.handleSelectedQueueChange}
                    />
                </div>
                <div className="column cell">{queueInformation.queueName}</div>
                <div className="column cell">{queueInformation.itemsPushed}</div>
            </div>
        });
        this.state = {
            queues: queues
        }
    }

    render() {
        return (
            <div>
                <div className="row" key="titles">
                    <div className="small-column table-header"/>
                    <div className="column table-header">Queue Name</div>
                    <div className="column table-header">Items Pushed</div>
                </div>
                {this.state.queues}
            </div>
        )
    }
}