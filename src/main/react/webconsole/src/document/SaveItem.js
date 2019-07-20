import React, {Component} from "react";
import axios from 'axios';
import "./SaveItem.css";

class SaveItem extends Component {
    constructor(props) {
        super(props);
        this.state = {
            item: "{}"
        };

        this.sendRequest = this.sendRequest.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }


    sendRequest(event) {
        console.log(this.state.item);
        axios.post('http://localhost:8080/NimbusWebConsole/DocumentStoreAPI?tableName='
            + this.props.tableName + '&operation=newItem',
            JSON.parse(this.state.item)
        ).then(response => {
                if (response.status === 200) {
                    this.props.notifyUpdate()
                }
            }
        );
        this.props.hideModal();
        event.preventDefault();
    }

    handleChange(event) {
        this.setState({item: event.target.value});
    }


    render() {
        let showHideClassName = this.props.show ? "modal display-block" : "modal display-none";

        return (
            <div className={showHideClassName}>
                <section className="modal-main">
                    <div className="SaveItem">
                        <span className="Title">New Item</span>
                        <form onSubmit={this.sendRequest}>
                            <textarea
                                rows="10"
                                value={this.state.item}
                                onChange={this.handleChange}/>
                            <div className="Actions">
                                <div>
                                    <input className="button" type="submit" value="Submit"/>
                                    <input className="button" type="button" value="Cancel"
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

export default SaveItem;
