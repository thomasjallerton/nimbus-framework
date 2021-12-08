import React from 'react';

import './DocumentStoreItems.css'

export class DocumentStoreItems extends React.Component {

    constructor(props) {
        super(props);

        let itemDescription = props.itemDescription;

        let attributeColumns = itemDescription.attributes.map(attribute =>
            <div key={attribute} className="column table-header">{attribute}</div>
        );

        let keyColumn = <div className="column table-header">{itemDescription.key}</div>;

        let items = props.items.map((item) => {

            let keyValue = item[itemDescription.key];
            this.props.addItem(keyValue);

            let checkBox = <div className="small-column cell">
                <input type="checkbox"
                       name={keyValue}
                       onChange={props.handleItemChange}
                />
            </div>;

            let itemColumns = itemDescription.attributes.map(attribute =>
                <div key={attribute} className="column cell">{JSON.stringify(item[attribute])}</div>
            );

            return <div className="row" key={keyValue}>
                {checkBox}
                <div className="column cell">{keyValue}</div>
                {itemColumns}
            </div>
        });
        this.state = {
            items: items,
            keyColumn: keyColumn,
            attributeColumns: attributeColumns
        }
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="small-column table-header"/>
                    {this.state.keyColumn}
                    {this.state.attributeColumns}
                </div>
                {this.state.items}
            </div>
        )
    }
}