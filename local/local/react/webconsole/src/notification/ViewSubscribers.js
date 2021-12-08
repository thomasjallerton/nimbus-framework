import React, {Component} from "react";
import "./ViewSubscribers.css";

class ViewSubscribers extends Component {
    constructor(props) {
        super(props);
        if (props.topic != null) {
            let generalSubscribers = props.topic.generalSubscribers.map(subscriber => {
                return <div className="row" key={subscriber.protocol + subscriber.endpoint}>
                    <div className="column cell">{subscriber.protocol}</div>
                    <div className="column cell">{subscriber.endpoint}</div>
                </div>
            });
            let functionSubscribers = props.topic.functionSubscribers.map(subscriber => {
                return <div className="row" key={subscriber.className + subscriber.methodName}>
                    <div className="column cell">{subscriber.className}</div>
                    <div className="column cell">{subscriber.methodName}</div>
                </div>
            });
            this.state = {
                functionSubscribers: functionSubscribers,
                generalSubscribers: generalSubscribers
            };
        } else {
            this.state = {
                functionSubscribers: [],
                generalSubscribers: []
            };
        }
    }

    render() {
        let showHideClassName = this.props.show ? "modal display-block" : "modal display-none";

        let topicName = this.props.topic ? this.props.topic.topicName : "";
        return (
            <div className={showHideClassName}>
                <section className="modal-main">
                    <div className="Subscribers">
                        <form onSubmit={this.sendRequest}>
                            <h2>{topicName} Subscribers</h2>

                            <h4>General Subscribers</h4>
                            <div className="row">
                                <div className="column table-header">Protocol</div>
                                <div className="column table-header">Endpoint</div>
                            </div>
                            {this.state.generalSubscribers}
                            <h4>Function Subscribers</h4>
                            <div className="row">
                                <div className="column table-header">Class Name</div>
                                <div className="column table-header">Method Name</div>
                            </div>
                            {this.state.functionSubscribers}
                            <div className="Actions">
                                <div>
                                    <input className="button" type="button" value="Close"
                                           onClick={this.props.hideModal}/>
                                </div>
                            </div>
                        </form>
                    </div>
                </section>
            </div>
        );
    }
}

export default ViewSubscribers;
