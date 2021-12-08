import React, {Component} from "react";
import Dropzone from "../dropzone/Dropzone";
import axios from 'axios';
import "./Upload.css";
import icon from "../images/baseline-check_circle_outline-24px.svg";
import Progress from "../progress/Progress";

class Upload extends Component {
    constructor(props) {
        super(props);
        this.state = {
            files: [],
            uploading: false,
            uploadProgress: {},
            successfulUploaded: false
        };

        this.onFilesAdded = this.onFilesAdded.bind(this);
        this.uploadFiles = this.uploadFiles.bind(this);
        this.sendRequest = this.sendRequest.bind(this);
        this.renderActions = this.renderActions.bind(this);
    }

    onFilesAdded(files) {
        this.setState(prevState => ({
            files: prevState.files.concat(files)
        }));
    }

    async uploadFiles() {
        this.setState({uploadProgress: {}, uploading: true});
        const promises = [];
        this.state.files.forEach(file => {
            promises.push(this.sendRequest(file));
        });
        try {
            await Promise.all(promises);

            this.setState({successfulUploaded: true, uploading: false});
            this.props.notifyUpdate();
        } catch (e) {
            // Not Production ready! Do some error handling here instead...
            this.setState({successfulUploaded: true, uploading: false});
        }
    }

    sendRequest(file) {
        const formData = new FormData();
        formData.append("file", file, file.name);

        return axios.post('http://localhost:8080/NimbusWebConsole/FileBucketAPI?bucketName='
            + this.props.bucketName + '&operation=saveFile'
            + '&destinationPath=' + file.name,
            formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data'
                },
                onUploadProgress: progressEvent => {
                    const copy = {...this.state.uploadProgress};
                    copy[file.name] = {
                        state: "pending",
                        percentage: Math.round((progressEvent.loaded * 100) / progressEvent.total)
                    };
                    this.setState({uploadProgress: copy});
                }
            }
        )
    }

    renderProgress(file) {
        if (this.state.uploading || this.state.successfulUploaded) {
            let progress = this.state.uploadProgress[file.name] ? this.state.uploadProgress[file.name].percentage : 0;
            return (
                <div className="ProgressWrapper">
                    <Progress progress={progress} key={progress}/>
                    <img
                        className="CheckIcon"
                        alt="done"
                        src={icon}
                        style={{
                            opacity:
                                this.state.uploadProgress[file.name] && this.state.uploadProgress[file.name].state === "done" ? 1 : 0
                        }}
                    />
                </div>
            );
        }
    }

    renderActions() {
        if (this.state.successfulUploaded) {
            return (
                <div>
                    <button
                        onClick={() =>
                            this.setState({files: [], successfulUploaded: false})
                        }
                    >
                        Clear
                    </button>
                    <button onClick={this.props.closeButton}>
                        Cancel
                    </button>
                </div>
            );
        } else {
            return (
                <div>
                    <button
                        disabled={this.state.files.length <= 0 || this.state.uploading}
                        onClick={this.uploadFiles}
                    >
                        Upload
                    </button>
                    <button onClick={this.props.closeButton}>
                        Cancel
                    </button>
                </div>
            );
        }
    }

    render() {
        let showHideClassName = this.props.show ? "modal display-block" : "modal display-none";

        return (
            <div className={showHideClassName}>
                <section className="modal-main">
                    <div className="Upload">
                        <span className="Title">Upload Files</span>
                        <div className="Content">
                            <div>
                                <Dropzone
                                    onFilesAdded={this.onFilesAdded}
                                    disabled={this.state.uploading || this.state.successfulUploaded}
                                />
                            </div>
                            <div className="Files">
                                {this.state.files.map(file => {
                                    return (
                                        <div key={file.name} className="Row">
                                            <span className="Filename">{file.name}</span>
                                            {this.renderProgress(file)}
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                        <div className="Actions">{this.renderActions()}</div>
                    </div>
                </section>
            </div>
        );
    }
}

export default Upload;
