import React from 'react';
import axios from 'axios';
import {Link} from "react-router-dom";

export class FileBucketList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            fileBuckets: []
        }
    }

    componentDidMount() {
        axios.get('http://localhost:8080/NimbusWebConsole/FileBucketAPI?operation=listBuckets')
            .then(response => {
                    let hoverStyle = {
                        ":hover": {
                            backgroundColor: "#efedeb",
                        }
                    };
                    let items = response.data.map((fileBucket) =>
                        <Link to={`${this.props.match.url}/${fileBucket.bucketName}`}
                              key={fileBucket.bucketName}
                              className="row-link">
                            <div className="row hover-row" style={hoverStyle}>
                                <div className="column cell">{fileBucket.bucketName}</div>
                                <div className="column cell">{fileBucket.numberOfFiles}</div>
                                <div className="column cell">{fileBucket.staticWebsite.toString()}</div>
                            </div>
                        </Link>
                    );
                    this.setState({
                        fileBuckets: items
                    })
                }
            )
            .catch(error => {
                console.log(error);
            });
    }

    render() {
        return (
            <div className="list-container">
                <div className="row">
                    <div className="column table-header">Bucket Name</div>
                    <div className="column table-header">Number of Files</div>
                    <div className="column table-header">Static Website</div>
                </div>
                {this.state.fileBuckets}
            </div>
        )
    }
}
