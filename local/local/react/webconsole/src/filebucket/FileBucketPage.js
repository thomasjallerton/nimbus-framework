import React from 'react';
import './FileBucketPage.css';
import axios from 'axios'
import {FileBucketFiles} from "./FileBucketFiles";
import Upload from "./upload/Upload";

export class FileBucketPage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            selectedFiles: [],
            files: [],
            show: false
        }
    }

    handleSelectedFileChange = changeEvent => {
        const {name} = changeEvent.target;

        this.setState(prevState => ({
            selectedFiles: prevState.selectedFiles.map(file => {
                if (file.path === name) {
                    return {
                        path: name,
                        selected: !file.selected
                    }
                } else {
                    return file
                }
            })

        }));
    };

    addFile = checkboxName => {
        this.setState(prevState => ({
            selectedFiles: [...prevState.selectedFiles, {path: checkboxName, selected: false}]
        }))
    };

    getFiles() {
        axios.get('http://localhost:8080/NimbusWebConsole/FileBucketAPI?bucketName='
            + this.props.match.params.bucketName + '&operation=listFiles')
            .then(response => {
                    this.setState({
                        files: response.data
                    })
                }
            )
            .catch(error => {
                console.log(error);
            });
    }

    componentDidMount() {
        this.getFiles()
    }

    render() {
        return (
            <div className="main-body">
                <h2>{this.props.match.params.bucketName}</h2>
                <div className="row">
                    <button className="myButton" onClick={this.showModal}>Save</button>
                    <button className="myButton" onClick={this.delete}>Delete</button>
                </div>
                <br/>
                <FileBucketFiles
                    bucketName={this.props.match.params.bucketName}
                    files={this.state.files}
                    addFile={this.addFile}
                    key={this.state.files}
                    handleSelectedFileChange={this.handleSelectedFileChange}/>
                <Upload show={this.state.show}
                        bucketName={this.props.match.params.bucketName}
                        notifyUpdate={this.notifyUpdate}
                        closeButton={this.hideModal}/>
            </div>
        )
    }

    notifyUpdate = () => {
        this.getFiles()
    };

    showModal = () => {
        this.setState({
            show: true
        })
    };

    hideModal = () => {
        this.setState({
            show: false
        })
    };

    delete = () => {
        this.state.selectedFiles.map(file => {
            if (file.selected) {
                axios.post('http://localhost:8080/NimbusWebConsole/FileBucketAPI?bucketName='
                    + this.props.match.params.bucketName + '&operation=deleteFile'
                    + '&destinationPath=' + file.path,
                    {}
                ).then(response => {
                    console.log(response);
                    if (response.status === 200) {
                        this.setState({
                                files: this.state.files.filter(fileList => fileList.path !== file.path)
                            }
                        );
                    }
                    return null;
                });
            }
            return null;
        });
        return null;
    }
}

