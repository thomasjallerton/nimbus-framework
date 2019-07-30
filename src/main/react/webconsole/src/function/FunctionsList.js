import React from 'react';

import './FunctionsList.css'
import {Link} from "react-router-dom";

export class FunctionsList extends React.Component {

    constructor(props) {
        super(props);
        let hoverStyle = {
            ":hover": {
                backgroundColor: "#efedeb",
            }
        };
        let functions = props.functions.map(functionInformation => {
            return <Link
                to={`${this.props.match.url}/${functionInformation.qualifiedClassName}/${functionInformation.methodName}`}
                key={functionInformation.className + functionInformation.methodName}
                className="row-link">
                <div className="row hover-row" style={hoverStyle}>
                    <div className="column cell">{functionInformation.className}</div>
                    <div className="column cell">{functionInformation.methodName}</div>
                    <div className="column cell">{functionInformation.timesInvoked}</div>
                </div>
            </Link>

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