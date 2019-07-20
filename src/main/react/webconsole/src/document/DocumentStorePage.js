import React from 'react';
import './DocumentStorePage.css';
import axios from 'axios'
import {DocumentStoreItems} from "./DocumentStoreItems";
import SaveItem from "./SaveItem"
export class DocumentStorePage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            selectedItems: [],
            itemDescription: {key: "", attributes: []},
            items: [],
            itemsKey: 0,
            show: false
        };
    }

    handleItemChange = changeEvent => {
        const {name} = changeEvent.target;
        this.setState(prevState => ({
            selectedItems: prevState.selectedItems.map(item => {
                if (item.key === name) {
                    return {
                        key: name,
                        selected: !item.selected
                    }
                } else {
                    return item
                }
            })
        }));
    };


    addItem = checkboxName => {
        this.setState(prevState => ({
            selectedItems: [...prevState.selectedItems, {key: checkboxName, selected: false}]
        }))
    };

    getItems() {
        axios.get('http://localhost:8080/NimbusWebConsole/DocumentStoreAPI?tableName='
            + this.props.match.params.tableName + '&operation=listTableItems')
            .then(response => {
                    this.setState({
                        items: response.data.items,
                        itemsKey: this.state.itemsKey+1,
                        itemDescription: response.data.itemDescription
                    });
                }
            )
            .catch(error => {
                console.log(error);
            });
    }

    componentDidMount() {
        this.getItems()
    }

    render() {
        return (
            <div className="main-body">
                <h2>{this.props.match.params.tableName}</h2>
                <div className="row">
                    <button className="myButton" onClick={this.showModal}>Save</button>
                    <button className="myButton" onClick={this.delete}>Delete</button>
                </div>
                <br/>
                <DocumentStoreItems
                    itemDescription={this.state.itemDescription}
                    items={this.state.items}
                    addItem={this.addItem}
                    key={this.state.itemsKey}
                    handleItemChange={this.handleItemChange}/>
                <SaveItem
                    show={this.state.show}
                    notifyUpdate={this.notifyUpdate}
                    hideModal={this.hideModal}
                    tableName={this.props.match.params.tableName}
                />
            </div>
        )
    }

    notifyUpdate = () => {
        this.getItems()
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
        this.state.selectedItems.map(item => {
            if (item.selected) {
                let data = {};
                data[this.state.itemDescription.key] = item.key;
                axios.post('http://localhost:8080/NimbusWebConsole/DocumentStoreAPI?tableName='
                    + this.props.match.params.tableName + '&operation=deleteItem',
                    data
                ).then(response => {
                    if (response.status === 200) {
                        this.setState({
                                items: this.state.items.filter(listItem =>
                                    listItem[this.state.itemDescription.key] !== item.key
                                ),
                                itemsKey: this.state.itemsKey+1
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

