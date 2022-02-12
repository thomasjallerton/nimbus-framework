import React from 'react';
import './FunctionsPage.css';
import axios from "axios";
import {FunctionsList} from './FunctionsList'

export class FunctionsPage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            functions: [],
            version: 0
        };
    }

    getFunctions() {
        axios.get('http://localhost:8080/NimbusWebConsole/FunctionAPI?operation=listFunctions')
            .then(response => {
                    this.setState({
                        functions: response.data,
                        version: this.state.version+1
                    })
                }
            )
            .catch(error => {
                console.log(error);
            });
    }

    componentDidMount() {
        this.getFunctions()
    }


    render() {
        return (
            <div className="content" key="QueuesPage">
                <h2 key="title">Functions</h2>
                <br/>
                <FunctionsList
                    functions={this.state.functions}
                    key={this.state.version}
                    match={this.props.match}
                />
            </div>
        );
    }
}

