import React from 'react';

import './FileBucketFiles.css'

export class FileBucketFiles extends React.Component {

    constructor(props) {
        super(props);
        let files = props.files.map((fileInformation) => {
            let lastModified = new Date(fileInformation.lastModified);
            this.props.addFile(fileInformation.path);
            let fileName = /[^/]*$/.exec(fileInformation.path)[0];
            console.log(fileName);
            return <div className="row" key={fileInformation.path}>
                <div className="small-column cell">
                    <input type="checkbox"
                           name={fileInformation.path}
                           onChange={this.props.handleSelectedFileChange}
                    />
                </div>
                <div className="column cell">
                    <a href={'http://localhost:8080/NimbusWebConsole/FileBucketAPI?bucketName='
                    + this.props.bucketName
                    + '&operation=getFile'
                    + '&destinationPath=' + fileInformation.path} download={fileName}>{fileInformation.path}</a>
                </div>

                <div className="column cell">{fileInformation.size} KB</div>
                <div
                    className="column cell">{lastModified.toLocaleTimeString()} {lastModified.toLocaleDateString()}</div>
            </div>
        });
        this.state = {
            files: files
        }
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="small-column table-header"/>
                    <div className="column table-header">File Path</div>
                    <div className="column table-header">Size</div>
                    <div className="column table-header">Last Modified</div>
                </div>
                {this.state.files}
            </div>
        )
    }
}