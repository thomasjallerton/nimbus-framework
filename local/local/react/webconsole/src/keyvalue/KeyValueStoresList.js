import React from 'react';
import axios from 'axios';
import {Link} from "react-router-dom";

export class KeyValueStoresList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            tables: []
        }
    }

    componentDidMount() {
        axios.get('http://localhost:8080/NimbusWebConsole/KeyValueStoreAPI?operation=listTables')
            .then(response => {
                    let hoverStyle = {
                        ":hover": {
                            backgroundColor: "#efedeb",
                        }
                    };
                    let items = response.data.map((table) =>
                        <Link to={`${this.props.match.url}/${table.tableName}`}
                              key={table.tableName}
                              className="row-link">
                            <div className="row hover-row" style={hoverStyle}>
                                <div className="column cell">{table.tableName}</div>
                                <div className="column cell">{table.numberOfItems}</div>
                            </div>
                        </Link>
                    );
                    this.setState({
                        tables: items
                    })
                }
            )
            .catch(error => {
                console.log(error);
            });
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="column table-header">Table Name</div>
                    <div className="column table-header">Number of Items</div>
                </div>
                {this.state.tables}
            </div>
        )
    }
}
