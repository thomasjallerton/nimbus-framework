import React from 'react';

import './FunctionsList.css'

export class FunctionsList extends React.Component {

    constructor(props) {
        super(props);
        let functions = props.functions.map(functionInformation => {
            return <div className="row" key={functionInformation.className}>
                <div className="column cell">{functionInformation.className}</div>
                <div className="column cell">{functionInformation.methodName}</div>
                <div className="column cell">{functionInformation.timesInvoked}</div>
            </div>
        });
        this.state = {
            functions: functions
        }
    }

    render() {
        return (
            <div>
                <div className="row" key="titles">
                    <div className="column table-header">Class Name</div>
                    <div className="column table-header">Method Name</div>
                    <div className="column table-header">Times Invoked</div>
                </div>
                {this.state.functions}
            </div>
        )
    }
}